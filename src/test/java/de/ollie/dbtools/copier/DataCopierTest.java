package de.ollie.dbtools.copier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.ollie.dbtools.modelreader.DBForeignKey;
import de.ollie.dbtools.modelreader.jdbc.JDBCForeignKeyReader;
import de.ollie.dbtools.utils.StatementBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
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

	private static final int RECORDS_PER_DOT = 100;

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
			unitUnderTest.copy(
				connectionSource,
				connectionTarget,
				true,
				Arrays.asList("*"),
				List.of(),
				null,
				null,
				RECORDS_PER_DOT
			);
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
				null,
				RECORDS_PER_DOT
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
				null,
				RECORDS_PER_DOT
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
				null,
				RECORDS_PER_DOT
			);
			// Check
			verify(foreignKeyRestorer, times(1)).restore(eq(List.of()), eq(connectionTarget), any(StatementBuilder.class));
		}
	}

	@Nested
	class copy_DetectedForeignKeys {

		@Mock
		private JDBCForeignKeyReader foreignKeyReader;

		@Mock
		private DBForeignKey<?> detectedForeignKey;

		@BeforeEach
		void setUp() {
			unitUnderTest.foreignKeyReader = foreignKeyReader;
		}

		@AfterEach
		void tearDown() {
			unitUnderTest.foreignKeyReader = new JDBCForeignKeyReader();
		}

		@Test
		void printsAllDataOfTheDetectedForeignKeysBeforeTheRemoval() throws Exception {
			// Prepare
			when(detectedForeignKey.getName()).thenReturn("fk-name");
			when(detectedForeignKey.getTableName()).thenReturn("table-name");
			when(detectedForeignKey.getKeyColumnNames()).thenReturn(List.of("key-column-name"));
			when(detectedForeignKey.getReferencedTableName()).thenReturn("referenced-table-name");
			when(detectedForeignKey.getReferencedKeyColumnNames()).thenReturn(List.of("referenced-key-column-name"));
			when(foreignKeyReader.getForeignKeys(any(DatabaseMetaData.class), eq(null), anyList()))
				.thenReturn(List.of(detectedForeignKey), List.of());
			ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
			PrintStream originalOut = System.out;
			System.setOut(new PrintStream(consoleOutput));
			// Run
			try {
				unitUnderTest.copy(
					connectionSource,
					connectionTarget,
					false,
					Arrays.asList("*"),
					List.of(),
					null,
					null,
					RECORDS_PER_DOT
				);
			} finally {
				System.setOut(originalOut);
			}
			// Check
			assertTrue(
				consoleOutput
					.toString()
					.contains(
						"- fk-name: table=table-name, key columns=[key-column-name], referenced table=referenced-table-name," +
						" referenced key columns=[referenced-key-column-name]"
					)
			);
		}
	}

	@Nested
	class copy_ErrorWhileCopyingTheData {

		@Mock
		private JDBCForeignKeyReader foreignKeyReader;

		@Mock
		private DBForeignKey<?> removedForeignKey;

		@BeforeEach
		void setUp() {
			unitUnderTest.foreignKeyReader = foreignKeyReader;
		}

		@AfterEach
		void tearDown() {
			unitUnderTest.foreignKeyReader = new JDBCForeignKeyReader();
		}

		private void createTable(Connection connection, int nameColumnSize) throws Exception {
			try (Statement stmt = connection.createStatement()) {
				stmt.execute(
					"CREATE TABLE " +
					TABLE_NAME_1 +
					" (" +
					COLUMN_NAME_1 +
					" INTEGER, " +
					COLUMN_NAME_2 +
					" VARCHAR(" +
					nameColumnSize +
					"), " +
					COLUMN_NAME_3 +
					" NUMERIC(10,2))"
				);
			}
		}

		private void insertData(Connection connection, int id, String name) throws Exception {
			try (Statement stmt = connection.createStatement()) {
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
					id +
					", '" +
					name +
					"', 1.0)"
				);
			}
		}

		private int count(Connection connection) throws Exception {
			try (Statement stmt = connection.createStatement()) {
				try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + TABLE_NAME_1)) {
					return rs.next() ? rs.getInt(1) : 0;
				}
			}
		}

		private void prepareACopyWhichFailsAtTheInsertOfTheTargetDatabase() throws Exception {
			createTable(connectionSource, 100);
			createTable(connectionTarget, 3);
			insertData(connectionSource, 1, "a-name-which-is-too-long-for-the-target");
		}

		private void copy() throws Exception {
			unitUnderTest.copy(
				connectionSource,
				connectionTarget,
				true,
				Arrays.asList("*"),
				List.of(),
				null,
				null,
				RECORDS_PER_DOT
			);
		}

		@Test
		void throwsTheExceptionOfTheFailedCopy() throws Exception {
			// Prepare
			prepareACopyWhichFailsAtTheInsertOfTheTargetDatabase();
			// Check
			assertThrows(SQLException.class, () -> copy());
		}

		@Test
		void restoresTheRemovedForeignKeys() throws Exception {
			// Prepare
			prepareACopyWhichFailsAtTheInsertOfTheTargetDatabase();
			when(foreignKeyReader.getForeignKeys(any(DatabaseMetaData.class), eq(null), anyList()))
				.thenReturn(List.of(removedForeignKey), List.of(), List.of());
			// Run
			assertThrows(SQLException.class, () -> copy());
			// Check
			verify(foreignKeyRestorer, times(1))
				.restore(eq(List.of(removedForeignKey)), eq(connectionTarget), any(StatementBuilder.class));
		}

		@Test
		void rollsBackTheDataWhichIsAlreadyDeletedInTheTargetDatabase() throws Exception {
			// Prepare
			prepareACopyWhichFailsAtTheInsertOfTheTargetDatabase();
			insertData(connectionTarget, 2, "abc");
			// Run
			assertThrows(SQLException.class, () -> copy());
			// Check
			assertEquals(1, count(connectionTarget));
		}
	}

	@Nested
	class copy_ForeignKeysAreNotRemovable {

		@Mock
		private JDBCForeignKeyReader foreignKeyReader;

		@Mock
		private DBForeignKey<?> removedForeignKey;

		@Mock
		private DBForeignKey<?> stillPresentForeignKey;

		private InputStream originalIn;

		@BeforeEach
		void setUp() throws Exception {
			unitUnderTest.foreignKeyReader = foreignKeyReader;
			originalIn = System.in;
			System.setIn(new ByteArrayInputStream("\n".getBytes()));
		}

		@AfterEach
		void tearDown() {
			unitUnderTest.foreignKeyReader = new JDBCForeignKeyReader();
			System.setIn(originalIn);
		}

		private void prepareForeignKeysReadInThreeSteps(
			List<DBForeignKey<?>> beforeRemoval,
			List<DBForeignKey<?>> afterFirstRemoval,
			List<DBForeignKey<?>> afterSecondRemoval
		) throws Exception {
			when(foreignKeyReader.getForeignKeys(any(DatabaseMetaData.class), eq(null), anyList()))
				.thenReturn(beforeRemoval, afterFirstRemoval, afterSecondRemoval);
		}

		private void copy() throws Exception {
			unitUnderTest.copy(
				connectionSource,
				connectionTarget,
				false,
				Arrays.asList("*"),
				List.of(),
				null,
				null,
				RECORDS_PER_DOT
			);
		}

		@Test
		void removesTheStillPresentForeignKeysASecondTime() throws Exception {
			// Prepare
			prepareForeignKeysReadInThreeSteps(List.of(stillPresentForeignKey), List.of(stillPresentForeignKey), List.of());
			// Run
			copy();
			// Check
			verify(foreignKeyRemover, times(2))
				.remove(eq(List.of(stillPresentForeignKey)), eq(connectionTarget), any(StatementBuilder.class));
		}

		@Test
		void throwsAnExceptionIfTheForeignKeysAreStillPresentAfterTheSecondRemoval() throws Exception {
			// Prepare
			when(stillPresentForeignKey.getName()).thenReturn("still-present-foreign-key");
			when(stillPresentForeignKey.getTableName()).thenReturn("still-present-table");
			prepareForeignKeysReadInThreeSteps(
				List.of(stillPresentForeignKey),
				List.of(stillPresentForeignKey),
				List.of(stillPresentForeignKey)
			);
			// Check
			assertThrows(IllegalStateException.class, () -> copy());
		}

		@Test
		void rollsBackTheRemovalOfTheRemovedForeignKeysIfTheSecondRemovalFails() throws Exception {
			// Prepare
			when(removedForeignKey.getName()).thenReturn("removed-foreign-key");
			when(stillPresentForeignKey.getName()).thenReturn("still-present-foreign-key");
			when(stillPresentForeignKey.getTableName()).thenReturn("still-present-table");
			prepareForeignKeysReadInThreeSteps(
				List.of(removedForeignKey, stillPresentForeignKey),
				List.of(stillPresentForeignKey),
				List.of(stillPresentForeignKey)
			);
			// Run
			assertThrows(IllegalStateException.class, () -> copy());
			// Check
			verify(foreignKeyRestorer, times(1))
				.restore(eq(List.of(removedForeignKey)), eq(connectionTarget), any(StatementBuilder.class));
		}
	}
}
