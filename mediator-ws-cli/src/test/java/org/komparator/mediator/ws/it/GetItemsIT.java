package org.komparator.mediator.ws.it;

import java.util.List;

import org.komparator.mediator.ws.*;
import org.komparator.supplier.ws.*;
import org.komparator.supplier.ws.cli.SupplierClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * Test suite
 */
public class GetItemsIT extends BaseWithSuppliersIT {

    @Before
    public void setUp() throws BadProductId_Exception, BadProduct_Exception {
        mediatorClient.clear();
        {
            ProductView product = new ProductView();
    		product.setId("TP");
    		product.setDesc("TestProd");
    		product.setPrice(1);
    		product.setQuantity(1);
            for ( SupplierClient client : supplierClients ) {
                client.createProduct(product);
            }
        }
	    // fill-in test products
	    // (since getItems is read-only the initialization below
	    // can be done once for all tests in this suite)
	    {
		    ProductView prod = new ProductView();
		    prod.setId("p1");
		    prod.setDesc("AAA bateries (pack of 3)");
		    prod.setPrice(3);
		    prod.setQuantity(10);
		    supplierClients[0].createProduct(prod);
	    }

	    {
		    ProductView prod = new ProductView();
		    prod.setId("p1");
		    prod.setDesc("3batteries");
		    prod.setPrice(4);
		    prod.setQuantity(10);
		    supplierClients[1].createProduct(prod);
	    }

	    {
		    ProductView prod = new ProductView();
		    prod.setId("p2");
		    prod.setDesc("AAA bateries (pack of 10)");
		    prod.setPrice(9);
		    prod.setQuantity(20);
		    supplierClients[0].createProduct(prod);
	    }

	    {
		    ProductView prod = new ProductView();
		    prod.setId("p2");
		    prod.setDesc("10x AAA battery");
		    prod.setPrice(8);
		    prod.setQuantity(20);
		    supplierClients[1].createProduct(prod);
	    }

	    {
		    ProductView prod = new ProductView();
		    prod.setId("p3");
		    prod.setDesc("Digital Multimeter");
		    prod.setPrice(15);
		    prod.setQuantity(5);
		    supplierClients[0].createProduct(prod);
	    }
    }

    @After
    public void tearDown() {
        mediatorClient.clear();
	    // even though mediator clear should have cleared suppliers, clear them
	    // explicitly after use
	    supplierClients[0].clear();
	    supplierClients[1].clear();
    }

    // Input tests: test for bad input

