package de.ollie.dbtools.copier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import de.ollie.dbtools.utils.StatementBuilder;

/**
 * Unit tests of class "DataCopier".
 *
 * @author Oliver.Lieshoff
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DataCopierTest {

	private static final String COLUMN_NAME_1 = "Id";
	private static final String COLUMN_NAME_2 = "Name";
	private static final String COLUMN_NAME_3 = "Salary";

	private static final String TABLE_NAME_1 = "TestTable";

	private DataCopier unitUnderTest = new DataCopier(new StatementBuilder());

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private Connection connectionSource = null;
	private Connection connectionTarget = null;

	private String dbNameSource = "sourceDB";
	private String dbNameTarget = "targetDB";

	@BeforeClass
	public static void setUpClass() {
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (Exception e) {
			System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
			e.printStackTrace();
			return;
		}
	}

	@Before
	public void setUp() throws Exception {
		this.connectionSource = getConnection(this.dbNameSource);
		this.connectionTarget = getConnection(this.dbNameTarget);
	}

	private Connection getConnection(String dbName) throws Exception {
		return DriverManager.getConnection(
				"jdbc:hsqldb:file:" + temp.getRoot().getAbsolutePath() + "/" + dbName + ";shutdown=true", "SA", "");
	}

	@After
	public void tearDown() throws Exception {
		if (this.connectionSource != null) {
			this.connectionSource.close();
		}
		if (this.connectionTarget != null) {
			this.connectionTarget.close();
		}
	}

	@Test
	public void copy_PassSourceAndTargetConnection_DatabaseContentCopied() throws Exception {
		// Prepare
		createDatabase(this.connectionSource, TABLE_NAME_1);
		createDatabase(this.connectionTarget, TABLE_NAME_1);
		insertData(this.connectionSource, 1, "eins", 1.11111F);
		insertData(this.connectionSource, 2, "zwei", 2.2F);
		// Run
		this.unitUnderTest.copy(this.connectionSource, this.connectionTarget, true, Arrays.asList("*"), null);
		// Check
		assertThat(count(this.connectionTarget, TABLE_NAME_1), equalTo(2));
	}

	private void createDatabase(Connection connection, String tableName) throws Exception {
		Statement stmt = connection.createStatement();
		stmt.execute("CREATE TABLE " + tableName + " (" + COLUMN_NAME_1 + " INTEGER, " + COLUMN_NAME_2
				+ " VARCHAR(100), " + COLUMN_NAME_3 + " NUMERIC(10,2))");
		stmt.close();
	}

	private void insertData(Connection connection, Integer i, String s, Float f) throws Exception {
		Statement stmt = connection.createStatement();
		stmt.execute("INSERT INTO " + TABLE_NAME_1 + " (" + COLUMN_NAME_1 + ", " + COLUMN_NAME_2 + ", " + COLUMN_NAME_3
				+ ") VALUES (" + i + ", " + (s == null ? "null" : "'" + s + "'") + ", " + f + ")");
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
	public void copy_PassSourceAndTargetConnectionWithTableNameMapping_DatabaseContentCopied() throws Exception {
		// Prepare
		String pappnase = "PAPPNASE";
		createDatabase(this.connectionSource, TABLE_NAME_1);
		createDatabase(this.connectionTarget, pappnase);
		insertData(this.connectionSource, 1, "eins", 1.11111F);
		insertData(this.connectionSource, 2, "zwei", 2.2F);
		Map<String, String> tableNameMapping = new HashMap<>();
		tableNameMapping.put(TABLE_NAME_1.toUpperCase(), pappnase);
		// Run
		this.unitUnderTest.copy(this.connectionSource, this.connectionTarget, true, Arrays.asList("*"),
				tableNameMapping);
		// Check
		assertThat(count(this.connectionTarget, pappnase), equalTo(2));
	}

}