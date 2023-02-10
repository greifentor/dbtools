package de.ollie.dbtools.cli;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.ollie.dbtools.cli.CLI.Command;
import de.ollie.dbtools.cli.CLI.MainParameters;
import de.ollie.dbtools.copier.DataCopier;
import de.ollie.dbtools.utils.StatementBuilder;

/**
 * An implementation of the command interface for table data copies.
 *
 * @author ollie (16.06.2020)
 */
@Parameters(commandDescription = "Copies contents of databases from one database to another.")
public class ListCommand implements Command {

	static Logger log = LogManager.getLogger(ListCommand.class);

	@Parameter(names = {
			"--sourceDriver" }, required = true, description = "The qualified class name of the JDBC driver for the "
					+ "source database.")
	private String sourceDriverClassName;

	@Parameter(names = { "--sourceURL" }, required = true, description = "The URL of the source database.")
	private String sourceURL;

	@Parameter(names = { "--sourceUser" }, required = true, description = "The name of the user for source database "
			+ "access.")
	private String sourceUserName;

	@Parameter(names = {
			"--sourcePassword" }, required = false, description = "The password of the user for source database "
					+ "access.")
	private String sourceUserPassword = "";

	@Parameter(names = { "--tableNamePattern" }, required = false, description = "A pattern for table names whose data "
			+ "are to copy (set '*' for variable start or end of the table names). More than one pattern could be "
			+ "defined comma separated. In this case at least one pattern matching is copying the table data (default "
			+ "is '*'; this will match all table names).")
	private String tableNamePattern = "*";

	@Override
	public String getCommand() {
		return "list";
	}

	@Override
	public int run(MainParameters mainCommand) {
		try {
			List<String> includeTableNamePatterns = getIncludes(tableNamePattern);
			Connection sourceConnection = getConnection(sourceDriverClassName, sourceURL, sourceUserName,
					sourceUserPassword);
			new DataCopier(new StatementBuilder()).list(sourceConnection, includeTableNamePatterns);
		} catch (Exception e) {
			log.error("error while copying data: " + e.getMessage(), e);
		}
		return 0;
	}

	private List<String> getIncludes(String includeTableNamePatterns) {
		if ((includeTableNamePatterns == null) || includeTableNamePatterns.isEmpty()) {
			return Arrays.asList("*");
		}
		return Arrays.asList(StringUtils.split(includeTableNamePatterns, ','));
	}

	private Connection getConnection(String driverClassName, String url, String userName, String userPassword)
			throws ClassNotFoundException, SQLException {
		Class.forName(driverClassName);
		return DriverManager.getConnection(url, userName, userPassword);
	}

}