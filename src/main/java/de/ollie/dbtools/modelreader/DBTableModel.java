package de.ollie.dbtools.modelreader;

import java.util.List;

/**
 * An interface for tables.
 *
 * @author O.Lieshoff
 *
 */
public interface DBTableModel {

	/**
	 * Returns the name of the table model
	 *
	 * @return The name of the table model.
	 */
	String getName();

	/**
	 * Returns the columns of the table model.
	 *
	 * @return The columns of the table model.
	 */
	List<DBColumnModel> getColumns();

}