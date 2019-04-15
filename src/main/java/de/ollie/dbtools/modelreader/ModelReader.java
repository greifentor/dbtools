/*
 * ModelReader.java
 *
 * 20.09.2018
 *
 * (c) by O.Lieshoff 
 */
package de.ollie.dbtools.modelreader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import de.ollie.dbtools.modelreader.models.DBColumnModel;
import de.ollie.dbtools.modelreader.models.DBDataModel;
import de.ollie.dbtools.modelreader.models.DBIndexModel;
import de.ollie.dbtools.modelreader.models.DBSequenceModel;
import de.ollie.dbtools.modelreader.models.DBTableModel;

/**
 * A class which is able to read the meta data of a database.
 *
 * @author O.Lieshoff
 */
public class ModelReader {

	/**
	 * Read the data model which is representing the database linked to the passed connection.
	 *
	 * @param connection The connection whose data model should be read.
	 * @return The data model for the database which is linked by the passed connection.
	 * @throws SQLException             If an error occurs while accessing the database.
	 * @throws IllegalArgumentException Passing null value.
	 */
	public DBDataModel readModel(Connection connection) throws SQLException {
		DatabaseMetaData dbmd = connection.getMetaData();
		List<DBTableModel> tables = getTables(dbmd);
		List<DBSequenceModel> sequences = getSequences(dbmd);
		return new DBDataModel(tables, sequences);
	}

	private List<DBTableModel> getTables(DatabaseMetaData dbmd) throws SQLException {
		List<DBTableModel> tables = readTables(dbmd);
		loadColumns(dbmd, tables);
		loadIndices(dbmd, tables);
		return new ArrayList<DBTableModel>(tables);
	}

	private List<DBTableModel> readTables(DatabaseMetaData dbmd) throws SQLException {
		List<DBTableModel> tables = new ArrayList<>();
		ResultSet rs = dbmd.getTables(null, null, null, new String[] { "TABLE", "VIEW" });
		while (rs.next()) {
			String tableName = rs.getString("TABLE_NAME");
			tables.add(new DBTableModel(tableName, new ArrayList<>(), new ArrayList<>()));
		}
		rs.close();
		return tables;
	}

	private void loadColumns(DatabaseMetaData dbmd, List<DBTableModel> tables) throws SQLException {
		for (DBTableModel table : tables) {
			ResultSet rs = dbmd.getColumns(null, null, table.getName(), "%");
			while (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME");
				String typeName = rs.getString("TYPE_NAME");
				int dataType = rs.getInt("DATA_TYPE");
				int columnSize = -1;
				int decimalDigits = -1;
				if ((dataType == Types.CHAR) || (dataType == Types.DECIMAL) || (dataType == Types.FLOAT)
						|| (dataType == Types.LONGVARCHAR) || (dataType == Types.NUMERIC)
						|| (dataType == Types.VARBINARY) || (dataType == Types.VARCHAR)) {
					columnSize = rs.getInt("COLUMN_SIZE");
				}
				if ((dataType == Types.DECIMAL) || (dataType == Types.NUMERIC)) {
					decimalDigits = rs.getInt("DECIMAL_DIGITS");
				}
				table.getColumns().add(new DBColumnModel(columnName, typeName, dataType, columnSize, decimalDigits));
			}
			rs.close();
		}
	}

	private void loadIndices(DatabaseMetaData dbmd, List<DBTableModel> tables) throws SQLException {
		for (DBTableModel table : tables) {
			ResultSet rs = dbmd.getIndexInfo(null, null, table.getName(), false, false);
			while (rs.next()) {
				boolean nonUniqueIndex = rs.getBoolean("NON_UNIQUE");
				if (nonUniqueIndex) {
					String indexName = rs.getString("INDEX_NAME");
					String columnName = rs.getString("COLUMN_NAME");
					DBIndexModel index = getIndexByName(indexName, table);
					DBColumnModel column = getColumnByName(columnName, table);
					index.getColumns().add(column);
				}
			}
			rs.close();
		}
	}

	private DBIndexModel getIndexByName(String name, DBTableModel table) {
		for (DBIndexModel index : table.getIndices()) {
			if (index.getName().equals(name)) {
				return index;
			}
		}
		DBIndexModel index = new DBIndexModel(name, new ArrayList<>());
		table.getIndices().add(index);
		return index;
	}

	private DBColumnModel getColumnByName(String name, DBTableModel table) {
		for (DBColumnModel column : table.getColumns()) {
			if (column.getName().equals(name)) {
				return column;
			}
		}
		throw new IllegalArgumentException("column '" + name + "' does not exist in table '" + table.getName() + "'.");
	}

	private List<DBSequenceModel> getSequences(DatabaseMetaData dbmd) throws SQLException {
		List<DBSequenceModel> sequences = new ArrayList<>();
		return sequences;
	}

}