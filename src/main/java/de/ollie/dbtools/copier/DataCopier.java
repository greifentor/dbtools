package de.ollie.dbtools.copier;

import de.ollie.dbtools.modelreader.DBDataScheme;
import de.ollie.dbtools.modelreader.DBTable;
import de.ollie.dbtools.modelreader.DBTypeConverter;
import de.ollie.dbtools.modelreader.DefaultDBObjectFactory;
import de.ollie.dbtools.modelreader.jdbc.JDBCModelReader;
import de.ollie.dbtools.utils.StatementBuilder;
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
	static ForeignKeyRemover foreignKeyRemover = new ForeignKeyRemover();
	static ForeignKeyRestorer foreignKeyRestorer = new ForeignKeyRestorer();

	private StatementBuilder statementBuilder;

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
		String schemeName
	) throws Exception {
		DBDataScheme model = new JDBCModelReader(
			new DefaultDBObjectFactory(),
			new DBTypeConverter(),
			sourceConnection,
			schemeName,
			includeTableNamePatterns,
			excludeTableNames
		)
			.readModel();
		foreignKeyRemover.remove(model.getForeignKeys(), targetConnection, statementBuilder);
		for (DBTable table : model.getTables()) {
			if (deleteBeforeCopy) {
				deleteTableData(table, targetConnection, tableNameMappings);
			}
			copyTableData(table, sourceConnection, targetConnection, tableNameMappings);
		}
		foreignKeyRestorer.restore(model.getForeignKeys(), targetConnection, statementBuilder);
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
			if (current % 100 == 0) {
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
