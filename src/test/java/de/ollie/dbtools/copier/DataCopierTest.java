package de.ollie.dbtools.copier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.ollie.dbtools.utils.StatementBuilder;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests of class "DataCopier".
 *
 * @author Oliver.Lieshoff
 *
 */
@ExtendWith(MockitoExtension.class)
class DataCopierTest {

	private static final String COLUMN_NAME_1 = "Id";
	private static final String COLUMN_NAME_2 = "Name";
	private static final String COLUMN_NAME_3 = "Salary";

	private static final String TABLE_NAME_1 = "TestTable";

	private DataCopier unitUnderTest = new DataCopier(new StatementBuilder());

	@TempDir
	private File temp;

	private Connection connectionSource = null;
	private Connection connectionTarget = null;

	private String dbNameSource = "sourceDB";
	private String dbNameTarget = "targetDB";

	@BeforeAll
	static void setUpClass() {
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (Exception e) {
			System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
			e.printStackTrace();
			return;
		}
	}

	@BeforeEach
	void setUp() throws Exception {
		connectionSource = getConnection(dbNameSource);
		connectionTarget = getConnection(dbNameTarget);
	}

	private Connection getConnection(String dbName) throws Exception {
		return DriverManager.getConnection(
			"jdbc:hsqldb:file:" + temp.getAbsolutePath() + "/" + dbName + ";shutdown=true",
			"SA",
			""
		);
	}

	@AfterEach
	void tearDown() throws Exception {
		if (connectionSource != null) {
			connectionSource.close();
		}
		if (connectionTarget != null) {
			connectionTarget.close();
		}
	}

	@Test
	void copy_PassSourceAndTargetConnection_DatabaseContentCopied() throws Exception {
		// Prepare
		createDatabase(connectionSource, TABLE_NAME_1);
		createDatabase(connectionTarget, TABLE_NAME_1);
		insertData(connectionSource, 1, "eins", 1.11111F);
		insertData(connectionSource, 2, "zwei", 2.2F);
		// Run
		unitUnderTest.copy(connectionSource, connectionTarget, true, Arrays.asList("*"), null);
		// Check
		assertEquals(count(connectionTarget, TABLE_NAME_1), 2);
	}

	private void createDatabase(Connection connection, String tableName) throws Exception {
		Statement stmt = connection.createStatement();
		stmt.execute(
			"CREATE TABLE " +
			tableName +
			" (" +
			COLUMN_NAME_1 +
			" INTEGER, " +
			COLUMN_NAME_2 +
			" VARCHAR(100), " +
			COLUMN_NAME_3 +
			" NUMERIC(10,2))"
		);
		stmt.close();
	}

	private void insertData(Connection connection, Integer i, String s, Float f) throws Exception {
		Statement stmt = connection.createStatement();
		stmt.execute(
			"INSERT INTO " +
			TABLE_NAME_1 +
			" (" +
			COLUMN_NAME_1 +
			", " +
			COLUMN_NAME_2 +
			", " +
			COLUMN_NAME_3 +
			") VALUES (" +
			i +
			", " +
			(s == null ? "null" : "'" + s + "'") +
			", " +
			f +
			")"
		);
		stmt.close();
	}

	private int count(Connection connection, String tableName) throws Exception {
		int count = 0;
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
		if (rs.next()) {
			count = rs.getInt(1);
		}
		rs.close();
		stmt.close();
		return count;
	}

	@Test
	void copy_PassSourceAndTargetConnectionWithTableNameMapping_DatabaseContentCopied() throws Exception {
		// Prepare
		String pappnase = "PAPPNASE";
		createDatabase(connectionSource, TABLE_NAME_1);
		createDatabase(connectionTarget, pappnase);
		insertData(connectionSource, 1, "eins", 1.11111F);
		insertData(connectionSource, 2, "zwei", 2.2F);
		Map<String, String> tableNameMapping = new HashMap<>();
		tableNameMapping.put(TABLE_NAME_1.toUpperCase(), pappnase);
		// Run
		unitUnderTest.copy(connectionSource, connectionTarget, true, Arrays.asList("*"), tableNameMapping);
		// Check
		assertEquals(count(connectionTarget, pappnase), 2);
	}
}
