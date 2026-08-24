package de.ollie.dbtools.copier;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.ollie.dbtools.modelreader.DBForeignKey;
import de.ollie.dbtools.utils.StatementBuilder;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ForeignKeyRemoverTest {

	@InjectMocks
	private ForeignKeyRemover unitUnderTest;

	@Nested
	class remove_ListDBForeignKey_Connection_StatementBuilder {

		private static final String DROP_STATEMENT_STRING = "drop-statement-string";
		private static final String FOREIGN_KEY_NAME = "foreign-key-name";
		private static final String WARNING_MESSAGE = "warning-message";

		@Mock
		private Connection connection;

		@Mock
		private DBForeignKey<?> foreignKey;

		@Mock
		private Statement statement;

		@Mock
		private StatementBuilder statementBuilder;

		@Test
		void happyRun() throws Exception {
			// Prepare
			List<DBForeignKey<?>> fks = List.of(foreignKey);
			when(connection.createStatement()).thenReturn(statement);
			when(statementBuilder.createDropForeignKeyStatementString(foreignKey)).thenReturn(DROP_STATEMENT_STRING);
			// Run
			unitUnderTest.remove(fks, connection, statementBuilder);
			// Check
			verify(statement, times(1)).execute(DROP_STATEMENT_STRING);
		}

		@Test
		void printsAWarningOfTheStatementToTheConsole() throws Exception {
			// Prepare
			List<DBForeignKey<?>> fks = List.of(foreignKey);
			when(foreignKey.getName()).thenReturn(FOREIGN_KEY_NAME);
			when(connection.createStatement()).thenReturn(statement);
			when(statementBuilder.createDropForeignKeyStatementString(foreignKey)).thenReturn(DROP_STATEMENT_STRING);
			when(statement.getWarnings()).thenReturn(new SQLWarning(WARNING_MESSAGE));
			ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
			PrintStream originalOut = System.out;
			System.setOut(new PrintStream(consoleOutput));
			// Run
			try {
				unitUnderTest.remove(fks, connection, statementBuilder);
			} finally {
				System.setOut(originalOut);
			}
			// Check
			assertTrue(
				consoleOutput.toString().contains("foreign key '" + FOREIGN_KEY_NAME + "' not removed: " + WARNING_MESSAGE)
			);
		}
	}
}
