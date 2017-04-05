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

	private static Properties loadConfigProperties() {
		Properties properties = new Properties();
		try {
			properties.load(MediatorConfig.class.getResourceAsStream(CONFIG_PROP_FILE));
			System.out.println("Loaded config properties:");
		} catch (IOException e) {
			System.out.printf("Could not load properties file %s, falling back to defaults.", CONFIG_PROP_FILE);
			properties.setProperty(PROPERTY_SUPPLIERS_UDDI_URL, DEFAULT_SUPPLIERS_UDDI_URL);
			properties.setProperty(PROPERTY_WS_NAME_FORMAT, DEFAULT_WS_NAME_FORMAT);
		}
		System.out.println(properties);
		return properties;
	}

	public static String getProperty(String property) {
		return CONFIG_PROPERTIES.getProperty(property);
	}

}
