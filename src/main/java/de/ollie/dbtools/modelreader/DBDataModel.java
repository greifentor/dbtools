package de.ollie.dbtools.modelreader;

import java.util.List;

/**
 * An interface which represents the data model.
 *
 * @author O.Lieshoff
 *
 */
public interface DBDataModel {

	/**
	 * Returns the tables of the data model.
	 *
	 * @return The tables of the data model.
	 */
	List<DBTableModel> getTables();

}