package org.komparator.security;

import java.util.Properties;

public class SecurityConfig {

	// Properties file location
	private static final String CONFIG_PROP_FILE = "/security.properties";

	// Properties object
	private static final Properties CONFIG_PROPERTIES = loadConfigProperties();

	// CA certificate path
	public static final String CA_CERTIFICATE_PATH = "/ca.cer";

	// Properties names and default values.
	public static final String PROPERTY_CA_ID = "ca.id";
	public static final String PROPERTY_CA_WS_URL = "ca.ws.url";
	public static final String PROPERTY_CERTIFICATE_PATH = "certificate.path";
	public static final String PROPERTY_KEYSTORE_PATH = "keystore.path";
	public static final String PROPERTY_KEYSTORE_PASSWORD = "keystore.password";
	public static final String PROPERTY_KEYSTORE_KEY_ALIAS = "keystore.key.alias";
	public static final String PROPERTY_KEYSTORE_KEY_PASSWORD = "keystore.key.password";

	private static Properties loadConfigProperties() {
		try {
			Properties properties = new Properties();
			properties.load(SecurityConfig.class.getResourceAsStream(CONFIG_PROP_FILE));
			System.out.println("Loaded security config properties:");
			System.out.println(properties);
			return properties;
		} catch (Exception e) {
			System.err.printf("Could not load security properties file %s.", CONFIG_PROP_FILE);
			System.exit(-1);
		}
		return null;
	}

	public static String getProperty(String property) {
		return CONFIG_PROPERTIES.getProperty(property);
	}

}
