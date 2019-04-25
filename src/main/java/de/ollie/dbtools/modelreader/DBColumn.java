package de.ollie.dbtools.modelreader;

/**
 * An interface for database columns.
 *
 * @author ollie
 *
 */
public interface DBColumn {

	/**
	 * Returns the size of the column or "0" if no size is defined for the
	 * column.
	 * 
	 * @return The size of the column or "0" if no size is defined for the
	 *         column.
	 */
	int getColumnSize();

	/**
	 * Returns the number of the decimal digits if column is a NUMERIC.
	 * 
	 * @return The number of the decimal digits if column is a NUMERIC.
	 */
	int getDecimalDigits();

	/**
	 * Returns the name of the column.
	 *
	 * @return The name of the column.
	 */
	String getName();

	/**
	 * Returns the "Types" type of the field.
	 *
	 * @return The "Types" type of the field.
	 */
	DBType getType();

	/**
	 * Returns a human readable name of the fields type.
	 *
	 * @return A human readable name of the fields type.
	 */
	String getTypeName();

}