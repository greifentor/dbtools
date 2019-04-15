package de.ollie.dbtools.modelreader;

import java.util.List;

import de.ollie.dbtools.modelreader.models.DBColumnModel;
import de.ollie.dbtools.modelreader.models.DBIndexModel;

/**
 * A default implementation of the DB object factory.
 *
 * @author ollie
 *
 */
public class DefaultDBObjectFactory implements DBObjectFactory {

	@Override
	public DBColumn createColumn(String columnName, String typeName, int dataType, int columnSize, int decimalDigits) {
		return new DBColumnModel(columnName, typeName, dataType, columnSize, decimalDigits);
	}

	@Override
	public DBIndex createIndex(String indexName, List<DBColumn> columns) {
		return new DBIndexModel(indexName, columns);
	}

}