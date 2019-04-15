package de.ollie.dbtools.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import de.ollie.dbtools.modelreader.models.DBColumnModel;
import de.ollie.dbtools.modelreader.models.DBTableModel;

/**
 * Unit tests for class "StatementBuilder".
 * 
 * @author Oliver.Lieshoff
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class StatementBuilderTest {

	private static final String COLUMN_NAME_1 = "Id";
	private static final String COLUMN_NAME_2 = "Name";
	private static final String COLUMN_NAME_3 = "Salary";

	private static final String TABLE_NAME_1 = "TestTable";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@InjectMocks
	private StatementBuilder unitUnderTest;

	@Test(expected = NullPointerException.class)
	public void createSelectStatmentString_PassANullValue_ThrowsAnException() {
		this.unitUnderTest.createSelectStatementString(null);
	}

	@Test
	public void createSelectStatmentString_PassATable_ReturnsASelectStatementWithAllFieldsOfTheTable() {
		// Prepare
		String expected = "SELECT " + COLUMN_NAME_1 + ", " + COLUMN_NAME_2
				+ ", " + COLUMN_NAME_3 + " FROM " + TABLE_NAME_1;
		List<DBColumnModel> columns = new ArrayList<>();
		columns.add(new DBColumnModel(COLUMN_NAME_1, "INTEGER", Types.INTEGER,
				-1, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_2, "VARCHAR", Types.VARCHAR,
				100, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_3, "NUMERIC", Types.NUMERIC,
				10, 2));
		DBTableModel table = new DBTableModel(TABLE_NAME_1, columns,
				new ArrayList<>());
		// Run
		String returned = this.unitUnderTest.createSelectStatementString(table);
		// Check
		assertThat(returned, equalTo(expected));
	}

	@Test
	public void createSelectStatmentString_PassATableWithOutColumns_ThrowsAnException() {
		// Prepare
		DBTableModel table = new DBTableModel(TABLE_NAME_1, new ArrayList<>(),
				new ArrayList<>());
		// Check
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage(
				equalTo("Table '" + TABLE_NAME_1 + "' has no columns."));

		this.unitUnderTest.createSelectStatementString(table);
	}

	@Test(expected = NullPointerException.class)
	public void createInsertStatmentString_PassANullValue_ThrowsAnException() {
		this.unitUnderTest.createInsertStatementString(null);
	}

	@Test
	public void createInsertStatmentString_PassATable_ReturnsAInsertStatementWithAllFieldsOfTheTable() {
		// Prepare
		String expected = "INSERT INTO " + TABLE_NAME_1 + " (" + COLUMN_NAME_1
				+ ", " + COLUMN_NAME_2 + ", " + COLUMN_NAME_3
				+ ") VALUES (?, ?, ?)";
		List<DBColumnModel> columns = new ArrayList<>();
		columns.add(new DBColumnModel(COLUMN_NAME_1, "INTEGER", Types.INTEGER,
				-1, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_2, "VARCHAR", Types.VARCHAR,
				100, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_3, "NUMERIC", Types.NUMERIC,
				10, 2));
		DBTableModel table = new DBTableModel(TABLE_NAME_1, columns,
				new ArrayList<>());
		// Run
		String returned = this.unitUnderTest.createInsertStatementString(table);
		// Check
		assertThat(returned, equalTo(expected));
	}

	@Test
	public void createInsertStatmentString_PassATableWithOutColumns_ThrowsAnException() {
		// Prepare
		DBTableModel table = new DBTableModel(TABLE_NAME_1, new ArrayList<>(),
				new ArrayList<>());
		// Check
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage(
				equalTo("Table '" + TABLE_NAME_1 + "' has no columns."));

		this.unitUnderTest.createInsertStatementString(table);
	}

}