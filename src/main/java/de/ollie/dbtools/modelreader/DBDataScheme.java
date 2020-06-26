package de.ollie.dbtools.modelreader;

import java.util.List;
import java.util.Optional;

/**
 * An interface for data schemes.
 * 
 * @author Oliver.Lieshoff
 *
 */
public interface DBDataScheme {

	void addTables(DBTable... dbTables);

	Optional<DBTable> getTableByName(String name);

	/**
	 * Returns the tables of the data scheme.
	 * 
	 * @return The tables of the data scheme.
	 */
	List<DBTable> getTables();

	/**
	 * Returns the sequences of the data scheme.
	 * 
	 * @return The sequences of the data scheme.
	 */
	List<DBSequence> getSequences();

}