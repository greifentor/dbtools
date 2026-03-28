package de.ollie.dbtools.copier;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.ollie.dbtools.modelreader.DBForeignKey;
import de.ollie.dbtools.utils.StatementBuilder;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ForeignKeyRestorerTest {

	@InjectMocks
	private ForeignKeyRestorer unitUnderTest;

	@Nested
	class restore_ListDBForeignKey_Connection_StatementBuilder {

		private static final String ADD_STATEMENT_STRING = "add-statement-string";

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
			when(statementBuilder.createAddForeignKeyStatementString(foreignKey)).thenReturn(ADD_STATEMENT_STRING);
			// Run
			unitUnderTest.restore(fks, connection, statementBuilder);
			// Check
			verify(statement, times(1)).execute(ADD_STATEMENT_STRING);
		}
	}
}
