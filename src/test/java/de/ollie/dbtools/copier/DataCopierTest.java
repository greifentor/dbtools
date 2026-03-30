package de.ollie.dbtools.copier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.ollie.dbtools.utils.StatementBuilder;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

	@Mock
	private ForeignKeyRemover foreignKeyRemover;

	@Mock
	private ForeignKeyRestorer foreignKeyRestorer;

	@Mock
	private SequenceUpdater sequenceUpater;

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
		unitUnderTest.foreignKeyRemover = foreignKeyRemover;
		unitUnderTest.foreignKeyRestorer = foreignKeyRestorer;
		unitUnderTest.sequenceUpater = sequenceUpater;
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

	@Nested
	class copy_Connection_Connection_boolean_ListString_MapStringString_String {

		@Test
		void passSourceAndTargetConnection_DatabaseContentCopied() throws Exception {
			// Prepare
			createDatabase(connectionSource, TABLE_NAME_1);
			createDatabase(connectionTarget, TABLE_NAME_1);
			insertData(connectionSource, 1, "eins", 1.11111F);
			insertData(connectionSource, 2, "zwei", 2.2F);
			// Run
			unitUnderTest.copy(connectionSource, connectionTarget, true, Arrays.asList("*"), List.of(), null, null);
			// Check
			assertEquals(2, count(connectionTarget, TABLE_NAME_1));
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
		void passSourceAndTargetConnectionWithTableNameMapping_DatabaseContentCopied() throws Exception {
			// Prepare
			String pappnase = "PAPPNASE";
			createDatabase(connectionSource, TABLE_NAME_1);
			createDatabase(connectionTarget, pappnase);
			insertData(connectionSource, 1, "eins", 1.11111F);
			insertData(connectionSource, 2, "zwei", 2.2F);
			Map<String, String> tableNameMapping = new HashMap<>();
			tableNameMapping.put(TABLE_NAME_1.toUpperCase(), pappnase);
			// Run
			unitUnderTest.copy(
				connectionSource,
				connectionTarget,
				true,
				Arrays.asList("*"),
				List.of(),
				tableNameMapping,
				null
			);
			// Check
			assertEquals(2, count(connectionTarget, pappnase));
		}

		@Test
		void callsForeignKeyRemoverCorrectly() throws Exception {
			// Prepare
			String pappnase = "PAPPNASE";
			createDatabase(connectionSource, TABLE_NAME_1);
			createDatabase(connectionTarget, pappnase);
			insertData(connectionSource, 1, "eins", 1.11111F);
			insertData(connectionSource, 2, "zwei", 2.2F);
			Map<String, String> tableNameMapping = new HashMap<>();
			tableNameMapping.put(TABLE_NAME_1.toUpperCase(), pappnase);
			// Run
			unitUnderTest.copy(
				connectionSource,
				connectionTarget,
				true,
				Arrays.asList("*"),
				List.of(),
				tableNameMapping,
				null
			);
			// Check
			verify(foreignKeyRemover, times(1)).remove(eq(List.of()), eq(connectionTarget), any(StatementBuilder.class));
		}

		@Test
		void callsForeignKeyRestorerCorrectly() throws Exception {
			// Prepare
			String pappnase = "PAPPNASE";
			createDatabase(connectionSource, TABLE_NAME_1);
			createDatabase(connectionTarget, pappnase);
			insertData(connectionSource, 1, "eins", 1.11111F);
			insertData(connectionSource, 2, "zwei", 2.2F);
			Map<String, String> tableNameMapping = new HashMap<>();
			tableNameMapping.put(TABLE_NAME_1.toUpperCase(), pappnase);
			// Run
			unitUnderTest.copy(
				connectionSource,
				connectionTarget,
				true,
				Arrays.asList("*"),
				List.of(),
				tableNameMapping,
				null
			);
			// Check
			verify(foreignKeyRestorer, times(1)).restore(eq(List.of()), eq(connectionTarget), any(StatementBuilder.class));
		}
	}
}
