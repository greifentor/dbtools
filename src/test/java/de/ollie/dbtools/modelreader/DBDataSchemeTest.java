package de.ollie.dbtools.modelreader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DBDataSchemeTest {

	@Spy
	private DBDataScheme unitUnderTest;

	@Nested
	class findByName_String {

		private static final String TABLE_NAME = "TABLE_NAME";
		private static final String ANOTHER_TABLE_NAME = "ANOTHER_TABLE_NAME";

		@Mock
		private DBTable table;

		@Mock
		private DBTable anotherTable;

		@Test
		void returnsAnEmptyOptionalPassingANullValueAsTableName() {
			// Prepare
			doReturn(TABLE_NAME).when(table).getName();
			doReturn(List.of(table)).when(unitUnderTest).getTables();
			// Run
			Optional<DBTable> returned = unitUnderTest.findByName(null);
			// Check
			assertTrue(returned.isEmpty());
		}

		@Test
		void returnsAnEmptyOptionalForASchemeWithoutTables() {
			// Prepare
			doReturn(List.of()).when(unitUnderTest).getTables();
			// Run
			Optional<DBTable> returned = unitUnderTest.findByName(TABLE_NAME);
			// Check
			assertTrue(returned.isEmpty());
		}

		@Test
		void returnsAnEmptyOptionalForAnUnknownTableName() {
			// Prepare
			doReturn(TABLE_NAME).when(table).getName();
			doReturn(List.of(table)).when(unitUnderTest).getTables();
			// Run
			Optional<DBTable> returned = unitUnderTest.findByName(ANOTHER_TABLE_NAME);
			// Check
			assertTrue(returned.isEmpty());
		}

		@Test
		void returnsTheTableWithAMatchingName() {
			// Prepare
			doReturn(ANOTHER_TABLE_NAME).when(anotherTable).getName();
			doReturn(TABLE_NAME).when(table).getName();
			doReturn(List.of(anotherTable, table)).when(unitUnderTest).getTables();
			// Run
			Optional<DBTable> returned = unitUnderTest.findByName(TABLE_NAME);
			// Check
			assertEquals(table, returned.get());
		}
	}
}