    @Test(expected = InvalidItemId_Exception.class)
    public void nullNameTest() throws InvalidItemId_Exception {
        mediatorClient.getItems(null);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void emptyName() throws InvalidItemId_Exception {
        mediatorClient.getItems("");
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void whitespaceNameTest() throws InvalidItemId_Exception {
        mediatorClient.getItems(" ");
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void whitespaceTabNameTest() throws InvalidItemId_Exception {
        mediatorClient.getItems("\t");
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void containsSpacesNameTest() throws InvalidItemId_Exception {
        mediatorClient.getItems("Te st");
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void containsSymbolsNameTest() throws InvalidItemId_Exception {
        mediatorClient.getItems("T$st");
    }

    // Behavioral tests: check if it actually does what we want

    @Test
    public void differentCaseNameTest() throws InvalidItemId_Exception {
        assertTrue(mediatorClient.getItems("tp").isEmpty());
        assertTrue(mediatorClient.getItems("Tp").isEmpty());
        assertTrue(mediatorClient.getItems("tP").isEmpty());
    }

    @Test
    public void differentCaseNameNoConflictTest() throws InvalidItemId_Exception, BadProductId_Exception, BadProduct_Exception {
        {
            ProductView product = new ProductView();
    		product.setId("tp");
    		product.setDesc("TestProdLowerCase");
    		product.setPrice(1);
    		product.setQuantity(1);
            for ( SupplierClient client : supplierClients ) {
                client.createProduct(product);
            }
        }

        List<ItemView> resultsUpper = mediatorClient.getItems("TP");
        assertEquals(supplierClients.length, resultsUpper.size());
        for (ItemView item : resultsUpper) {
            assertEquals("TP", item.getItemId().getProductId());
            assertEquals("TestProd", item.getDesc());
        }

        List<ItemView> resultsLower = mediatorClient.getItems("tp");
        assertEquals(supplierClients.length, resultsLower.size());
        for (ItemView item : resultsLower) {
            assertEquals("tp", item.getItemId().getProductId());
            assertEquals("TestProdLowerCase", item.getDesc());
        }
    }

    @Test
    public void noItemsTest() throws InvalidItemId_Exception {
        assertTrue(mediatorClient.getItems("FAKE").isEmpty());
    }

    @Test
    public void noQuantityItemTest() throws InvalidItemId_Exception, BadProductId_Exception, BadProduct_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
        {
            ProductView product = new ProductView();
    		product.setId("TPSS");
    		product.setDesc("TestProdSingleSup");
    		product.setPrice(1);
    		product.setQuantity(1);
            supplierClients[0].createProduct(product);
            supplierClients[0].buyProduct("TPSS", 1);
        }
        List<ItemView> results = mediatorClient.getItems("TPSS");
        assertEquals(1, results.size());
    }

    @Test
    public void singleItemSingleSupplierTest() throws InvalidItemId_Exception, BadProductId_Exception, BadProduct_Exception {
        {
            ProductView product = new ProductView();
    		product.setId("TPSS");
    		product.setDesc("TestProdSingleSup");
    		product.setPrice(1);
    		product.setQuantity(1);
            supplierClients[0].createProduct(product);
        }
        List<ItemView> results = mediatorClient.getItems("TPSS");
        assertEquals(1, results.size());
        assertEquals("TPSS", results.get(0).getItemId().getProductId());
        assertEquals(supplierNames[0], results.get(0).getItemId().getSupplierId());
        assertEquals("TestProdSingleSup", results.get(0).getDesc());
        assertEquals(1, results.get(0).getPrice());
    }

    @Test
    public void multipleItemsDifferentSuppliersTest() throws InvalidItemId_Exception {
        List<ItemView> results = mediatorClient.getItems("TP");
        assertEquals(results.size(), supplierClients.length);
        for (int i = 0; i < supplierClients.length; i++) {
            assertEquals("TP", results.get(i).getItemId().getProductId());
            assertEquals("TestProd", results.get(i).getDesc());
            assertEquals(1, results.get(i).getPrice());
        }
    }

    @Test
    public void priceOrderTest() throws InvalidItemId_Exception, BadProductId_Exception, BadProduct_Exception {
        {
            ProductView product = new ProductView();
    		product.setId("TP1");
    		product.setDesc("TestProduct");
    		product.setPrice(10);
    		product.setQuantity(1);
            supplierClients[0].createProduct(product);
        }
        {
            ProductView product = new ProductView();
    		product.setId("TP1");
    		product.setDesc("TestProduct");
    		product.setPrice(1);
    		product.setQuantity(1);
            supplierClients[1].createProduct(product);
        }
        {
            ProductView product = new ProductView();
    		product.setId("TP2");
    		product.setDesc("TestProduct");
    		product.setPrice(1);
    		product.setQuantity(1);
            supplierClients[0].createProduct(product);
        }
        {
            ProductView product = new ProductView();
    		product.setId("TP2");
    		product.setDesc("TestProduct");
    		product.setPrice(10);
    		product.setQuantity(1);
            supplierClients[1].createProduct(product);
        }

        for (String input : new String[]{"TP1", "TP2"}) {
            List<ItemView> results = mediatorClient.getItems(input);
            for (int i = 1; i < results.size(); i++) {
                assertTrue(results.get(i-1).getPrice() <= results.get(i).getPrice());
            }
        }
    }

    // Teacher

	// ------ WSDL Faults (error cases) ------

	@Test(expected = InvalidItemId_Exception.class)
	public void testNullItem() throws InvalidItemId_Exception {
		mediatorClient.getItems(null);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void testEmptyItemId() throws InvalidItemId_Exception {
		mediatorClient.getItems("");
	}

	// not used for evaluation
	// @Test(expected = InvalidItemId_Exception.class)
	public void testSpace() throws InvalidItemId_Exception {
		mediatorClient.getItems(" ");
	}

	// not used for evaluation
	// @Test(expected = InvalidItemId_Exception.class)
	public void testTab() throws InvalidItemId_Exception {
		mediatorClient.getItems("\t");
	}

	// not used for evaluation
	// @Test(expected = InvalidItemId_Exception.class)
	public void testNewline() throws InvalidItemId_Exception {
		mediatorClient.getItems("\n");
	}

	// not used for evaluation
	// @Test(expected = InvalidItemId_Exception.class)
	public void testDot() throws InvalidItemId_Exception {
		mediatorClient.getItems(".");
	}

	// ------ Normal cases ------

	@Test
	public void testInexistingItem1() throws InvalidItemId_Exception {
		List<ItemView> items = mediatorClient.getItems("p4");
		// desired output is empty, but null is also accepted (case not
		// specified in project statement)
		assertTrue(items == null || items.size() == 0);
	}

	@Test
	public void testInexistingItem2() throws InvalidItemId_Exception {
		// There is no such item, but there is an item with the description
		// "3batteries"
		List<ItemView> items = mediatorClient.getItems("3batteries");
		// desired output is empty, but null is also accepted (case not
		// specified in project statement)
		assertTrue(items == null || items.size() == 0);
	}

	@Test
	public void testSingleExistingItem() throws InvalidItemId_Exception {
		// Testing all item properties only in this test
		List<ItemView> items = mediatorClient.getItems("p3");
		assertEquals(1, items.size());

		assertEquals("p3", items.get(0).getItemId().getProductId());
		assertEquals("Digital Multimeter", items.get(0).getDesc());
		assertEquals(15, items.get(0).getPrice());
		assertEquals(supplierNames[0], items.get(0).getItemId().getSupplierId());
	}

	@Test
	public void testMultipleExistingItems() throws InvalidItemId_Exception {
		List<ItemView> items = mediatorClient.getItems("p1");
		assertEquals(2, items.size());
	}

	@Test
	public void testOrder1() throws InvalidItemId_Exception {
		// First supplier has the cheapest price
		List<ItemView> items = mediatorClient.getItems("p1");
		assertEquals(2, items.size());
		assertTrue(items.get(0).getPrice() <= items.get(1).getPrice());
	}

	@Test
	public void testOrder2() throws InvalidItemId_Exception {
		// First supplier has the most expensive price
		List<ItemView> items = mediatorClient.getItems("p2");
		assertEquals(2, items.size());
		assertTrue(items.get(0).getPrice() <= items.get(1).getPrice());
	}

}
