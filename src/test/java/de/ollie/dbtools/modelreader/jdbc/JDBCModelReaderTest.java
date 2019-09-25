/*
 * ModelReaderTest.java
 *
 * 20.09.2018
 *
 * (c) by ollie
 */
package de.ollie.dbtools.modelreader.jdbc;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import de.ollie.dbtools.modelreader.DBColumn;
import de.ollie.dbtools.modelreader.DBDataScheme;
import de.ollie.dbtools.modelreader.DBIndex;
import de.ollie.dbtools.modelreader.DBTable;
import de.ollie.dbtools.modelreader.DBType;
import de.ollie.dbtools.modelreader.DBTypeConverter;
import de.ollie.dbtools.modelreader.DefaultDBObjectFactory;
import de.ollie.dbtools.modelreader.models.DBColumnModel;
import de.ollie.dbtools.modelreader.models.DBDataSchemeModel;
import de.ollie.dbtools.modelreader.models.DBTableModel;

/**
 * Unit tests of class "ModelReader".
 *
 * @author O.Lieshoff
 */
@RunWith(MockitoJUnitRunner.class)
public class JDBCModelReaderTest {

	private static final String COLUMN_NAME_1 = "Id";
	private static final String COLUMN_NAME_2 = "Name";
	private static final String COLUMN_NAME_3 = "Salary";
	private static final String COLUMN_NAME_4 = "field4";
	private static final String COLUMN_NAME_5 = "field5";
	private static final String COLUMN_NAME_6 = "field6";
	private static final String COLUMN_NAME_7 = "field7";
	private static final String COLUMN_NAME_8 = "field8";
	private static final String COLUMN_NAME_9 = "field9";

	private static final String INDEX_NAME = "Index1";

	private static final String TABLE_NAME_1 = "TestTable";
	private static final String TABLE_NAME_2 = "AnotherTestTable";

	private JDBCModelReader unitUnderTest = null;

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private Connection connectionSource = null;
	private DefaultDBObjectFactory factory = new DefaultDBObjectFactory();
	private String dbNameSource = "sourceDB";
	private DBTypeConverter typeConverter = new DBTypeConverter();

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
		this.connectionSource = getConnection(this.dbNameSource);
		this.unitUnderTest = new JDBCModelReader(this.factory, this.typeConverter, this.connectionSource, null);
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
		DBDataScheme expected = new DBDataSchemeModel(new ArrayList<>(), new ArrayList<>());

		// Run
		DBDataScheme returned = this.unitUnderTest.readModel();

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

		DBTableModel table = new DBTableModel(TABLE_NAME_1.toUpperCase(), new ArrayList<>(), new ArrayList<DBIndex>());
		List<DBTableModel> tables = new ArrayList<>();
		tables.add(table);

		// Run
		DBDataScheme returned = this.unitUnderTest.readModel();

