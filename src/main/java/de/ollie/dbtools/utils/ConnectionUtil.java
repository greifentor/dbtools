package de.ollie.dbtools.utils;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionUtil {

	private static final String POSTGRESQL_DRIVER_NAME_PART = "postgre";

	public static boolean isPostgreConnection(Connection connection) throws SQLException {
		return connection.getMetaData().getDriverName().toLowerCase().contains(POSTGRESQL_DRIVER_NAME_PART);
	}
}
