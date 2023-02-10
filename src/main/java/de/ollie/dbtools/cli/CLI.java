package de.ollie.dbtools.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.JCommander.Builder;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import lombok.Getter;

/**
 * The anchor class for the dbtools CLI tooling.
 *
 * @author ollie (16.06.2020)
 */
public class CLI {

	static Logger log = LogManager.getLogger(CLI.class);

	@Getter
	@Parameters(commandDescription = "A main command for the common options.")
	public static class MainParameters {

		@Parameter(names = "--help", description = "Prints this help info to the console.")
		private boolean help = false;

	}

	public static interface Command {

		String getCommand();

		int run(MainParameters mainCommand);

	}

	private Map<String, Command> commands = new HashMap<>();

	public static void main(final String[] args) {
		log.info("starting CLI");
		System.exit(new CLI().start(args));
	}

	public int start(String... args) {
		MainParameters mainParameters = new MainParameters();
		Builder builder = JCommander.newBuilder() //
				.addObject(mainParameters);
		addCommands(builder);
		JCommander jc = builder.build();
		try {
			jc.parse(args);
		} catch (Exception e) {
			log.error("error while parsing command line: " + e.getMessage());
			return 1;
		}
		if (mainParameters.isHelp()) {
			jc.usage();
		} else {
			return runCommand(this.commands.get(jc.getParsedCommand()), mainParameters);
		}
		return 0;
	}

	protected void addCommands(Builder builder) {
		addCommands(builder, createCommand(new CopyCommand()));
		addCommands(builder, createCommand(new ListCommand()));
	}

	protected void addCommands(Builder builder, Command... commands) {
		Arrays.asList(commands).forEach(command -> builder.addCommand(command.getCommand(), command));
	}

	protected <T extends Command> T createCommand(T t) {
		this.commands.put(t.getCommand(), t);
		return t;
	}

	private int runCommand(Command selectedCommand, MainParameters mainParameters) {
		int returnCode = 0;
		if (selectedCommand != null) {
			returnCode = selectedCommand.run(mainParameters);
		}
		log.info("program finished with code: " + returnCode);
		return returnCode;
	}

}