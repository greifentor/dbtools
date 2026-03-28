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

	private StatementBuilder statementBuilder;

	/**
	 * Creates a new DataCopier object with the passed parameters.
	 *
	 * @param statementBuilder A reference to a statement builder.
	 */
	public DataCopier(StatementBuilder statementBuilder) {
		super();
		statementBuilder = statementBuilder;
	}

	public void copy(
		Connection sourceConnection,
		Connection targetConnection,
		boolean deleteBeforeCopy,
		List<String> includeTableNamePatterns,
		Map<String, String> tableNameMappings,
		String schemeName
	) throws Exception {
		DBDataScheme model = new JDBCModelReader(
			new DefaultDBObjectFactory(),
			new DBTypeConverter(),
			sourceConnection,
			null,
			includeTableNamePatterns
		)
			.readModel();
		for (DBTable table : model.getTables()) {
			if (deleteBeforeCopy) {
				deleteTableData(table, targetConnection, tableNameMappings);
			}
			copyTableData(table, sourceConnection, targetConnection, tableNameMappings);
		}
	}

	private void deleteTableData(DBTable table, Connection connection, Map<String, String> tableNameMappings)
		throws Exception {
		Statement statement = connection.createStatement();
		statement.executeUpdate("DELETE FROM " + getMappedTableName(table, tableNameMappings));
		statement.close();
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
		ResultSet rs = sourceStatement.executeQuery(select);
		long count = count(table.getName(), sourceConnection);
		long current = 0;
		while (rs.next()) {
			for (int i = 0, leni = rs.getMetaData().getColumnCount(); i < leni; i++) {
				targetStatement.setObject(i + 1, rs.getObject(i + 1));
			}
			targetStatement.executeUpdate();
			current++;
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
