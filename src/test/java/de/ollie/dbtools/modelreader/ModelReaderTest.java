/*
 * ModelReaderTest.java
 *
 * 20.09.2018
 *
 * (c) by ollie
 */
package de.ollie.dbtools.modelreader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import de.ollie.dbtools.modelreader.dto.DBColumnModelDTO;
import de.ollie.dbtools.modelreader.dto.DBDataModelDTO;
import de.ollie.dbtools.modelreader.dto.DBSequenceModelDTO;
import de.ollie.dbtools.modelreader.dto.DBTableModelDTO;

/**
 * Unit tests of class "ModelReader".
 *
 * @author O.Lieshoff
 */
@RunWith(MockitoJUnitRunner.class)
public class ModelReaderTest {

	private static final String COLUMN_NAME_1 = "Id";
	private static final String COLUMN_NAME_2 = "Name";
	private static final String COLUMN_NAME_3 = "Salary";
	private static final String COLUMN_NAME_4 = "field4";
	private static final String COLUMN_NAME_5 = "field5";
	private static final String COLUMN_NAME_6 = "field6";
	private static final String COLUMN_NAME_7 = "field7";
	private static final String COLUMN_NAME_8 = "field8";
	private static final String COLUMN_NAME_9 = "field9";

	private static final int SEQUENCE_INC = 42;
	private static final String SEQUENCE_NAME = "Sequence";
	private static final int SEQUENCE_START = 4711;

	private static final String TABLE_NAME_1 = "TestTable";
	private static final String TABLE_NAME_2 = "AnotherTestTable";

	@InjectMocks
	private ModelReader unitUnderTest;

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private Connection connectionSource = null;

	private String dbNameSource = "sourceDB";

	@BeforeClass
	public static void setUpClass() {
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (Exception e) {
			System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
			e.printStackTrace();
			return;
		}
	}

	@Before
	public void setUp() throws Exception {
		this.connectionSource = getConnection(dbNameSource);
	}

	private Connection getConnection(String dbName) throws Exception {
		return DriverManager.getConnection(
				"jdbc:hsqldb:file:" + temp.getRoot().getAbsolutePath() + "/" + dbName + ";shutdown=true", "SA", "");
	}

	@After
	public void tearDown() throws Exception {
		if (this.connectionSource != null) {
			this.connectionSource.close();
		}
	}

	@Test
	public void readModel_ValidConnectionOfAnEmptyDatabasePassed_ReturnsAnEmptyModel() throws Exception {
		// Prepare
		DBDataModelDTO expected = new DBDataModelDTO(new ArrayList<DBTableModelDTO>(),
				new ArrayList<DBSequenceModelDTO>());

		// Run
		DBDataModelDTO returned = this.unitUnderTest.readModel(this.connectionSource);

		// Check
		assertEquals(expected.toString(), returned.toString());
	}

	private void createDatabase(Connection connection) throws Exception {
		Statement stmt = connection.createStatement();
		stmt.execute("CREATE TABLE " + TABLE_NAME_1 + " (" + COLUMN_NAME_1 + " INTEGER, " + COLUMN_NAME_2
				+ " VARCHAR(100), " + COLUMN_NAME_3 + " NUMERIC(10,2))");
		stmt.close();
	}

	@Test
	public void readModel_ValidConnectionWithATablePassed_ReturnsTheModelOfTheDatabaseLinkedToTheConnection()
			throws Exception {
		// Prepare
		createDatabase(this.connectionSource);

		DBTableModelDTO table = new DBTableModelDTO(TABLE_NAME_1.toUpperCase(), new ArrayList<DBColumnModelDTO>());
		List<DBTableModelDTO> tables = new ArrayList<>();
		tables.add(table);
		DBDataModelDTO expected = new DBDataModelDTO(tables, new ArrayList<DBSequenceModelDTO>());

		// Run
		DBDataModelDTO returned = this.unitUnderTest.readModel(this.connectionSource);

		// Check
		assertEquals(TABLE_NAME_1.toUpperCase(), returned.getTables().get(0).getName());
	}

	@Test
	public void readModel_ValidConnectionWithATableAndColumnsPassed_ReturnsTheModelOfTheDatabaseLinkedToTheConnection()
			throws Exception {
		// Prepare
		createDatabase(this.connectionSource);

		List<DBColumnModelDTO> columns = new ArrayList<>();
		columns.add(new DBColumnModelDTO(COLUMN_NAME_1.toUpperCase(), "INTEGER", Types.INTEGER, -1, -1));
		columns.add(new DBColumnModelDTO(COLUMN_NAME_2.toUpperCase(), "VARCHAR", Types.VARCHAR, 100, -1));
		columns.add(new DBColumnModelDTO(COLUMN_NAME_3.toUpperCase(), "NUMERIC", Types.NUMERIC, 10, 2));
		DBTableModelDTO table = new DBTableModelDTO(TABLE_NAME_1.toUpperCase(), columns);
		List<DBTableModelDTO> tables = new ArrayList<>();
		tables.add(table);
		DBDataModelDTO expected = new DBDataModelDTO(tables, new ArrayList<DBSequenceModelDTO>());

		// Run
		DBDataModelDTO returned = this.unitUnderTest.readModel(this.connectionSource);

		// Check
		assertEquals(expected.toString(), returned.toString());
	}

