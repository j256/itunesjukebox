package com.j256.common.main;

import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Class to parse main's arguments.
 * 
 * @author graywatson
 */
public class ArgumentParser {

	public static final int DEFAULT_JMX_PORT = 5256;

	private static final String HELP_OPTION = "h";
	private static final String HELP_LONG_OPTION = "help";
	private static final String JMX_PORT_OPTION = "j";
	private static final String JMX_PORT_LONG_OPTION = "jmx-port";
	/**
	 * Set this to not call System.exit on error but rather throw an exception. It does not cause the main thread to
	 * wait for shutdown however.
	 */
	private static final String PROPERTIES_FILE_OPTION = "p";
	private static final String PROPERTIES_FILE_LONG_OPTION = "prop-file";
	private static final String PROPERTY_OPTION = "P";
	private static final String PROPERTY_LONG_OPTION = "property";
	/**
	 * Use this parameter to not wait for the context to close but exit immediately.
	 */
	private static final String USAGE_OPTION = "u";
	private static final String USAGE_LONG_OPTION = "usage";

	private Options cmdLineOptions;
	private CommandLine cmdLine;

	/**
	 * Parse the arguments from the command line.
	 * 
	 * @return True if worked otherwise false if parse error.
	 */
	public boolean parseArguments(String[] args) {

		if (args == null) {
			usage("No arguments specified");
			return false;
		}

		cmdLineOptions = getCommandOptions();
		try {
			// parse the command line arguments
			cmdLine = new GnuParser().parse(cmdLineOptions, args);
		} catch (ParseException exp) {
			usage("Parsing failed.  Reason: " + exp.getMessage());
			return false;
		}

		// spit out our help message
		if (cmdLine.hasOption(HELP_OPTION) || cmdLine.hasOption(USAGE_OPTION)) {
			help();
			return false;
		}

		return true;
	}

	/**
	 * Output our usage message.
	 */
	public void usage(String message) {
		assert message != null : "message must be specified";
		HelpFormatter formatter = new HelpFormatter();
		PrintWriter pw = new PrintWriter(System.err);
		pw.println(message);
		pw.println("Use -" + USAGE_OPTION + " option for usage information");
		formatter.printUsage(pw, 120, "java " + Main.class.getName(), getCommandOptions());
		pw.flush();
	}

	public String[] getPropertiesFiles() {
		String[] propertiesFiles = cmdLine.getOptionValues(PROPERTIES_FILE_OPTION);
		if (propertiesFiles == null) {
			return new String[0];
		} else {
			return propertiesFiles;
		}
	}

	public String[] getProperties() {
		String[] properties = cmdLine.getOptionValues(PROPERTY_OPTION);
		if (properties == null) {
			return new String[0];
		} else {
			return properties;
		}
	}

	public int getJmxPort() {
		if (cmdLine.hasOption(JMX_PORT_OPTION)) {
			return Integer.parseInt(cmdLine.getOptionValue(JMX_PORT_OPTION));
		} else {
			return DEFAULT_JMX_PORT;
		}
	}

	private void help() {
		new HelpFormatter().printHelp("java " + Main.class.getName(), getCommandOptions());
	}

	private Options getCommandOptions() {
		if (cmdLineOptions != null) {
			return cmdLineOptions;
		}

		// NOTE: nothing should be required here.
		cmdLineOptions = new Options();

		OptionBuilder.withArgName("port");
		OptionBuilder.withLongOpt(JMX_PORT_LONG_OPTION);
		OptionBuilder.hasArgs();
		OptionBuilder.withDescription("jmx port");
		cmdLineOptions.addOption(OptionBuilder.create(JMX_PORT_OPTION));

		OptionBuilder.withArgName("file");
		OptionBuilder.withLongOpt(PROPERTIES_FILE_LONG_OPTION);
		OptionBuilder.hasArgs();
		OptionBuilder.withDescription("Properties file(s).");
		cmdLineOptions.addOption(OptionBuilder.create(PROPERTIES_FILE_OPTION));

		OptionBuilder.withArgName("property");
		OptionBuilder.withLongOpt(PROPERTY_LONG_OPTION);
		OptionBuilder.hasArgs();
		OptionBuilder.withDescription("Property a=b");
		cmdLineOptions.addOption(OptionBuilder.create(PROPERTY_OPTION));

		cmdLineOptions.addOption(HELP_OPTION, HELP_LONG_OPTION, false, "Display this usage message.");
		cmdLineOptions.addOption(USAGE_OPTION, USAGE_LONG_OPTION, false, "Display this usage message.");
		return cmdLineOptions;
	}
}