		// Check
		assertEquals(TABLE_NAME_1.toUpperCase(), returned.getTables().get(0).getName());
	}

	@Test
	public void readModel_ValidConnectionWithATableAndColumnsPassed_ReturnsTheModelOfTheDatabaseLinkedToTheConnection()
			throws Exception {
		// Prepare
		createDatabase(this.connectionSource);

		List<DBColumn> columns = new ArrayList<>();
		columns.add(new DBColumnModel(COLUMN_NAME_1.toUpperCase(), "INTEGER", DBType.INTEGER, -1, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_2.toUpperCase(), "VARCHAR", DBType.VARCHAR, 100, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_3.toUpperCase(), "NUMERIC", DBType.NUMERIC, 10, 2));
		DBTable table = new DBTableModel(TABLE_NAME_1.toUpperCase(), columns, new ArrayList<>());
		List<DBTable> tables = new ArrayList<>();
		tables.add(table);
		DBDataScheme expected = new DBDataSchemeModel(tables, new ArrayList<>());

		// Run
		DBDataScheme returned = this.unitUnderTest.readModel();

		// Check
		assertEquals(expected.toString(), returned.toString());
	}

	@Test
	public void readModel_ValidConnectionWithTwoTableAndColumnsPassed_ReturnsTheModelOfTheDatabaseLinkedToTheConnection()
			throws Exception {
		// Prepare
		createDatabaseWithTwoTables(this.connectionSource);

		List<DBColumn> columns1 = new ArrayList<>();
		columns1.add(new DBColumnModel(COLUMN_NAME_1.toUpperCase(), "INTEGER", DBType.INTEGER, -1, -1));
		columns1.add(new DBColumnModel(COLUMN_NAME_2.toUpperCase(), "VARCHAR", DBType.VARCHAR, 100, -1));
		columns1.add(new DBColumnModel(COLUMN_NAME_3.toUpperCase(), "NUMERIC", DBType.NUMERIC, 10, 2));
		DBTableModel table1 = new DBTableModel(TABLE_NAME_1.toUpperCase(), columns1, new ArrayList<>());
		List<DBColumn> columns2 = new ArrayList<>();
		columns2.add(new DBColumnModel(COLUMN_NAME_1.toUpperCase(), "INTEGER", DBType.INTEGER, -1, -1));
		columns2.add(new DBColumnModel(COLUMN_NAME_2.toUpperCase(), "VARCHAR", DBType.VARCHAR, 100, -1));
		columns2.add(new DBColumnModel(COLUMN_NAME_3.toUpperCase(), "NUMERIC", DBType.NUMERIC, 10, 2));
		DBTableModel table2 = new DBTableModel(TABLE_NAME_2.toUpperCase(), columns1, new ArrayList<>());
		List<DBTable> tables = new ArrayList<>();
		tables.add(table2);
		tables.add(table1);
		DBDataScheme expected = new DBDataSchemeModel(tables, new ArrayList<>());

		// Run
		DBDataScheme returned = this.unitUnderTest.readModel();

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

		List<DBColumn> columns = new ArrayList<>();
		columns.add(new DBColumnModel(COLUMN_NAME_1.toUpperCase(), "INTEGER", DBType.INTEGER, -1, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_2.toUpperCase(), "VARCHAR", DBType.VARCHAR, 100, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_3.toUpperCase(), "NUMERIC", DBType.NUMERIC, 10, 2));
		columns.add(new DBColumnModel(COLUMN_NAME_4.toUpperCase(), "CHARACTER", DBType.CHAR, 12, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_5.toUpperCase(), "DECIMAL", DBType.DECIMAL, 24, 12));
		DBTableModel table = new DBTableModel(TABLE_NAME_1.toUpperCase(), columns, new ArrayList<>());
		List<DBTable> tables = new ArrayList<>();
		tables.add(table);
		DBDataScheme expected = new DBDataSchemeModel(tables, new ArrayList<>());

		// Run
		DBDataScheme returned = this.unitUnderTest.readModel();

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
	public void readlModel_ValidConnectionWithAnIndexOnTable_ReturnsTheModelWithTheIndex() throws Exception {
		// Prepare
		Statement stmt = connectionSource.createStatement();
		stmt.execute("CREATE TABLE " + TABLE_NAME_1 + " (" + COLUMN_NAME_1 + " INTEGER, " + COLUMN_NAME_2
				+ " VARCHAR(100), " + COLUMN_NAME_3 + " NUMERIC(10,2), " + COLUMN_NAME_4 + " CHAR(12), " + COLUMN_NAME_5
				+ " DECIMAL(24,12))");
		stmt.execute("CREATE INDEX " + INDEX_NAME + " ON " + TABLE_NAME_1 + " (" + COLUMN_NAME_1 + ", " + COLUMN_NAME_2
				+ ")");
		stmt.execute("CREATE UNIQUE INDEX U" + INDEX_NAME + " ON " + TABLE_NAME_1 + " (" + COLUMN_NAME_3 + ", "
				+ COLUMN_NAME_4 + ")"); // To
										// check,
										// that
										// unique
										// indices
										// are
										// not
										// respected.
		stmt.close();

		List<DBColumn> columns = new ArrayList<>();
		columns.add(new DBColumnModel(COLUMN_NAME_1.toUpperCase(), "INTEGER", DBType.INTEGER, -1, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_2.toUpperCase(), "VARCHAR", DBType.VARCHAR, 100, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_3.toUpperCase(), "NUMERIC", DBType.NUMERIC, 10, 2));
		columns.add(new DBColumnModel(COLUMN_NAME_4.toUpperCase(), "CHARACTER", DBType.CHAR, 12, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_5.toUpperCase(), "DECIMAL", DBType.DECIMAL, 24, 12));
		DBTable table = new DBTableModel(TABLE_NAME_1.toUpperCase(), columns, new ArrayList<>());
		List<DBTable> tables = new ArrayList<>();
		tables.add(table);

		// Run
		DBDataScheme returned = this.unitUnderTest.readModel();

		// Check
		assertThat(returned.getTables().get(0).getIndices().size(), equalTo(1));
		DBIndex index = returned.getTables().get(0).getIndices().get(0);
		assertThat(index.getName(), equalTo(INDEX_NAME.toUpperCase()));
		assertThat(index.getColumns().size(), equalTo(2));
		assertThat(index.getColumns().get(0),
				equalTo(new DBColumnModel(COLUMN_NAME_1.toUpperCase(), "INTEGER", DBType.INTEGER, -1, -1)));
		assertThat(index.getColumns().get(1),
				equalTo(new DBColumnModel(COLUMN_NAME_2.toUpperCase(), "VARCHAR", DBType.VARCHAR, 100, -1)));
	}

}