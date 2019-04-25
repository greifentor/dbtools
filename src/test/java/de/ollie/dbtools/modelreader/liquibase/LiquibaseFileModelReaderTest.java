package de.ollie.dbtools.modelreader.liquibase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import de.ollie.dbtools.modelreader.DBColumn;
import de.ollie.dbtools.modelreader.DBDataScheme;
import de.ollie.dbtools.modelreader.DBTable;
import de.ollie.dbtools.modelreader.DBType;
import de.ollie.dbtools.modelreader.DBTypeConverter;
import de.ollie.dbtools.modelreader.DefaultDBObjectFactory;
import de.ollie.dbtools.modelreader.models.DBColumnModel;
import de.ollie.dbtools.modelreader.models.DBDataSchemeModel;
import de.ollie.dbtools.modelreader.models.DBTableModel;

/**
 * Unit tests of class "LiquibaseFileModelReader".
 *
 * @author Oliver.Lieshoff
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class LiquibaseFileModelReaderTest {

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

	private static final String TABLE_1_PRIMARY_KEY = TABLE_NAME_1 + "_pkey";

	private LiquibaseFileModelReader unitUnderTest = null;

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private DefaultDBObjectFactory factory = new DefaultDBObjectFactory();
	private Path pathMasterFile = null;
	private Path pathTicket1File = null;
	private Path pathVersionMasterFile = null;
	private DBTypeConverter typesConverter = new DBTypeConverter();

	private static final String CHANGE_LOG_MASTER_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<databaseChangeLog\n"
			+ "        xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n"
			+ "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
			+ "        xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n"
			+ "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd\">\n"
			+ "    <include file=\"1.0.0/version-master.xml\" relativeToChangelogFile=\"true\"/>\n"
			+ "</databaseChangeLog>";

	private static final String VERSION_MASTER_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\r\n"
			+ "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
			+ "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\r\n"
			+ "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd\">\r\n"
			+ "    <include file=\"Ticket-1/Ticket-1.xml\" relativeToChangelogFile=\"true\"/>\r\n"
			+ "</databaseChangeLog>";

	private static final String TICKET_1_CONTENT = "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n"
			+ "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\r\n"
			+ "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n"
			+ "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.5.xsd\">\r\n"
			+ "    <changeSet author=\"Oliver.Lieshoff\" id=\"Ticket-1\">\r\n"
			+ "        <createTable tableName=\"" + TABLE_NAME_1 + "\">\r\n"
			+ "            <column name=\"" + COLUMN_NAME_1
			+ "\" type=\"INTEGER\">\r\n"
			+ "                <constraints primaryKey=\"true\" primaryKeyName=\""
			+ TABLE_1_PRIMARY_KEY + "_pkey\"/>\r\n"
			+ "            </column>\r\n" + "            <column name=\""
			+ COLUMN_NAME_2 + "\" type=\"VARCHAR(100)\"/>\r\n"
			+ "            <column name=\"" + COLUMN_NAME_3
			+ "\" type=\"NUMERIC(10,2)\"/>\r\n" + "        </createTable>\r\n"
			+ "    </changeSet>\r\n" + "</databaseChangeLog>";

	@Before
	public void setUp() throws Exception {
		File baseFolder = this.temp.newFolder();
		File versionFolder = new File(baseFolder, "1.0.0");
		versionFolder.mkdirs();
		File ticketFolder = new File(versionFolder, "Ticket-1");
		ticketFolder.mkdirs();
		this.pathMasterFile = Files.createFile(
				Paths.get(baseFolder.getAbsolutePath(), "master.xml"));
		Files.write(this.pathMasterFile, CHANGE_LOG_MASTER_CONTENT.getBytes());
		this.pathVersionMasterFile = Files.createFile(Paths
				.get(versionFolder.getAbsolutePath(), "version-master.xml"));
		Files.write(this.pathVersionMasterFile,
				VERSION_MASTER_CONTENT.getBytes());
		this.pathTicket1File = Files.createFile(
				Paths.get(ticketFolder.getAbsolutePath(), "Ticket-1.xml"));
		Files.write(this.pathTicket1File, TICKET_1_CONTENT.getBytes());
		this.unitUnderTest = new LiquibaseFileModelReader(this.factory,
				this.typesConverter, baseFolder, this.pathMasterFile.toFile());
	}

	@Test
	public void temporaryFilesAreCreatedProperly() throws Exception {
		assertThat(this.pathMasterFile.toFile().exists(), equalTo(true));
		assertThat(CHANGE_LOG_MASTER_CONTENT.getBytes(),
				equalTo(Files.readAllBytes(this.pathMasterFile)));
		assertThat(this.pathVersionMasterFile.toFile().exists(), equalTo(true));
		assertThat(VERSION_MASTER_CONTENT.getBytes(),
				equalTo(Files.readAllBytes(this.pathVersionMasterFile)));
		assertThat(this.pathTicket1File.toFile().exists(), equalTo(true));
		assertThat(TICKET_1_CONTENT.getBytes(),
				equalTo(Files.readAllBytes(this.pathTicket1File)));
	}

	@Test
	public void readModel_ModelWithOnlyOneTable_ReadsTheModelFromTheFiles()
			throws Exception {
		// Vorbereitung
		List<DBColumn> columns = new ArrayList<>();
		columns.add(new DBColumnModel(COLUMN_NAME_1, "INTEGER", DBType.INTEGER,
				-1, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_2, "VARCHAR", DBType.VARCHAR,
				100, -1));
		columns.add(new DBColumnModel(COLUMN_NAME_3, "NUMERIC", DBType.NUMERIC,
				10, 2));
		DBTable table = new DBTableModel(TABLE_NAME_1, columns,
				new ArrayList<>());
		List<DBTable> tables = new ArrayList<>();
		tables.add(table);
		DBDataScheme expected = new DBDataSchemeModel(tables,
				new ArrayList<>());

		// Ausführung
		DBDataScheme returned = this.unitUnderTest.readModel();

		// Prüfung
		assertEquals(expected.toString(), returned.toString());

	}

}