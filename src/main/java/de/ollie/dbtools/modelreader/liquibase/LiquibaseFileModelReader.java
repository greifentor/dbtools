package de.ollie.dbtools.modelreader.liquibase;

import java.io.File;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import de.ollie.dbtools.modelreader.DBColumn;
import de.ollie.dbtools.modelreader.DBDataScheme;
import de.ollie.dbtools.modelreader.DBObjectFactory;
import de.ollie.dbtools.modelreader.DBTable;
import de.ollie.dbtools.modelreader.DBType;
import de.ollie.dbtools.modelreader.DBTypeConverter;
import de.ollie.dbtools.modelreader.ModelReader;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

/**
 * A Liquibase model reader, which is able to process Liquibase XML file from the file system.
 *
 * @author Oliver.Lieshoff
 *
 */
public class LiquibaseFileModelReader implements ModelReader {

	private DBObjectFactory factory;
	private DBTypeConverter typesConverter;
	private File baseDirectory;
	private File rootFile;

	/**
	 * Creates a new model reader with the passed parameters.
	 *
	 * @param factory       An object factory implementation to create the DB objects.
	 * @param typeConverter A converter for the types.
	 * @param baseDirectory The directory which contains the base XML file of the model.
	 * @param rootFile      The root file of the Liquibase model.
	 * @throws IllegalArgumentException Passing null value.
	 */
	public LiquibaseFileModelReader(DBObjectFactory factory, DBTypeConverter typesConverter, File baseDirectory,
			File rootFile) {
		super();
		this.baseDirectory = baseDirectory;
		this.factory = factory;
		this.rootFile = rootFile;
		this.typesConverter = typesConverter;
	}

	@Override
	public DBDataScheme readModel() throws Exception {
		DatabaseChangeLog changeLog = getDatabaseChangeLog();
		return createDataScheme(changeLog);
	}

	private DatabaseChangeLog getDatabaseChangeLog() throws Exception {
		ResourceAccessor resourceAccessor = new FileSystemResourceAccessor(this.baseDirectory.getAbsolutePath());
		ChangeLogParameters changeLogParameters = new ChangeLogParameters();
		return ChangeLogParserFactory.getInstance().getParser(this.rootFile.getName(), resourceAccessor)
				.parse(this.rootFile.getName(), changeLogParameters, resourceAccessor);
	}

	private DBDataScheme createDataScheme(DatabaseChangeLog changeLog) {
		List<DBTable> tables = new ArrayList<>();
		for (ChangeSet changeSet : changeLog.getChangeSets()) {
			for (Change change : changeSet.getChanges()) {
				if (change instanceof CreateTableChange) {
					CreateTableChange ctc = (CreateTableChange) change;
					List<DBColumn> columns = createColumns(ctc.getColumns());
					tables.add(this.factory.createTable(ctc.getTableName(), columns, new ArrayList<>()));
				} else {
					System.out.println("ignored: " + change.getClass().getSimpleName());
				}
			}
		}
		return this.factory.createDataScheme(tables, new ArrayList<>());
	}

	private List<DBColumn> createColumns(List<ColumnConfig> columnConfigs) {
		List<DBColumn> columns = new ArrayList<>();
		for (ColumnConfig cc : columnConfigs) {
			TypeInfo type = getDataType(cc);
			columns.add(this.factory.createColumn(cc.getName(), type.getName(), getDBType(type.getDataType()),
					type.getColumnSize(), type.getDecimalDigits()));
		}
		return columns;
	}

	private DBType getDBType(int dataType) {
		return typesConverter.convert(dataType);
	}

	// TODO: Types should be managed by special classes (independent from the
	// Types or other classes and frameworks).
	private TypeInfo getDataType(ColumnConfig cc) {
		LiquibaseDataType type = DataTypeFactory.getInstance().fromDescription(cc.getType(), null);
		TypeInfo ti = new TypeInfo().setName(type.getName().toUpperCase());
		if ("int".equalsIgnoreCase(type.getName()) || "integer".equalsIgnoreCase(type.getName())) {
			ti.setName("INTEGER");
			ti.setDataType(Types.INTEGER);
		} else if ("number".equalsIgnoreCase(type.getName()) || "numeric".equalsIgnoreCase(type.getName())) {
			ti.setName("NUMERIC");
			ti.setDataType(Types.NUMERIC);
			ti.setColumnSize(Integer.valueOf(type.getParameters()[0].toString()));
			ti.setDecimalDigits(Integer.valueOf(type.getParameters()[1].toString()));
		} else if ("varchar".equalsIgnoreCase(type.getName())) {
			ti.setDataType(Types.VARCHAR);
			ti.setColumnSize(Integer.valueOf(type.getParameters()[0].toString()));
		} else {
			System.out.println("ignored type: " + type.getName());
		}
		return ti;
	}

}