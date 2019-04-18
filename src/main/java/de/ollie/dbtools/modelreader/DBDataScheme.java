package de.ollie.dbtools.modelreader;

import java.util.List;

/**
 * An interface for data schemes.
 * 
 * @author Oliver.Lieshoff
 *
 */
public interface DBDataScheme {

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