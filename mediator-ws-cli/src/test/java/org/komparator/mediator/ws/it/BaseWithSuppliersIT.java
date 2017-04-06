package org.komparator.mediator.ws.it;

import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.komparator.supplier.ws.cli.SupplierClient;

public class BaseWithSuppliersIT extends BaseIT {

	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;

	protected static SupplierClient[] supplierClients;
	protected static String[] supplierNames;

	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		testProps = new Properties();
		try {
			testProps.load(BaseIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Loaded test properties:");
			System.out.println(testProps);
		} catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}

		int supCount = Integer.parseInt(testProps.getProperty("suppliers.count"));
		String supNamePrefix = testProps.getProperty("suppliers.name.prefix");
		String supUDDIUrl = testProps.getProperty("suppliers.uddi.url");

		supplierClients = new SupplierClient[supCount];
		supplierNames = new String[supCount];
		for (int i = 1; i <= supCount; i++) {
			supplierNames[i-1] = new String(supNamePrefix + Integer.toString(i));
			supplierClients[i-1] = new SupplierClient(supUDDIUrl, supplierNames[i-1]);
		}

	}

	@AfterClass
	public static void cleanup() {
	}

}
