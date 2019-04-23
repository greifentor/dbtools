package de.ollie.dbtools.modelreader;

/**
 * An interface for model readers.
 *
 * @author Oliver.Lieshoff
 *
 */
public interface ModelReader {

	/**
	 * Read the data model which is representing the database linked to the
	 * passed connection.
	 *
	 * @return The data model for the database which is linked by the passed
	 *         connection.
	 * @throws Exception
	 *             If an error occurs while accessing the database.
	 */
	DBDataScheme readModel() throws Exception;

}