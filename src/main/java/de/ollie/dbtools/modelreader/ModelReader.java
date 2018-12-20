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

import de.ollie.dbtools.modelreader.dto.DBColumnModelDTO;
import de.ollie.dbtools.modelreader.dto.DBDataModelDTO;
import de.ollie.dbtools.modelreader.dto.DBSequenceModelDTO;
import de.ollie.dbtools.modelreader.dto.DBTableModelDTO;

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
	public DBDataModelDTO readModel(Connection connection) throws SQLException {
		DatabaseMetaData dbmd = connection.getMetaData();
		List<DBTableModelDTO> tables = getTables(dbmd);
		List<DBSequenceModelDTO> sequences = getSequences(dbmd);
		return new DBDataModelDTO(tables, sequences);
	}

	private List<DBTableModelDTO> getTables(DatabaseMetaData dbmd) throws SQLException {
		List<DBTableModelDTO> tables = readTables(dbmd);
		loadColumns(dbmd, tables);
		return new ArrayList<DBTableModelDTO>(tables);
	}

	private List<DBTableModelDTO> readTables(DatabaseMetaData dbmd) throws SQLException {
		List<DBTableModelDTO> tables = new ArrayList<>();
		ResultSet rs = dbmd.getTables(null, null, null, new String[] { "TABLE", "VIEW" });
		while (rs.next()) {
			String tableName = rs.getString("TABLE_NAME");
			tables.add(new DBTableModelDTO(tableName, new ArrayList<DBColumnModelDTO>()));
		}
		rs.close();
		return tables;
	}

	private void loadColumns(DatabaseMetaData dbmd, List<DBTableModelDTO> tables) throws SQLException {
		for (DBTableModelDTO table : tables) {
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
				table.getColumns().add(new DBColumnModelDTO(columnName, typeName, dataType, columnSize, decimalDigits));
			}
			rs.close();
		}
	}

	private List<DBSequenceModelDTO> getSequences(DatabaseMetaData dbmd) throws SQLException {
		List<DBSequenceModelDTO> sequences = new ArrayList<>();
		return sequences;
	}

}