package de.ollie.dbtools.counter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

import de.ollie.dbtools.modelreader.DBDataScheme;
import de.ollie.dbtools.modelreader.DBTable;
import de.ollie.dbtools.modelreader.DBTypeConverter;
import de.ollie.dbtools.modelreader.DefaultDBObjectFactory;
import de.ollie.dbtools.modelreader.jdbc.JDBCModelReader;

/**
 * A class which counts the records of all tables in a database.
 *
 * @author Oliver.Lieshoff
 *
 */
public class Counter {

	/**
	 * Counts the records of all tables in a database and prints the result to the console.
	 * 
	 * @param connection The JDBC connection whose data are to read.
	 * @param schemeName The name of the scheme whose tables are to respect.
	 */
	public static void countAllRecords(Connection connection, String schemeName) throws Exception {
		DBDataScheme scheme = new JDBCModelReader(new DefaultDBObjectFactory(), new DBTypeConverter(), connection,
				schemeName).readModel();
		int maxlen = 0;
		for (DBTable table : scheme.getTables()) {
			maxlen = Math.max(maxlen, table.getName().length());
		}
		for (DBTable table : scheme.getTables()) {
			System.out.println(LocalDateTime.now() + " - start reading results for table: " + table.getName());
			String sql = "SELECT COUNT(*) FROM " + table.getName();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				System.out.println(String.format("%-" + maxlen + "s: %10d", table.getName(), rs.getLong(1)));
			}
			rs.close();
			stmt.close();
			System.out.println(table);
		}
	}

	public static void main(String[] args) throws Exception {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (Exception e) {
			System.err.println("ERROR: failed to load Oracle JDBC driver.");
			e.printStackTrace();
			return;
		}
		Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@HOST:1521:lalala", "user", "password");
		countAllRecords(connection, "KITA");
	}

}