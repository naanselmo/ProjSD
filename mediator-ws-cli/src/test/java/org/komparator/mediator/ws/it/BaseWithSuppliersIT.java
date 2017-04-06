package org.komparator.mediator.ws.it;

import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.komparator.supplier.ws.cli.SupplierClient;

public class BaseWithSuppliersIT extends BaseIT {

	protected static SupplierClient[] supplierClients;
	protected static String[] supplierNames;

	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		BaseIT.oneTimeSetup();

		int supCount = Integer.parseInt(BaseIT.testProps.getProperty("suppliers.count"));
		String supNamePrefix = BaseIT.testProps.getProperty("suppliers.name.prefix");
		String supUDDIUrl = BaseIT.testProps.getProperty("suppliers.uddi.url");

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
