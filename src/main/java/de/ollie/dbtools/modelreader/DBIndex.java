package de.ollie.dbtools.modelreader;

import java.util.List;

/**
 * An interface for database indices.
 *
 * @author ollie
 *
 */
public interface DBIndex {

	/**
	 * Returns a list with the columns which are members of the index.
	 * 
	 * @return A list with the columns which are members of the index.
	 */
	List<DBColumn> getColumns();

	/**
	 * Returns the name of the index.
	 *
	 * @return The name of the index.
	 */
	String getName();

}