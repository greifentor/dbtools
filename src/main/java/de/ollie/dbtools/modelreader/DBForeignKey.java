package de.ollie.dbtools.modelreader;

import java.util.List;

public interface DBForeignKey<T> {
	T addColumnNames(String keyColumnName, String referencedKeyColumnName);

	List<String> getKeyColumnNames();

	String getName();

	String getReferencedTableName();

	List<String> getReferencedKeyColumnNames();

	String getTableName();
}
