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
    }

    @After
    public void tearDown() {
        mediatorClient.clear();
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

}
