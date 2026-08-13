package de.ollie.dbtools.copier;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.ollie.dbtools.modelreader.DBColumn;
import de.ollie.dbtools.modelreader.DBDataScheme;
import de.ollie.dbtools.modelreader.DBTable;
import de.ollie.dbtools.modelreader.DBType;
import de.ollie.dbtools.modelreader.models.DBColumnModel;
import de.ollie.dbtools.modelreader.models.DBDataSchemeModel;
import de.ollie.dbtools.modelreader.models.DBTableModel;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SequenceUpdaterTest {

	@InjectMocks
	private SequenceUpdater unitUnderTest;

	@Nested
	class update_String_Connection_DBDataScheme {

		private static final String ANOTHER_ID_COLUMN_NAME = "another_id";
		private static final String ID_COLUMN_NAME = "id";
		private static final String POSTGRESQL_DRIVER_NAME = "PostgreSQL JDBC Driver";
		private static final String SCHEME_NAME = "scheme-name";
		private static final String TABLE_NAME = "table-name";
		private static final String SEQUENCE_NAME = TABLE_NAME + "_id_seq";

		@Mock
		private Connection connection;

		@Mock
		private ResultSet countResultSet;

		@Mock
		private DatabaseMetaData databaseMetaData;

		@Mock
		private ResultSet sequenceResultSet;

		@Mock
		private Statement statement;

		private void prepareAPostgreConnectionWithASequence() throws Exception {
			when(connection.getMetaData()).thenReturn(databaseMetaData);
			when(databaseMetaData.getDriverName()).thenReturn(POSTGRESQL_DRIVER_NAME);
			when(connection.createStatement()).thenReturn(statement);
			when(statement.executeQuery(contains("pg_sequence"))).thenReturn(sequenceResultSet);
			when(sequenceResultSet.next()).thenReturn(true, false);
			when(sequenceResultSet.getString("sequence_name")).thenReturn(SEQUENCE_NAME);
		}

		private void prepareTheReadingOfTheTableContent() throws Exception {
			when(statement.executeQuery(contains("FROM " + TABLE_NAME))).thenReturn(countResultSet);
		}

		private DBDataScheme createModel(DBColumn... columns) {
			DBTable table = new DBTableModel(TABLE_NAME, new ArrayList<>(List.of(columns)), new ArrayList<>());
			return new DBDataSchemeModel(new ArrayList<>(List.of(table)), new ArrayList<>(), new ArrayList<>());
		}

		private DBColumn createColumn(String name, boolean primaryKey) {
			return new DBColumnModel(name, "INTEGER", DBType.INTEGER, -1, -1, primaryKey);
		}

		private String updateAndReadConsoleOutput(DBDataScheme model) throws Exception {
			ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
			PrintStream originalOut = System.out;
			System.setOut(new PrintStream(consoleOutput));
			try {
				unitUnderTest.update(SCHEME_NAME, connection, model);
			} finally {
				System.setOut(originalOut);
			}
			return consoleOutput.toString();
		}

		@Test
		void readsTheMaximumOfThePrimaryKeyColumnForATableWithASinglePrimaryKeyColumn() throws Exception {
			// Prepare
			prepareAPostgreConnectionWithASequence();
			prepareTheReadingOfTheTableContent();
			DBDataScheme model = createModel(createColumn(ID_COLUMN_NAME, true), createColumn("name", false));
			// Run
			unitUnderTest.update(SCHEME_NAME, connection, model);
			// Check
			verify(statement, times(1)).executeQuery("SELECT max(" + ID_COLUMN_NAME + ") FROM " + TABLE_NAME);
		}

		@Test
		void restartsTheSequenceWithTheMaximumOfThePrimaryKeyColumnIncrementedByOne() throws Exception {
			// Prepare
			prepareAPostgreConnectionWithASequence();
			prepareTheReadingOfTheTableContent();
			when(countResultSet.next()).thenReturn(true);
			when(countResultSet.getInt(1)).thenReturn(4711);
			DBDataScheme model = createModel(createColumn(ID_COLUMN_NAME, true));
			// Run
			unitUnderTest.update(SCHEME_NAME, connection, model);
			// Check
			verify(statement, times(1)).execute("SELECT setval('" + SEQUENCE_NAME + "', 4712)");
		}

		@Test
		void doesNotUpdateTheSequenceForATableWithACombinedPrimaryKey() throws Exception {
			// Prepare
			prepareAPostgreConnectionWithASequence();
			DBDataScheme model = createModel(createColumn(ID_COLUMN_NAME, true), createColumn(ANOTHER_ID_COLUMN_NAME, true));
			// Run
			unitUnderTest.update(SCHEME_NAME, connection, model);
			// Check
			verify(statement, never()).execute(anyString());
		}

		@Test
		void printsTheReasonForATableWithACombinedPrimaryKey() throws Exception {
			// Prepare
			prepareAPostgreConnectionWithASequence();
			DBDataScheme model = createModel(createColumn(ID_COLUMN_NAME, true), createColumn(ANOTHER_ID_COLUMN_NAME, true));
			// Run
			String returned = updateAndReadConsoleOutput(model);
			// Check
			assertTrue(
				returned.contains(
					"sequence not updated: " +
					SEQUENCE_NAME +
					" - table '" +
					TABLE_NAME +
					"' has a combined primary key of 2 columns"
				)
			);
		}

		@Test
		void doesNotUpdateTheSequenceForATableWithoutAPrimaryKey() throws Exception {
			// Prepare
			prepareAPostgreConnectionWithASequence();
			DBDataScheme model = createModel(createColumn(ID_COLUMN_NAME, false));
			// Run
			unitUnderTest.update(SCHEME_NAME, connection, model);
			// Check
			verify(statement, never()).execute(anyString());
		}

		@Test
		void printsTheReasonForATableWithoutAPrimaryKey() throws Exception {
			// Prepare
			prepareAPostgreConnectionWithASequence();
			DBDataScheme model = createModel(createColumn(ID_COLUMN_NAME, false));
			// Run
			String returned = updateAndReadConsoleOutput(model);
			// Check
			assertTrue(returned.contains("table '" + TABLE_NAME + "' has no primary key column"));
		}

		@Test
		void doesNotUpdateTheSequenceForATableWhichIsNotPartOfTheModel() throws Exception {
			// Prepare
			prepareAPostgreConnectionWithASequence();
			DBDataScheme model = new DBDataSchemeModel(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
			// Run
			unitUnderTest.update(SCHEME_NAME, connection, model);
			// Check
			verify(statement, never()).execute(anyString());
		}

		@Test
		void printsTheReasonForATableWhichIsNotPartOfTheModel() throws Exception {
			// Prepare
			prepareAPostgreConnectionWithASequence();
			DBDataScheme model = new DBDataSchemeModel(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
			// Run
			String returned = updateAndReadConsoleOutput(model);
			// Check
			assertTrue(returned.contains("table '" + TABLE_NAME + "' is not part of the model"));
		}
	}
}
