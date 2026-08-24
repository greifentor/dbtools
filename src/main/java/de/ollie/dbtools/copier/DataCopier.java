package de.ollie.dbtools.copier;

import de.ollie.dbtools.modelreader.DBDataScheme;
import de.ollie.dbtools.modelreader.DBForeignKey;
import de.ollie.dbtools.modelreader.DBTable;
import de.ollie.dbtools.modelreader.DBTypeConverter;
import de.ollie.dbtools.modelreader.DefaultDBObjectFactory;
import de.ollie.dbtools.modelreader.jdbc.JDBCForeignKeyReader;
import de.ollie.dbtools.modelreader.jdbc.JDBCModelReader;
import de.ollie.dbtools.utils.StatementBuilder;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class which is able to copy data from one JDBC connection to another one.
 * Note that both connection should have equal data schemes.
 *
 * @author Oliver.Lieshoff
 *
 */
public class DataCopier {

	static Logger log = LogManager.getLogger(DataCopier.class);
	static JDBCForeignKeyReader foreignKeyReader = new JDBCForeignKeyReader();
	static ForeignKeyRemover foreignKeyRemover = new ForeignKeyRemover();
	static ForeignKeyRestorer foreignKeyRestorer = new ForeignKeyRestorer();
	static SequenceUpdater sequenceUpater = new SequenceUpdater();

	private StatementBuilder statementBuilder;
	private int recordsPerDot = 100;

	/**
	 * Creates a new DataCopier object with the passed parameters.
	 *
	 * @param statementBuilder A reference to a statement builder.
	 */
	public DataCopier(StatementBuilder statementBuilder) {
		super();
		this.statementBuilder = statementBuilder;
	}

	public void copy(
		Connection sourceConnection,
		Connection targetConnection,
		boolean deleteBeforeCopy,
		List<String> includeTableNamePatterns,
		List<String> excludeTableNames,
		Map<String, String> tableNameMappings,
		String schemeName,
		int recordsPerDot
	) throws Exception {
		this.recordsPerDot = recordsPerDot;
		DBDataScheme model = new JDBCModelReader(
			new DefaultDBObjectFactory(),
			new DBTypeConverter(),
			sourceConnection,
			schemeName,
			includeTableNamePatterns,
			excludeTableNames
		)
			.readModel();
		List<DBForeignKey<?>> fksTargetDb = readForeignKeys(targetConnection, schemeName, model);
		printDetectedForeignKeys(fksTargetDb);
		boolean autoCommit = targetConnection.getAutoCommit();
		targetConnection.setAutoCommit(false);
		try {
			foreignKeyRemover.remove(fksTargetDb, targetConnection, statementBuilder);
			ensureForeignKeysAreRemoved(targetConnection, schemeName, model);
			for (DBTable table : model.getTables()) {
				if (deleteBeforeCopy) {
					deleteTableData(table, targetConnection, tableNameMappings);
				}
				copyTableData(table, sourceConnection, targetConnection, tableNameMappings);
			}
			foreignKeyRestorer.restore(fksTargetDb, targetConnection, statementBuilder);
			sequenceUpater.update(schemeName, targetConnection, model);
			targetConnection.commit();
		} catch (Exception e) {
			System.out.println("error while copying, rolling back the transaction: " + e.getMessage());
			targetConnection.rollback();
			restoreRemovedForeignKeys(targetConnection, schemeName, model, fksTargetDb);
			throw e;
		} finally {
			targetConnection.setAutoCommit(autoCommit);
		}
	}

	private void restoreRemovedForeignKeys(
		Connection targetConnection,
		String schemeName,
		DBDataScheme model,
		List<DBForeignKey<?>> removedForeignKeys
	) {
		try {
			rollBackRemoval(removedForeignKeys, readForeignKeys(targetConnection, schemeName, model), targetConnection);
			targetConnection.commit();
		} catch (Exception e) {
			log.error("foreign keys could not be restored after the rollback: " + e.getMessage(), e);
		}
	}

	private List<DBForeignKey<?>> readForeignKeys(Connection targetConnection, String schemeName, DBDataScheme model)
		throws SQLException {
		return foreignKeyReader.getForeignKeys(targetConnection.getMetaData(), schemeName, model.getTables());
	}

