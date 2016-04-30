package com.j256.common.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.j256.simplewebframework.logger.Logger;
import com.j256.simplewebframework.logger.LoggerFactory;

/**
 * Main program which parses the command line arguments and starts up Jmx (if necessary) and spring stuff.
 * 
 * @author graywatson
 */
public class Main {

	private static final String JMX_SERVER_PORT_SYSTEM_PROPERTY = "common.main.jmxServer.registryPort";
	private GenericXmlApplicationContext mainContext;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static Main myself;
	private static final String LAUNCH_CONFIG_FILENAME = "com/j256/run.xml";

	private String[] args;
	private String[] configPaths;

	public static void main(String[] args) throws Exception {
		mainWithReturn(args);
	}

	public static boolean mainWithReturn(String[] args) throws Exception {
		myself = new Main();
		return myself.instanceMain(args);
	}

	/**
	 * Little hack for JMX.
	 */
	public static Main getInstance() {
		return myself;
	}

	/**
	 * This is the main that is called once it creates an instance variable.
	 */
	public boolean instanceMain(String[] newArgs) throws Exception {

		// for the getter
		this.args = newArgs;
		ArgumentParser argumentParser = new ArgumentParser();
		if (!argumentParser.parseArguments(this.args)) {
			return false;
		}

		System.setProperty(JMX_SERVER_PORT_SYSTEM_PROPERTY, Integer.toString(argumentParser.getJmxPort()));

		// handle overriding system properties
		for (String property : argumentParser.getProperties()) {
			String[] propParts = property.split("=", 2);
			if (propParts.length != 2) {
				argumentParser.usage("Problems parsing property '" + property + "'.  Should be in a=b form.");
				return false;
			}
			System.setProperty(propParts[0], propParts[1]);
		}

		// handle overriding system properties
		for (String propFileName : argumentParser.getPropertiesFiles()) {
			File propFile = new File(propFileName);
			try {
				InputStream inputStream = new FileInputStream(propFile);
				System.getProperties().load(inputStream);
				inputStream.close();
			} catch (IOException e) {
				argumentParser.usage("Problems loading properties file '" + propFileName + "': " + e.getMessage());
				return false;
			}
		}

		runContext(argumentParser);
		return true;
	}

	/**
	 * Get our command-line args.
	 */
	public String[] getArgs() {
		return args;
	}

	/**
	 * Get the config paths we are using.
	 */
	public String[] getConfigPaths() {
		return configPaths;
	}

	/**
	 * Load and run our context.
	 */
	private void runContext(ArgumentParser argumentParser) throws Exception {

		List<Resource> resourceList = new ArrayList<Resource>();

		Enumeration<URL> launcherConfigs = getClass().getClassLoader().getResources(LAUNCH_CONFIG_FILENAME);
		if (!launcherConfigs.hasMoreElements()) {
			throw new IllegalStateException("Cannot find any files matching: " + LAUNCH_CONFIG_FILENAME);
		}
		while (launcherConfigs.hasMoreElements()) {
			URL configUrl = launcherConfigs.nextElement();
			resourceList.add(new UrlResource(configUrl));
		}
		List<String> configList = new ArrayList<String>(resourceList.size());
		for (Resource resource : resourceList) {
			configList.add(resource.getURL().getPath());
		}
		configPaths = configList.toArray(new String[configList.size()]);
		logger.info("Xml Files: " + Arrays.toString(configPaths));

		try {
			try {
				mainContext = new GenericXmlApplicationContext(resourceList.toArray(new Resource[resourceList.size()]));
				mainContext.registerShutdownHook();
			} catch (Throwable th) {
				throwOrExit(argumentParser, th, "Unable to load the main context", 1, true);
			}
			logger.info("loaded the application context");

			MainJmx mainJmx = mainContext.getBean("mainJmx", MainJmx.class);
			mainJmx.waitForShutdown();
			logger.info("shutting down");
		} finally {
			if (mainContext != null) {
				// NOTE: this removes the shutdown hook so we don't need to do it
				mainContext.close();
				mainContext = null;
			}
		}
	}

	/**
	 * Routine to either throw or exit depending on our command line arguments.
	 */
	private void throwOrExit(ArgumentParser argumentParser, Throwable th, String message, int exitCode,
			boolean printStack) throws IllegalStateException {
		if (message == null) {
			// do nothing, just exit
		} else if (th == null) {
			System.err.println("Error.  " + message);
		} else {
			System.err.println("Error.  " + message + ": " + th.getMessage());
			if (printStack) {
				th.printStackTrace();
			}
		}
		System.exit(exitCode);
	}
}
