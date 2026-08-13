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
class DBTableTest {

	@Spy
	private DBTable unitUnderTest;

	@Nested
	class findIdFieldName {

		private static final String PRIMARY_KEY_COLUMN_NAME = "PRIMARY_KEY_COLUMN_NAME";

		@Mock
		private DBColumn column;

		@Mock
		private DBColumn primaryKeyColumn;

		@Mock
		private DBColumn anotherPrimaryKeyColumn;

		@Test
		void returnsAnEmptyOptionalForATableWithoutColumns() {
			// Prepare
			doReturn(List.of()).when(unitUnderTest).getColumns();
			// Run
			Optional<String> returned = unitUnderTest.findIdFieldName();
			// Check
			assertTrue(returned.isEmpty());
		}

		@Test
		void returnsAnEmptyOptionalForATableWithoutAPrimaryKeyColumn() {
			// Prepare
			doReturn(false).when(column).isPrimaryKey();
			doReturn(List.of(column)).when(unitUnderTest).getColumns();
			// Run
			Optional<String> returned = unitUnderTest.findIdFieldName();
			// Check
			assertTrue(returned.isEmpty());
		}

		@Test
		void returnsTheNameOfThePrimaryKeyColumn() {
			// Prepare
			doReturn(false).when(column).isPrimaryKey();
			doReturn(true).when(primaryKeyColumn).isPrimaryKey();
			doReturn(PRIMARY_KEY_COLUMN_NAME).when(primaryKeyColumn).getName();
			doReturn(List.of(column, primaryKeyColumn)).when(unitUnderTest).getColumns();
			// Run
			Optional<String> returned = unitUnderTest.findIdFieldName();
			// Check
			assertEquals(PRIMARY_KEY_COLUMN_NAME, returned.get());
		}

		@Test
		void returnsTheNameOfTheFirstPrimaryKeyColumnForACombinedPrimaryKey() {
			// Prepare
			doReturn(true).when(primaryKeyColumn).isPrimaryKey();
			doReturn(PRIMARY_KEY_COLUMN_NAME).when(primaryKeyColumn).getName();
			doReturn(List.of(primaryKeyColumn, anotherPrimaryKeyColumn)).when(unitUnderTest).getColumns();
			// Run
			Optional<String> returned = unitUnderTest.findIdFieldName();
			// Check
			assertEquals(PRIMARY_KEY_COLUMN_NAME, returned.get());
		}
	}
}