	private void ensureForeignKeysAreRemoved(Connection targetConnection, String schemeName, DBDataScheme model)
		throws Exception {
		List<DBForeignKey<?>> stillPresentForeignKeys = readForeignKeys(targetConnection, schemeName, model);
		if (stillPresentForeignKeys.isEmpty()) {
			return;
		}
		printForeignKeys("foreign key still present after removal: ", stillPresentForeignKeys);
		waitForKeyPressed();
		foreignKeyRemover.remove(stillPresentForeignKeys, targetConnection, statementBuilder);
		stillPresentForeignKeys = readForeignKeys(targetConnection, schemeName, model);
		if (stillPresentForeignKeys.isEmpty()) {
			System.out.println("all foreign keys removed by the second attempt.");
			return;
		}
		printForeignKeys("foreign key not removable: ", stillPresentForeignKeys);
		throw new IllegalStateException("foreign keys could not be removed: " + getNames(stillPresentForeignKeys));
	}

	private void printDetectedForeignKeys(List<DBForeignKey<?>> foreignKeys) {
		System.out.println("foreign keys detected in the target database: " + foreignKeys.size());
		for (DBForeignKey<?> fk : foreignKeys) {
			System.out.println(
				"- " +
				fk.getName() +
				": table=" +
				fk.getTableName() +
				", key columns=" +
				fk.getKeyColumnNames() +
				", referenced table=" +
				fk.getReferencedTableName() +
				", referenced key columns=" +
				fk.getReferencedKeyColumnNames()
			);
		}
	}

	private void printForeignKeys(String message, List<DBForeignKey<?>> foreignKeys) {
		for (DBForeignKey<?> fk : foreignKeys) {
			System.out.println(message + fk.getName() + " on table " + fk.getTableName());
		}
	}

	private void rollBackRemoval(
		List<DBForeignKey<?>> removedForeignKeys,
		List<DBForeignKey<?>> stillPresentForeignKeys,
		Connection targetConnection
	) throws SQLException {
		List<DBForeignKey<?>> foreignKeysToRestore = removedForeignKeys
			.stream()
			.filter(fk -> !isContained(fk, stillPresentForeignKeys))
			.toList();
		System.out.println("rolling back the removal of " + foreignKeysToRestore.size() + " foreign key(s)");
		foreignKeyRestorer.restore(foreignKeysToRestore, targetConnection, statementBuilder);
	}

	private boolean isContained(DBForeignKey<?> foreignKey, List<DBForeignKey<?>> foreignKeys) {
		return foreignKeys
			.stream()
			.anyMatch(fk -> fk.getName().equals(foreignKey.getName()) && fk.getTableName().equals(foreignKey.getTableName()));
	}

	private String getNames(List<DBForeignKey<?>> foreignKeys) {
		return foreignKeys.stream().map(DBForeignKey::getName).reduce((s0, s1) -> s0 + ", " + s1).orElse("");
	}

	private void waitForKeyPressed() throws IOException {
		System.out.println("press ENTER to continue ...");
		System.in.read();
	}

	private void deleteTableData(DBTable table, Connection connection, Map<String, String> tableNameMappings)
		throws Exception {
		try (Statement statement = connection.createStatement()) {
			String stmt = "DELETE FROM " + getMappedTableName(table, tableNameMappings);
			System.out.println("running: " + stmt);
			statement.executeUpdate(stmt);
		}
	}

	private String getMappedTableName(DBTable table, Map<String, String> tableNameMappings) {
		return (tableNameMappings != null) && tableNameMappings.containsKey(table.getName())
			? tableNameMappings.get(table.getName())
			: table.getName();
	}

	private void copyTableData(
		DBTable table,
		Connection sourceConnection,
		Connection targetConnection,
		Map<String, String> tableNameMappings
	) throws SQLException {
		String tableName = getMappedTableName(table, tableNameMappings);
		String select = statementBuilder.createSelectStatementString(table);
		String insert = statementBuilder.createInsertStatementString(table, tableName);
		Statement sourceStatement = sourceConnection.createStatement();
		PreparedStatement targetStatement = targetConnection.prepareStatement(insert);
		long count = count(table.getName(), sourceConnection);
		System.out.print("copying: " + tableName + " with " + count + " record(s) ");
		ResultSet rs = sourceStatement.executeQuery(select);
		long current = 0;
		while (rs.next()) {
			for (int i = 0, leni = rs.getMetaData().getColumnCount(); i < leni; i++) {
				targetStatement.setObject(i + 1, rs.getObject(i + 1));
			}
			targetStatement.executeUpdate();
			current++;
			if (current % recordsPerDot == 0) {
				System.out.print(".");
			}
			log.info(
				"copied record number " +
				current +
				" (" +
				count +
				") for table: " +
				table.getName() +
				(tableName.equals(table.getName()) ? "" : " -> " + tableName)
			);
		}
		rs.close();
		sourceStatement.close();
		targetStatement.close();
		System.out.println(" ready.");
	}

	private long count(String tableName, Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		long count = 0;
		ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + tableName);
		if (rs.next()) {
			count = rs.getLong(1);
		}
		rs.close();
		statement.close();
		return count;
	}
}
