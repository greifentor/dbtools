package de.ollie.dbtools.copier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
	 * @param sourceConnection The connection which the data are read from.
	 * @param targetConnection The connection which the data are to write into.
	 * @param deleteBeforeCopy Set this flag to delete all data from the tables in the target connection. Not that only
	 *                         the data of those tables are deleted which are included by the copy process.
	 * @throws Exception If an error occurs while copying the data.
	 */
	public void copy(Connection sourceConnection, Connection targetConnection, boolean deleteBeforeCopy)
			throws Exception {
		DBDataScheme model = new JDBCModelReader(new DefaultDBObjectFactory(), new DBTypeConverter(), sourceConnection,
				null).readModel();
		for (DBTable table : model.getTables()) {
			copyTableData(table, sourceConnection, targetConnection);
		}
	}

	private void copyTableData(DBTable table, Connection sourceConnection, Connection targetConnection)
			throws SQLException {
		String select = this.statementBuilder.createSelectStatementString(table);
		String insert = this.statementBuilder.createInsertStatementString(table);
		Statement sourceStatement = sourceConnection.createStatement();
		PreparedStatement targetStatement = targetConnection.prepareStatement(insert);
		ResultSet rs = sourceStatement.executeQuery(select);
		while (rs.next()) {
			for (int i = 0, leni = rs.getMetaData().getColumnCount(); i < leni; i++) {
				targetStatement.setObject(i + 1, rs.getObject(i + 1));
			}
			targetStatement.executeUpdate();
		}
		rs.close();
		sourceStatement.close();
	}

}