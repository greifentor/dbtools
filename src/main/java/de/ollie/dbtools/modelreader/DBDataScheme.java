package de.ollie.dbtools.modelreader;

import java.util.List;
import java.util.Optional;

public interface DBDataScheme {
	void addForeignKey(DBForeignKey<?>... dbForeignKeys);

	void addTables(DBTable... dbTables);

	Optional<DBTable> getTableByName(String name);

	List<DBTable> getTables();

	List<DBSequence> getSequences();

	List<DBForeignKey<?>> getForeignKeys();
}
