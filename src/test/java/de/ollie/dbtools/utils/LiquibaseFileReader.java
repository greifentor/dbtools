package de.ollie.dbtools.utils;

import java.io.File;

import org.junit.Test;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

public class LiquibaseFileReader {

	@Test
	public void parseAndGenerate() throws Exception {

		String changeLogFile = "db.changelog-master.xml";
		System.out.println(new File(changeLogFile).exists());
		ResourceAccessor resourceAccessor = new FileSystemResourceAccessor(
				"C:\\workspace\\redesign\\tagesbetreuungsportal\\schema\\src\\main\\resources\\changelog");

		ChangeLogParameters changeLogParameters = new ChangeLogParameters();

		DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance()
				.getParser(changeLogFile, resourceAccessor)
				.parse(changeLogFile, changeLogParameters, resourceAccessor);

		for (ChangeSet changeSet : changeLog.getChangeSets()) {
			System.out.println(changeSet);
			for (Change change : changeSet.getChanges()) {
				System.out.println("    " + change);
				if (change instanceof CreateTableChange) {
					CreateTableChange ctc = (CreateTableChange) change;
					for (ColumnConfig c : ctc.getColumns()) {
						System.out.println("        " + c.getName() + " ("
								+ c.getType() + ")"
								+ getConstraints(c.getConstraints()));
					}
				}
			}
		}
	}

	private String getConstraints(ConstraintsConfig cc) {
		String s = "";
		if (cc != null) {
			s = (!Boolean.TRUE.equals(cc.isNullable()) ? " NOT NULL" : "")
					+ (Boolean.TRUE.equals(cc.isPrimaryKey()) ? " PK" : "")
					+ (Boolean.TRUE.equals(cc.isUnique()) ? " UNIQUE" : "");
		}
		return s;
	}

}