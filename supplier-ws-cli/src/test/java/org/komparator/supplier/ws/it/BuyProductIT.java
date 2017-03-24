package org.komparator.supplier.ws.it;

import org.junit.*;
import org.komparator.supplier.ws.*;

import static org.junit.Assert.*;

/**
 * Test suite
 */
public class BuyProductIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() {
	}

	@AfterClass
	public static void oneTimeTearDown() {
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() throws BadProductId_Exception, BadProduct_Exception {
		// clear remote service state before each test
		client.clear();

		// fill-in test products
		// (since buyProduct reads and writes the initialization below
		// has to be done every time for each tests in this suite)
		{
			ProductView product = new ProductView();
			product.setId("X1");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball");
			product.setPrice(20);
			product.setQuantity(20);
			client.createProduct(product);
		}
	}

	@After
	public void tearDown() {
		// clear remote service state after each test
		client.clear();
	}

	// tests
	// assertEquals(expected, actual);

	// public String buyProduct(String productId, int quantity)
	// throws BadProductId_Exception, BadQuantity_Exception,
	// InsufficientQuantity_Exception {

	// bad input tests

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNullProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(null, 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductEmptyProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("", 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductWhitespaceProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(" ", 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductWhitespacesProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("              ", 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductTabProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\t", 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductWindowsNewLineProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\r\n", 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductUnixNewLineProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\n", 1);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductMacNewLineProductIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\r", 1);
	}

	@Test(expected = BadQuantity_Exception.class)
	public void buyProductNegativeQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", -1000);
	}

	@Test(expected = BadQuantity_Exception.class)
	public void buyProductZeroQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", 0);
	}

	@Test(expected = BadProductId_Exception.class)
	public void buyProductBothArgumentsInvalidTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(null, -1000);
	}

	// main tests

	@Test
	public void buyProductOneQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		String purchaseId = client.buyProduct("X1", 1);
		assertNotNull(purchaseId);
		ProductView product = client.getProduct("X1");
		assertEquals(9, product.getQuantity());
		// Check if the purchaseId was added to the purchase history.
		assertEquals(true, client.listPurchases().stream().anyMatch(purchaseView -> purchaseId.equals(purchaseView.getId())));
	}

	@Test
	public void buyProductMaxQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		String purchaseId = client.buyProduct("X1", 10);
		assertNotNull(purchaseId);
		ProductView product = client.getProduct("X1");
		assertEquals(0, product.getQuantity());
		// Check if the purchaseId was added to the purchase history.
		assertEquals(true, client.listPurchases().stream().anyMatch(purchaseView -> purchaseId.equals(purchaseView.getId())));
	}

	@Test
	public void buyProductAnotherOneQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		String purchaseId = client.buyProduct("Y2", 1);
		assertNotNull(purchaseId);
		ProductView product = client.getProduct("Y2");
		assertEquals(19, product.getQuantity());
		// Check if the purchaseId was added to the purchase history.
		assertEquals(true, client.listPurchases().stream().anyMatch(purchaseView -> purchaseId.equals(purchaseView.getId())));
	}

	@Test
	public void buyProductAnotherMaxQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		String purchaseId = client.buyProduct("Y2", 20);
		assertNotNull(purchaseId);
		ProductView product = client.getProduct("Y2");
		assertEquals(0, product.getQuantity());
		// Check if the purchaseId was added to the purchase history.
		assertEquals(true, client.listPurchases().stream().anyMatch(purchaseView -> purchaseId.equals(purchaseView.getId())));
	}

	@Test(expected = InsufficientQuantity_Exception.class)
	public void buyProductInsufficientQuantityTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1", 15);
	}

	@Test
	public void buyProductCaseSensitiveProductTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		try {
			client.buyProduct("x1", 1);
			fail();
		} catch (BadProductId_Exception ignored) {
		}
		ProductView product = client.getProduct("X1");
		assertEquals(10, product.getQuantity());
	}

	@Test
	public void buyProductProductNotExistsTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		try {
			client.buyProduct("A0", 1);
			fail();
		} catch (BadProductId_Exception ignored) {
		}
	}

}
