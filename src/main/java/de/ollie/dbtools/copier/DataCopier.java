package de.ollie.dbtools.copier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ollie.dbtools.modelreader.DBDataScheme;
import de.ollie.dbtools.modelreader.DBTable;
import de.ollie.dbtools.modelreader.DBTypeConverter;
import de.ollie.dbtools.modelreader.DefaultDBObjectFactory;
import de.ollie.dbtools.modelreader.jdbc.JDBCModelReader;
import de.ollie.dbtools.utils.StatementBuilder;

/**
 * A class which is able to copy data from one JDBC connection to another one. Note that both connection should have
 * equal data schemes.
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
		this.statementBuilder = statementBuilder;
	}

	/**
	 * Copies the data from the source to the target JDBC connection.
	 * 
	 * <P>
	 * <B>Note:</B> the data schemes of the two databases referenced by the connection should be equal. In minimum the
	 * tables and fields of the source database must be members of the target database.
	 * 
	 * @param sourceConnection         The connection which the data are read from.
	 * @param targetConnection         The connection which the data are to write into.
	 * @param deleteBeforeCopy         Set this flag to delete all data from the tables in the target connection. Not
	 *                                 that only the data of those tables are deleted which are included by the copy
	 *                                 process.
	 * @param tableNameMappings        A map with mappings for table names which differs in source and target scheme.
	 * @param includeTableNamePatterns A list with the table name patterns. Only one have to match to import a table.
	 * @throws Exception If an error occurs while copying the data.
	 */
	public void copy(Connection sourceConnection, Connection targetConnection, boolean deleteBeforeCopy,
			List<String> includeTableNamePatterns, Map<String, String> tableNameMappings, String schemeName)
			throws Exception {
		DBDataScheme model = new JDBCModelReader(new DefaultDBObjectFactory(), new DBTypeConverter(), sourceConnection,
				schemeName, includeTableNamePatterns).readModel();
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

	private void copyTableData(DBTable table, Connection sourceConnection, Connection targetConnection,
			Map<String, String> tableNameMappings) throws SQLException {
		String tableName = getMappedTableName(table, tableNameMappings);
		String select = this.statementBuilder.createSelectStatementString(table);
		String insert = this.statementBuilder.createInsertStatementString(table, tableName);
		Statement sourceStatement = sourceConnection.createStatement();
		PreparedStatement targetStatement = targetConnection.prepareStatement(insert);
		log.info("selecting by: " + select);
		ResultSet rs = sourceStatement.executeQuery(select);
		long count = count(table.getName(), sourceConnection);
		long current = 0;
		while (rs.next()) {
			for (int i = 0, leni = rs.getMetaData().getColumnCount(); i < leni; i++) {
				targetStatement.setObject(i + 1, rs.getObject(i + 1));
			}
			targetStatement.executeUpdate();
			current++;
			log.info("copied record number " + current + " (" + count + ") for table: " + table.getName()
					+ (tableName.equals(table.getName()) ? "" : " -> " + tableName));
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

	public void list(Connection sourceConnection, List<String> includeTableNamePatterns) throws Exception {
		DBDataScheme model = new JDBCModelReader(
				new DefaultDBObjectFactory(), new DBTypeConverter(), sourceConnection, null, includeTableNamePatterns)
						.readModel();
		for (DBTable table : model.getTables()) {
			log.info("found: " + table.getName());
		}
	}

}