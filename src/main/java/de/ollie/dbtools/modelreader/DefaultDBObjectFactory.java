package de.ollie.dbtools.modelreader;

import java.util.List;

import de.ollie.dbtools.modelreader.models.DBColumnModel;
import de.ollie.dbtools.modelreader.models.DBDataSchemeModel;
import de.ollie.dbtools.modelreader.models.DBIndexModel;
import de.ollie.dbtools.modelreader.models.DBTableModel;

/**
 * A default implementation of the DB object factory.
 *
 * @author ollie
 *
 */
public class DefaultDBObjectFactory implements DBObjectFactory {

	@Override
	public DBColumn createColumn(String columnName, String typeName,
			DBType dataType, int columnSize, int decimalDigits) {
		return new DBColumnModel(columnName, typeName, dataType, columnSize,
				decimalDigits);
	}

	@Override
	public DBDataScheme createDataScheme(List<DBTable> tables,
			List<DBSequence> sequences) {
		return new DBDataSchemeModel(tables, sequences);
	}

	@Override
	public DBIndex createIndex(String indexName, List<DBColumn> columns) {
		return new DBIndexModel(indexName, columns);
	}

	@Override
	public DBTable createTable(String tableName, List<DBColumn> columns,
			List<DBIndex> indices) {
		return new DBTableModel(tableName, columns, indices);
	}

}