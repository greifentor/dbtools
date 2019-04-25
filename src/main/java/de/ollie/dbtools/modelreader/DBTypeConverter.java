package de.ollie.dbtools.modelreader;

import java.sql.Types;

/**
 * A converter for Types to DBType and vice versa.
 *
 * @author Oliver.Lieshoff
 *
 */
public class DBTypeConverter {

	/**
	 * Converts the passed Types value to an DBType enum.
	 * 
	 * @param dataType
	 *            A Types value.
	 * @return A DBType for the passed Types constant.
	 * @throws IllegalArgumentException
	 *             Passing an unknown value.
	 */
	public DBType convert(int dataType) {
		if (dataType == Types.BIGINT) {
			return DBType.BIGINT;
		} else if (dataType == Types.CHAR) {
			return DBType.CHAR;
		} else if (dataType == Types.DECIMAL) {
			return DBType.DECIMAL;
		} else if (dataType == Types.INTEGER) {
			return DBType.INTEGER;
		} else if (dataType == Types.NUMERIC) {
			return DBType.NUMERIC;
		} else if (dataType == Types.VARCHAR) {
			return DBType.VARCHAR;
		}
		throw new IllegalArgumentException(
				"there is no mapping for data type value: " + dataType);
	}

}