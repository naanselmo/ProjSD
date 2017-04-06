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
public class SearchItemsIT extends BaseWithSuppliersIT {

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
    }

    @After
    public void tearDown() {
        mediatorClient.clear();
    }

    // Input tests: test for bad input

    @Test(expected = InvalidText_Exception.class)
    public void nullDescriptionTest() throws InvalidText_Exception {
        mediatorClient.searchItems(null);
    }

    @Test(expected = InvalidText_Exception.class)
    public void whitespaceDescriptionTest() throws InvalidText_Exception {
        mediatorClient.searchItems(" ");
    }

    @Test(expected = InvalidText_Exception.class)
    public void whitespaceTabDescriptionTest() throws InvalidText_Exception {
        mediatorClient.searchItems("\t");
    }

    @Test
    public void containsSpacesDescriptionTest() throws InvalidText_Exception {
        mediatorClient.searchItems("Test Product");
    }

    @Test
    public void containsSymbolsDescriptionTest() throws InvalidText_Exception {
        mediatorClient.searchItems("T$st");
    }

    // Behavioral tests: check if it actually does what we want

    @Test
    public void differentCaseDescriptionTest() throws InvalidText_Exception {
        assertTrue(mediatorClient.searchItems("testProd").isEmpty());
        assertTrue(mediatorClient.searchItems("Testprod").isEmpty());
        assertTrue(mediatorClient.searchItems("testprod").isEmpty());
    }

    @Test
    public void differentCaseDescriptionNoConflictTest() throws InvalidText_Exception, BadProductId_Exception, BadProduct_Exception {
        {
            ProductView product = new ProductView();
    		product.setId("tp");
    		product.setDesc("testprod");
    		product.setPrice(1);
    		product.setQuantity(1);
            for ( SupplierClient client : supplierClients ) {
                client.createProduct(product);
            }
        }

        List<ItemView> resultsUpper = mediatorClient.searchItems("TP");
        assertEquals(supplierClients.length, resultsUpper.size());
        for (ItemView item : resultsUpper) {
            assertEquals("TP", item.getItemId().getProductId());
            assertEquals("TestProd", item.getDesc());
        }

        List<ItemView> resultsLower = mediatorClient.searchItems("tp");
        assertEquals(supplierClients.length, resultsLower.size());
        for (ItemView item : resultsLower) {
            assertEquals("tp", item.getItemId().getProductId());
            assertEquals("testprod", item.getDesc());
        }
    }

    @Test
    public void noItemsTest() throws InvalidText_Exception {
        assertTrue(mediatorClient.searchItems("FAKE").isEmpty());
    }

    @Test
    public void noQuantityItemTest() throws InvalidText_Exception, BadProductId_Exception, BadProduct_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
        {
            ProductView product = new ProductView();
    		product.setId("TPSS");
    		product.setDesc("TestProdSingleSup");
    		product.setPrice(1);
    		product.setQuantity(1);
            supplierClients[0].createProduct(product);
            supplierClients[0].buyProduct("TPSS", 1);
        }
        List<ItemView> results = mediatorClient.searchItems("TPSS");
        assertEquals(1, results.size());
    }

    @Test
    public void singleItemSingleSupplierTest() throws InvalidText_Exception, BadProductId_Exception, BadProduct_Exception {
        {
            ProductView product = new ProductView();
    		product.setId("TPSS");
    		product.setDesc("TestProdSingleSup");
    		product.setPrice(1);
    		product.setQuantity(1);
            supplierClients[0].createProduct(product);
        }
        List<ItemView> results = mediatorClient.searchItems("TPSS");
        assertEquals(1, results.size());
        assertEquals("TPSS", results.get(0).getItemId().getProductId());
        assertEquals(supplierNames[0], results.get(0).getItemId().getSupplierId());
        assertEquals("ProdSingle", results.get(0).getDesc());
        assertEquals(1, results.get(0).getPrice());
    }

    @Test
    public void multipleItemsDifferentSuppliersTest() throws InvalidText_Exception {
        List<ItemView> results = mediatorClient.searchItems("TP");
        assertEquals(results.size(), supplierClients.length);
        for (int i = 0; i < supplierClients.length; i++) {
            assertEquals("TP", results.get(i).getItemId().getProductId());
            assertEquals(supplierNames[i], results.get(i).getItemId().getSupplierId());
            assertEquals("TestProd", results.get(i).getDesc());
            assertEquals(1, results.get(i).getPrice());
        }
    }

    @Test
    public void idOrderTest() throws InvalidText_Exception, BadProductId_Exception, BadProduct_Exception {
        {
            ProductView product = new ProductView();
    		product.setId("aBbBb");
    		product.setDesc("TestProduct");
    		product.setPrice(1);
    		product.setQuantity(1);
            supplierClients[0].createProduct(product);
        }
        {
            ProductView product = new ProductView();
    		product.setId("aaaaa");
    		product.setDesc("TestProduct");
    		product.setPrice(3);
    		product.setQuantity(1);
            supplierClients[1].createProduct(product);
        }
        {
            ProductView product = new ProductView();
    		product.setId("AAAAA");
    		product.setDesc("TestProduct");
    		product.setPrice(5);
    		product.setQuantity(1);
            supplierClients[0].createProduct(product);
        }
        {
            ProductView product = new ProductView();
    		product.setId("A");
    		product.setDesc("TestProduct");
    		product.setPrice(2);
    		product.setQuantity(1);
            supplierClients[1].createProduct(product);
        }
        {
            ProductView product = new ProductView();
    		product.setId("BbBbB");
    		product.setDesc("TestProduct");
    		product.setPrice(2);
    		product.setQuantity(1);
            supplierClients[1].createProduct(product);
        }

        for (String input : new String[]{"TestProduct"}) {
            List<ItemView> results = mediatorClient.searchItems(input);
            for (int i = 1; i < results.size(); i++) {
                assertTrue(results.get(i-1).getItemId().getProductId().compareTo(results.get(i).getItemId().getProductId()) <= 0);
            }
        }
    }

    @Test
    public void priceOrderTest() throws InvalidText_Exception, BadProductId_Exception, BadProduct_Exception {
        {
            ProductView product = new ProductView();
    		product.setId("TP1");
    		product.setDesc("TestProduct1");
    		product.setPrice(10);
    		product.setQuantity(1);
            supplierClients[0].createProduct(product);
        }
        {
            ProductView product = new ProductView();
    		product.setId("TP1");
    		product.setDesc("TestProduct1");
    		product.setPrice(1);
    		product.setQuantity(1);
            supplierClients[1].createProduct(product);
        }
        {
            ProductView product = new ProductView();
    		product.setId("TP2");
    		product.setDesc("TestProduct2");
    		product.setPrice(1);
    		product.setQuantity(1);
            supplierClients[0].createProduct(product);
        }
        {
            ProductView product = new ProductView();
    		product.setId("TP2");
    		product.setDesc("TestProduct2");
    		product.setPrice(10);
    		product.setQuantity(1);
            supplierClients[1].createProduct(product);
        }

        for (String input : new String[]{"TestProduct1", "TestProduct2"}) {
            List<ItemView> results = mediatorClient.searchItems(input);
            for (int i = 1; i < results.size(); i++) {
                assertTrue(results.get(i-1).getPrice() <= results.get(i).getPrice());
            }
        }
    }

}
