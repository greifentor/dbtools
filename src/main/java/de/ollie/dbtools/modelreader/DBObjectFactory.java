package de.ollie.dbtools.modelreader;

import java.util.List;

/**
 * An interface for DB object factories.
 *
 * @author ollie
 *
 */
public interface DBObjectFactory {

	/**
	 * Creates a database column object.
	 *
	 * @param columnName
	 *            The name of the column.
	 * @param typeName
	 *            A human readable name of the columns type.
	 * @param dataType
	 *            The "Types" value for the column.
	 * @param columnSize
	 *            A column size value. Set "0" if no size is necessary for the
	 *            type.
	 * @param decimalDigits
	 *            The count of the decimal digits.
	 */
	DBColumn createColumn(String columnName, String typeName, int dataType,
			int columnSize, int decimalDigits);

	/**
	 * Creates a database scheme object.
	 *
	 * @param tables
	 *            The tables of the data scheme.
	 * @param sequences
	 *            The sequences of the data scheme.
	 */
	DBDataScheme createDataScheme(List<DBTable> tables,
			List<DBSequence> sequences);

	/**
	 * Creates a database index object.
	 *
	 * @param indexName
	 *            The name of the index.
	 * @param columns
	 *            The columns which are members of the index.
	 */
	DBIndex createIndex(String indexName, List<DBColumn> columns);

	/**
	 * Creates a database table object.
	 *
	 * @param tableName
	 *            The name of the table.
	 * @param columns
	 *            The columns which are members of the table.
	 * @param index
	 *            The indices which are assigned to the table.
	 */
	DBTable createTable(String tableName, List<DBColumn> columns,
			List<DBIndex> indices);

}