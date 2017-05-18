package org.komparator.mediator.ws.cli;

import java.io.IOException;
import java.util.Properties;

public class MediatorClientConfig {

	// Properties file location
	private static final String CONFIG_PROP_FILE = "/config.properties";

	// Properties object
	private static final Properties CONFIG_PROPERTIES = loadConfigProperties();

	// Properties names and default values.
	public static final String PROPERTY_REDUNDANCY_RETRIES_WAIT = "redundancy.retries.wait";
	public static final String DEFAULT_REDUNDANCY_RETRIES_WAIT = "2500";

	public static final String PROPERTY_REDUNDANCY_RETRIES_MAX = "redundancy.retries.max";
	public static final String DEFAULT_REDUNDANCY_RETRIES_MAX = "3";

	private static Properties loadConfigProperties() {
		Properties properties = new Properties();
		try {
			properties.load(MediatorClientConfig.class.getResourceAsStream(CONFIG_PROP_FILE));
			System.out.println("Loaded config properties:");
			System.out.println(properties);
		} catch (IOException e) {
			System.err.printf("Could not load properties file %s, falling back to defaults.", CONFIG_PROP_FILE);
			properties.setProperty(PROPERTY_REDUNDANCY_RETRIES_WAIT, DEFAULT_REDUNDANCY_RETRIES_WAIT);
			properties.setProperty(PROPERTY_REDUNDANCY_RETRIES_MAX, DEFAULT_REDUNDANCY_RETRIES_MAX);
			System.err.println(properties);
		}
		return properties;
	}

	public static String getProperty(String property) {
		return CONFIG_PROPERTIES.getProperty(property);
	}

	public static boolean getBooleanProperty(String property) { return Boolean.valueOf(CONFIG_PROPERTIES.getProperty(property)); }

	public static long getLongProperty(String property) { return Long.parseLong(CONFIG_PROPERTIES.getProperty(property)); }

}
