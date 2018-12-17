package de.ollie.dbtools.modelreader;

/**
 * An interface for columns.
 *
 * @author O.Lieshoff
 *
 */
public interface DBColumnModel {

	/**
	 * Returns the size of the column.
	 * 
	 * @return The size of the column.
	 */
	int getColumnSize();

	/**
	 * Returns the decimal digits of the column.
	 * 
	 * @return The decimal digits of the column.
	 */
	int getDecimalDigits();

	/**
	 * Returns the name of the column.
	 * 
	 * @return The name of the column.
	 */
	String getName();

	/**
	 * Returns the type of the column.
	 * 
	 * @return The type of the column.
	 */
	int getType();

	/**
	 * Returns the type name of the column.
	 * 
	 * @return The type name of the column.
	 */
	String getTypeName();

}