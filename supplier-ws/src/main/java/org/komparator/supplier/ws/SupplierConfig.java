package org.komparator.supplier.ws;

import java.io.IOException;
import java.util.Properties;

public class SupplierConfig {

	// Properties file location
	private static final String CONFIG_PROP_FILE = "/config.properties";

	// Properties object
	private static final Properties CONFIG_PROPERTIES = loadConfigProperties();

	// Properties names and default values.
	public static final String PROPERTY_CERTIFICATE_PATH = "certificate.path";
	public static final String PROPERTY_KEYSTORE_PATH = "keystore.path";
	public static final String PROPERTY_KEYSTORE_PASSWORD = "keystore.password";
	public static final String PROPERTY_KEYSTORE_KEY_ALIAS = "keystore.key.alias";
	public static final String PROPERTY_KEYSTORE_KEY_PASSWORD = "keystore.key.password";

	private static Properties loadConfigProperties() {
		Properties properties = new Properties();
		try {
			properties.load(SupplierConfig.class.getResourceAsStream(CONFIG_PROP_FILE));
			System.out.println("Loaded config properties:");
			System.out.println(properties);
		} catch (IOException e) {
			System.err.printf("Could not load properties file %s, falling back to defaults.", CONFIG_PROP_FILE);
			System.err.println(properties);
		}
		return properties;
	}

	public static String getProperty(String property) {
		return CONFIG_PROPERTIES.getProperty(property);
	}

}