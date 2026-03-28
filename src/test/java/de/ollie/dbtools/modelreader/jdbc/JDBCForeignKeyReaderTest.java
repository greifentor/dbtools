package de.ollie.dbtools.modelreader.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import de.ollie.dbtools.modelreader.DBForeignKey;
import de.ollie.dbtools.modelreader.DBTable;
import de.ollie.dbtools.modelreader.models.DBForeignKeyModel;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JDBCForeignKeyReaderTest {

	@InjectMocks
	private JDBCForeignKeyReader unitUnderTest;

	@Nested
	class getForeignKeys_DatabaseMetaData_ListDBTable {

		private static final String FOREIGNKEY_COLUMN_NAME_0 = "foreignkey-column-name-0";
		private static final String FOREIGNKEY_COLUMN_NAME_1 = "foreignkey-column-name-1";
		private static final String FOREIGNKEY_NAME_0 = "foreignkey-name-0";
		private static final String FOREIGNKEY_NAME_1 = "foreignkey-name-1";
		private static final String FOREIGNKEY_TABLE_NAME_0 = "foreignkey-table-name-0";
		private static final String FOREIGNKEY_TABLE_NAME_1 = "foreignkey-table-name-1";
		private static final String PKCOLUMN_NAME_0 = "pkcolumn-name-0";
		private static final String PKCOLUMN_NAME_1 = "pkcolumn-name-1";
		private static final String PKTABLE_NAME_0 = "pktable-name-0";
		private static final String PKTABLE_NAME_1 = "pktable-name-1";
		private static final String SCHEME_NAME = "scheme-name";
		private static final String TABLE_NAME_0 = "table-name-0";
		private static final String TABLE_NAME_1 = "table-name-1";

		@Mock
		private DBTable dbTable0;

		@Mock
		private DBTable dbTable1;

		@Mock
		private DatabaseMetaData databaseMetaData;

		@Mock
		private ResultSet resultSet;

		@Test
		void throwsAnException_passingANullValue_asDatabaseMetaData() {
			assertThrows(IllegalArgumentException.class, () -> unitUnderTest.getForeignKeys(null, SCHEME_NAME, List.of()));
		}

		@Test
		void throwsAnException_passingANullValue_asListDBTables() {
			assertThrows(
				IllegalArgumentException.class,
				() -> unitUnderTest.getForeignKeys(databaseMetaData, SCHEME_NAME, null)
			);
		}

		@Test
		void returnsTheForeignKeysOfAllPassedTables() throws Exception {
			// Prepare
			List<DBForeignKey<?>> expected = List.of(
				new DBForeignKeyModel(
					FOREIGNKEY_NAME_0,
					FOREIGNKEY_TABLE_NAME_0,
					FOREIGNKEY_COLUMN_NAME_0,
					PKTABLE_NAME_0,
					PKCOLUMN_NAME_0
				)
			);
			when(dbTable0.getName()).thenReturn(TABLE_NAME_0);
			when(databaseMetaData.getImportedKeys(null, SCHEME_NAME, TABLE_NAME_0)).thenReturn(resultSet);
			when(resultSet.next()).thenReturn(true, false);
			when(resultSet.getString("FK_NAME")).thenReturn(FOREIGNKEY_NAME_0);
			when(resultSet.getString("FKTABLE_NAME")).thenReturn(FOREIGNKEY_TABLE_NAME_0);
			when(resultSet.getString("FKCOLUMN_NAME")).thenReturn(FOREIGNKEY_COLUMN_NAME_0);
			when(resultSet.getString("PKTABLE_NAME")).thenReturn(PKTABLE_NAME_0);
			when(resultSet.getString("PKCOLUMN_NAME")).thenReturn(PKCOLUMN_NAME_0);
			// Run
			List<DBForeignKey<?>> returned = unitUnderTest.getForeignKeys(databaseMetaData, SCHEME_NAME, List.of(dbTable0));
			// Check
			assertEquals(expected, returned);
		}

		@Test
		void returnsTheForeignKeysOfAllPassedTables_compositeKeys() throws Exception {
			// Prepare
			List<DBForeignKey<?>> expected = List.of(
				new DBForeignKeyModel(
					FOREIGNKEY_NAME_0,
					FOREIGNKEY_TABLE_NAME_0,
					FOREIGNKEY_COLUMN_NAME_0,
					PKTABLE_NAME_0,
					PKCOLUMN_NAME_0
				)
					.addColumnNames(FOREIGNKEY_COLUMN_NAME_1, PKCOLUMN_NAME_1)
			);
			when(dbTable0.getName()).thenReturn(TABLE_NAME_0);
			when(databaseMetaData.getImportedKeys(null, SCHEME_NAME, TABLE_NAME_0)).thenReturn(resultSet);
			when(resultSet.next()).thenReturn(true, true, false);
			when(resultSet.getString("FK_NAME")).thenReturn(FOREIGNKEY_NAME_0, FOREIGNKEY_NAME_0);
			when(resultSet.getString("FKTABLE_NAME")).thenReturn(FOREIGNKEY_TABLE_NAME_0, FOREIGNKEY_TABLE_NAME_0);
			when(resultSet.getString("FKCOLUMN_NAME")).thenReturn(FOREIGNKEY_COLUMN_NAME_0, FOREIGNKEY_COLUMN_NAME_1);
			when(resultSet.getString("PKTABLE_NAME")).thenReturn(PKTABLE_NAME_0, PKTABLE_NAME_0);
			when(resultSet.getString("PKCOLUMN_NAME")).thenReturn(PKCOLUMN_NAME_0, PKCOLUMN_NAME_1);
			// Run
			List<DBForeignKey<?>> returned = unitUnderTest.getForeignKeys(databaseMetaData, SCHEME_NAME, List.of(dbTable0));
			// Check
			assertEquals(expected, returned);
		}

		@Test
		void returnsTheForeignKeysOfAllPassedTables_twoDifferentKeys() throws Exception {
			// Prepare
			List<DBForeignKey<?>> expected = List.of(
				new DBForeignKeyModel(
					FOREIGNKEY_NAME_0,
					FOREIGNKEY_TABLE_NAME_0,
					FOREIGNKEY_COLUMN_NAME_0,
					PKTABLE_NAME_0,
					PKCOLUMN_NAME_0
				),
				new DBForeignKeyModel(
					FOREIGNKEY_NAME_1,
					FOREIGNKEY_TABLE_NAME_1,
					FOREIGNKEY_COLUMN_NAME_1,
					PKTABLE_NAME_1,
					PKCOLUMN_NAME_1
				)
			);
			when(dbTable0.getName()).thenReturn(TABLE_NAME_0);
			when(dbTable1.getName()).thenReturn(TABLE_NAME_1);
			when(databaseMetaData.getImportedKeys(null, SCHEME_NAME, TABLE_NAME_0)).thenReturn(resultSet);
			when(databaseMetaData.getImportedKeys(null, SCHEME_NAME, TABLE_NAME_1)).thenReturn(resultSet);
			when(resultSet.next()).thenReturn(true, false, true, false);
			when(resultSet.getString("FK_NAME")).thenReturn(FOREIGNKEY_NAME_0, FOREIGNKEY_NAME_1);
			when(resultSet.getString("FKTABLE_NAME")).thenReturn(FOREIGNKEY_TABLE_NAME_0, FOREIGNKEY_TABLE_NAME_1);
			when(resultSet.getString("FKCOLUMN_NAME")).thenReturn(FOREIGNKEY_COLUMN_NAME_0, FOREIGNKEY_COLUMN_NAME_1);
			when(resultSet.getString("PKTABLE_NAME")).thenReturn(PKTABLE_NAME_0, PKTABLE_NAME_1);
			when(resultSet.getString("PKCOLUMN_NAME")).thenReturn(PKCOLUMN_NAME_0, PKCOLUMN_NAME_1);
			// Run
			List<DBForeignKey<?>> returned = unitUnderTest.getForeignKeys(
				databaseMetaData,
				SCHEME_NAME,
				List.of(dbTable0, dbTable1)
			);
			// Check
			assertEquals(expected, returned);
		}
	}
}
