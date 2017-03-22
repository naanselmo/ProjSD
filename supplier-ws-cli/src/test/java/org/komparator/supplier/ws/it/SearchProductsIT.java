package org.komparator.supplier.ws.it;

import org.junit.*;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.ProductView;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test suite
 */
public class SearchProductsIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception {
		// clear remote service state before all tests
		client.clear();

		// fill-in test products
		// (since searchProducts is read-only the initialization below
		// can be done once for all tests in this suite)
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
		{
			ProductView product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}
	}

	@AfterClass
	public static void oneTimeTearDown() {
		// clear remote service state after all tests
		client.clear();
	}

	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	// tests
	// assertEquals(expected, actual);

	// public List<ProductView> searchProducts(String descText) throws
	// BadText_Exception

	// bad input tests

	@Test(expected = BadText_Exception.class)
	public void searchProductsNullTest() throws BadText_Exception {
		client.searchProducts(null);
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsEmptyTest() throws BadText_Exception {
		client.searchProducts("");
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsWhitespaceTest() throws BadText_Exception {
		client.searchProducts(" ");
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsWhitespacesTest() throws BadText_Exception {
		client.searchProducts("              ");
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsTabTest() throws BadText_Exception {
		client.searchProducts("\t");
	}

	@Test(expected = BadText_Exception.class)
	public void getProductWindowsNewLineTest() throws BadText_Exception {
		client.searchProducts("\r\n");
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsUnixNewlineTest() throws BadText_Exception {
		client.searchProducts("\n");
	}

	@Test(expected = BadText_Exception.class)
	public void searchProductsMacNewlineTest() throws BadText_Exception {
		client.searchProducts("\r");
	}

	// main tests

	@Test
	public void searchProductsSingleProductExistsTest() throws BadText_Exception {
		List<ProductView> products = client.searchProducts("Basketball");
		assertEquals(1, products.size());
		ProductView product = products.get(0);
		assertEquals("X1", product.getId());
		assertEquals(10, product.getPrice());
		assertEquals(10, product.getQuantity());
		assertEquals("Basketball", product.getDesc());
	}

	@Test
	public void searchProductsSingleProductCaseInsensitiveExistsTest() throws BadText_Exception {
		// products search should be case insensitive,
		// so a product with the description "Basketball" should be found by the search text "BasKeTbaLL"
		List<ProductView> products = client.searchProducts("BasKeTbaLL");
		assertEquals(1, products.size());
		ProductView product = products.get(0);
		assertEquals("X1", product.getId());
		assertEquals(10, product.getPrice());
		assertEquals(10, product.getQuantity());
		assertEquals(true, product.getDesc().toLowerCase().contains("basketball"));
	}

	@Test
	public void searchProductsMultipleProductExistsTest() throws BadText_Exception {
		List<ProductView> products = client.searchProducts("ball");
		assertEquals(3, products.size());
		for (ProductView productView : products) {
			assertEquals(true, productView.getDesc().contains("ball"));
		}
	}

	@Test
	public void searchProductsMultipleProductCaseInsensitiveExistsTest() throws BadText_Exception {
		// products search should be case insensitive,
		// so the products with "ball" in their description should be found by the search text "BaLL"
		List<ProductView> products = client.searchProducts("BaLL");
		assertEquals(3, products.size());
		for (ProductView productView : products) {
			assertEquals(true, productView.getDesc().toLowerCase().contains("ball"));
		}
	}

	@Test
	public void searchProductsNotExistsTest() throws BadText_Exception {
		List<ProductView> products = client.searchProducts("Golf ball");
		// products list shouldn't be null when the description is not found
		assertNotNull(products);
		// should be an empty list instead
		assertEquals(0, products.size());
	}

}
