package de.ollie.dbtools.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.ollie.dbtools.modelreader.DBColumn;
import de.ollie.dbtools.modelreader.DBForeignKey;
import de.ollie.dbtools.modelreader.DBType;
import de.ollie.dbtools.modelreader.models.DBColumnModel;
import de.ollie.dbtools.modelreader.models.DBTableModel;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for class "StatementBuilder".
 *
 * @author Oliver.Lieshoff
 *
 */
@ExtendWith(MockitoExtension.class)
class StatementBuilderTest {

	private static final String COLUMN_NAME_1 = "Id";
	private static final String COLUMN_NAME_2 = "Name";
	private static final String COLUMN_NAME_3 = "Salary";

	private static final String TABLE_NAME_1 = "TestTable";

	@InjectMocks
	private StatementBuilder unitUnderTest;

	@Nested
	class createAddForeignKeyStatementString_DBForeignKey {

		private static final String FK_NAME = "fk-name";
		private static final String FK_COLUMN_NAMES = "fk-column-names";
		private static final String REF_COLUMN_NAMES = "ref-column-names";
		private static final String REF_TABLE_NAME = "ref-table-name";
		private static final String TABLE_NAME = "table-name";

		@Test
		void throwsException_passingANullValue_asDBForeignKey() {
			assertThrows(IllegalArgumentException.class, () -> unitUnderTest.createAddForeignKeyStatementString(null));
		}

		@Test
		void returnsTheCorrectStatemen_passingADBForeignKey() {
			// Prepare
			String expected =
				"ALTER TABLE " +
				TABLE_NAME +
				" ADD CONSTRAINT " +
				FK_NAME +
				"FOREIGN KEY (" +
				FK_COLUMN_NAMES +
				") " +
				"REFERENCES " +
				REF_TABLE_NAME +
				"(" +
				REF_COLUMN_NAMES +
				")";
			DBForeignKey<?> fk = mock(DBForeignKey.class);
			when(fk.getName()).thenReturn(FK_NAME);
			when(fk.getKeyColumnNames()).thenReturn(List.of(FK_COLUMN_NAMES));
			when(fk.getTableName()).thenReturn(TABLE_NAME);
			when(fk.getReferencedKeyColumnNames()).thenReturn(List.of(REF_COLUMN_NAMES));
			when(fk.getReferencedTableName()).thenReturn(REF_TABLE_NAME);
			// Run & Check
			assertEquals(expected, unitUnderTest.createAddForeignKeyStatementString(fk));
		}
	}

	@Nested
	class createDropForeignKeyStatementString_DBForeignKey {

		private static final String FK_NAME = "fk-name";
		private static final String TABLE_NAME = "table-name";

		@Test
		void throwsException_passingANullValue_asDBForeignKey() {
			assertThrows(IllegalArgumentException.class, () -> unitUnderTest.createDropForeignKeyStatementString(null));
		}

		@Test
		void returnsTheCorrectStatemen_passingADBForeignKey() {
			// Prepare
			String expected = "ALTER TABLE " + TABLE_NAME + " DROP CONSTRAINT " + FK_NAME;
			DBForeignKey<?> fk = mock(DBForeignKey.class);
			when(fk.getName()).thenReturn(FK_NAME);
			when(fk.getTableName()).thenReturn(TABLE_NAME);
			// Run & Check
			assertEquals(expected, unitUnderTest.createDropForeignKeyStatementString(fk));
		}
	}

	@Test
	void createSelectStatmentString_PassANullValue_ThrowsAnException() {
		assertThrows(NullPointerException.class, () -> unitUnderTest.createSelectStatementString(null));
	}

	@Test
	void createSelectStatmentString_PassATable_ReturnsASelectStatementWithAllFieldsOfTheTable() {
		// Prepare
		String expected = "SELECT " + COLUMN_NAME_1 + ", " + COLUMN_NAME_2 + ", " + COLUMN_NAME_3 + " FROM " + TABLE_NAME_1;
		List<DBColumn> columns = new ArrayList<>();
		columns.add(new DBColumnModel(COLUMN_NAME_1, "INTEGER", DBType.INTEGER, -1, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_2, "VARCHAR", DBType.VARCHAR, 100, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_3, "NUMERIC", DBType.NUMERIC, 10, 2));
		DBTableModel table = new DBTableModel(TABLE_NAME_1, columns, new ArrayList<>());
		// Run
		String returned = unitUnderTest.createSelectStatementString(table);
		// Check
		assertEquals(expected, returned);
	}

	@Test
	void createSelectStatmentString_PassATableWithOutColumns_ThrowsAnException() {
		// Prepare
		DBTableModel table = new DBTableModel(TABLE_NAME_1, new ArrayList<>(), new ArrayList<>());
		// Check
		IllegalArgumentException thrown = assertThrows(
			IllegalArgumentException.class,
			() -> unitUnderTest.createSelectStatementString(table)
		);
		assertEquals("Table '" + TABLE_NAME_1 + "' has no columns.", thrown.getMessage());
	}

	@Test
	void createInsertStatmentString_PassANullValue_ThrowsAnException() {
		assertThrows(NullPointerException.class, () -> unitUnderTest.createInsertStatementString(null));
	}

	@Test
	void createInsertStatmentString_PassATable_ReturnsAInsertStatementWithAllFieldsOfTheTable() {
		// Prepare
		String expected =
			"INSERT INTO " +
			TABLE_NAME_1 +
			" (" +
			COLUMN_NAME_1 +
			", " +
			COLUMN_NAME_2 +
			", " +
			COLUMN_NAME_3 +
			") VALUES (?, ?, ?)";
		List<DBColumn> columns = new ArrayList<>();
		columns.add(new DBColumnModel(COLUMN_NAME_1, "INTEGER", DBType.INTEGER, -1, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_2, "VARCHAR", DBType.VARCHAR, 100, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_3, "NUMERIC", DBType.NUMERIC, 10, 2));
		DBTableModel table = new DBTableModel(TABLE_NAME_1, columns, new ArrayList<>());
		// Run
		String returned = unitUnderTest.createInsertStatementString(table);
		// Check
		assertEquals(expected, returned);
	}

	@Test
	void createInsertStatmentString_PassATableWithOutColumns_ThrowsAnException() {
		// Prepare
		DBTableModel table = new DBTableModel(TABLE_NAME_1, new ArrayList<>(), new ArrayList<>());
		// Check
		IllegalArgumentException thrown = assertThrows(
			IllegalArgumentException.class,
			() -> unitUnderTest.createInsertStatementString(table)
		);
		assertEquals("Table '" + TABLE_NAME_1 + "' has no columns.", thrown.getMessage());
	}
}
