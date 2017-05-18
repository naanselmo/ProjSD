package org.komparator.mediator.ws;

import java.io.IOException;
import java.util.Properties;

public class MediatorConfig {

	// Properties file location
	private static final String CONFIG_PROP_FILE = "/config.properties";

	// Properties object
	private static final Properties CONFIG_PROPERTIES = loadConfigProperties();

	// Properties names and default values.
	public static final String PROPERTY_SUPPLIERS_UDDI_URL = "suppliers.uddi.url";
	private static final String DEFAULT_SUPPLIERS_UDDI_URL = "http://localhost:9090/";

	public static final String PROPERTY_WS_NAME_FORMAT = "suppliers.ws.name.format";
	private static final String DEFAULT_WS_NAME_FORMAT = "T04_Supplier%";

	public static final String PROPERTY_CC_WS_URL = "cc.ws.url";
	private static final String DEFAULT_CC_WS_URL = "http://ws.sd.rnl.tecnico.ulisboa.pt:8080/cc";

	public static final String PROPERTY_REDUNDANCY_PRIMARY = "redundancy.primary";
	public static final String PROPERTY_REDUNDANCY_SECONDARY_WS_URL = "redundancy.secondary.ws.url";
	public static final String PROPERTY_REDUNDANCY_ENABLED = "redundancy.enabled";
	public static final String PROPERTY_REDUNDANCY_HEARTBEAT_PERIOD = "redundancy.heartbeat.period";
	public static final String PROPERTY_REDUNDANCY_HEARTBEAT_TIMEOUT = "redundancy.heartbeat.timeout";

	private static Properties loadConfigProperties() {
		Properties properties = new Properties();
		try {
			properties.load(MediatorConfig.class.getResourceAsStream(CONFIG_PROP_FILE));
			System.out.println("Loaded config properties:");
			System.out.println(properties);
		} catch (IOException e) {
			System.err.printf("Could not load properties file %s, falling back to defaults.", CONFIG_PROP_FILE);
			properties.setProperty(PROPERTY_SUPPLIERS_UDDI_URL, DEFAULT_SUPPLIERS_UDDI_URL);
			properties.setProperty(PROPERTY_WS_NAME_FORMAT, DEFAULT_WS_NAME_FORMAT);
			properties.setProperty(PROPERTY_CC_WS_URL, DEFAULT_CC_WS_URL);
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