	@Test
	public void readModel_ValidConnectionWithTwoTableAndColumnsPassed_ReturnsTheModelOfTheDatabaseLinkedToTheConnection()
			throws Exception {
		// Prepare
		createDatabaseWithTwoTables(this.connectionSource);

		List<DBColumnModelDTO> columns1 = new ArrayList<>();
		columns1.add(new DBColumnModelDTO(COLUMN_NAME_1.toUpperCase(), "INTEGER", Types.INTEGER, -1, -1));
		columns1.add(new DBColumnModelDTO(COLUMN_NAME_2.toUpperCase(), "VARCHAR", Types.VARCHAR, 100, -1));
		columns1.add(new DBColumnModelDTO(COLUMN_NAME_3.toUpperCase(), "NUMERIC", Types.NUMERIC, 10, 2));
		DBTableModelDTO table1 = new DBTableModelDTO(TABLE_NAME_1.toUpperCase(), columns1);
		List<DBColumnModelDTO> columns2 = new ArrayList<>();
		columns2.add(new DBColumnModelDTO(COLUMN_NAME_1.toUpperCase(), "INTEGER", Types.INTEGER, -1, -1));
		columns2.add(new DBColumnModelDTO(COLUMN_NAME_2.toUpperCase(), "VARCHAR", Types.VARCHAR, 100, -1));
		columns2.add(new DBColumnModelDTO(COLUMN_NAME_3.toUpperCase(), "NUMERIC", Types.NUMERIC, 10, 2));
		DBTableModelDTO table2 = new DBTableModelDTO(TABLE_NAME_2.toUpperCase(), columns1);
		List<DBTableModelDTO> tables = new ArrayList<>();
		tables.add(table2);
		tables.add(table1);
		DBDataModelDTO expected = new DBDataModelDTO(tables, new ArrayList<DBSequenceModelDTO>());

		// Run
		DBDataModelDTO returned = this.unitUnderTest.readModel(this.connectionSource);

		// Check
		assertEquals(expected.toString(), returned.toString());
	}

	private void createDatabaseWithTwoTables(Connection connection) throws Exception {
		Statement stmt = connection.createStatement();
		stmt.execute("CREATE TABLE " + TABLE_NAME_1 + " (" + COLUMN_NAME_1 + " INTEGER, " + COLUMN_NAME_2
				+ " VARCHAR(100), " + COLUMN_NAME_3 + " NUMERIC(10,2))");
		stmt.execute("CREATE TABLE " + TABLE_NAME_2 + " (" + COLUMN_NAME_1 + " INTEGER, " + COLUMN_NAME_2
				+ " VARCHAR(100), " + COLUMN_NAME_3 + " NUMERIC(10,2))");
		stmt.close();
	}

	@Test
	public void readModel_ValidConnectionWithATableWithFieldsOfAllTypesPassed_ReturnsTheModelOfTheDatabaseLinkedToTheConnection()
			throws Exception {
		// Prepare
		createDatabaseWithATableWithFieldsAllTypes(this.connectionSource);

		List<DBColumnModelDTO> columns = new ArrayList<>();
		columns.add(new DBColumnModelDTO(COLUMN_NAME_1.toUpperCase(), "INTEGER", Types.INTEGER, -1, -1));
		columns.add(new DBColumnModelDTO(COLUMN_NAME_2.toUpperCase(), "VARCHAR", Types.VARCHAR, 100, -1));
		columns.add(new DBColumnModelDTO(COLUMN_NAME_3.toUpperCase(), "NUMERIC", Types.NUMERIC, 10, 2));
		columns.add(new DBColumnModelDTO(COLUMN_NAME_4.toUpperCase(), "CHARACTER", Types.CHAR, 12, -1));
		columns.add(new DBColumnModelDTO(COLUMN_NAME_5.toUpperCase(), "DECIMAL", Types.DECIMAL, 24, 12));
		DBTableModelDTO table = new DBTableModelDTO(TABLE_NAME_1.toUpperCase(), columns);
		List<DBTableModelDTO> tables = new ArrayList<>();
		tables.add(table);
		DBDataModelDTO expected = new DBDataModelDTO(tables, new ArrayList<DBSequenceModelDTO>());

		// Run
		DBDataModelDTO returned = this.unitUnderTest.readModel(this.connectionSource);

		// Check
		assertEquals(expected.toString(), returned.toString());
	}

	private void createDatabaseWithATableWithFieldsAllTypes(Connection connection) throws Exception {
		Statement stmt = connection.createStatement();
		stmt.execute("CREATE TABLE " + TABLE_NAME_1 + " (" + COLUMN_NAME_1 + " INTEGER, " + COLUMN_NAME_2
				+ " VARCHAR(100), " + COLUMN_NAME_3 + " NUMERIC(10,2), " + COLUMN_NAME_4 + " CHAR(12), " + COLUMN_NAME_5
				+ " DECIMAL(24,12))");
		stmt.close();
	}

	@Test
	public void readlModel_ValidConnectionWithOneSequence_ReturnsTheModelWithTheSequence() throws Exception {
		// Prepare
		Statement stmt = connectionSource.createStatement();
		stmt.execute("CREATE SEQUENCE " + SEQUENCE_NAME + " START WITH " + SEQUENCE_START + " INCREMENT BY "
				+ SEQUENCE_INC + ";");
		stmt.close();

		List<DBSequenceModelDTO> sequences = new ArrayList<>();
		sequences.add(new DBSequenceModelDTO(SEQUENCE_NAME, 100, 100));

		DBDataModelDTO expected = new DBDataModelDTO(new ArrayList<>(), sequences);

		// Run
		DBDataModelDTO returned = this.unitUnderTest.readModel(connectionSource);

		// Check
		assertThat(returned.getSequences().size(), equalTo(1));
		DBSequenceModelDTO sequence = returned.getSequences().get(0);
		assertThat(sequence.getIncrement(), equalTo(SEQUENCE_INC));
		assertThat(sequence.getName(), equalTo(SEQUENCE_NAME));
		assertThat(sequence.getStart(), equalTo(SEQUENCE_START));
	}

}