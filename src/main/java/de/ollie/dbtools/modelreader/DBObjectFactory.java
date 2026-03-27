package de.ollie.dbtools.modelreader;

import java.util.List;

public interface DBObjectFactory {
	DBColumn createColumn(String columnName, String typeName, DBType dataType, int columnSize, int decimalDigits);

	DBDataScheme createDataScheme(List<DBTable> tables, List<DBSequence> sequences, List<DBForeignKey> foreignKeys);

	DBIndex createIndex(String indexName, List<DBColumn> columns);

	DBTable createTable(String tableName, List<DBColumn> columns, List<DBIndex> indices);
}
