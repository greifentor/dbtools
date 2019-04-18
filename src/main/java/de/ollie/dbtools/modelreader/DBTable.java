package de.ollie.dbtools.modelreader;

import java.util.List;

/**
 * An interface for the tables of a data scheme.
 *
 * @author Oliver.Lieshoff
 *
 */
public interface DBTable {

	/**
	 * Returns a list with the columns of the table.
	 *
	 * @return A list with the columns of the table.
	 */
	List<DBColumn> getColumns();

	/**
	 * Returns a list with the indices of the table.
	 *
	 * @return A list with the indices of the table.
	 */
	List<DBIndex> getIndices();

	/**
	 * Returns the name of the table.
	 * 
	 * @return The name of the table.
	 */
	String getName();

}