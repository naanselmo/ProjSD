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
public class ListCartsIT extends BaseWithSuppliersIT {

    @Before
    public void setUp() throws BadProductId_Exception, BadProduct_Exception {
        {
            ProductView product = new ProductView();
    		product.setId("TP");
    		product.setDesc("TestProd");
    		product.setPrice(1);
    		product.setQuantity(1);
            supplierClients[0].createProduct(product);
        }
    }

    @After
    public void tearDown() {
        mediatorClient.clear();
    }

    @Test
    public void emptyCartsTest() throws InvalidCartId_Exception {
        assertTrue(mediatorClient.listCarts().isEmpty());
    }

    @Test
    public void singleCartTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(supplierNames[0]);
            mediatorClient.addToCart("TC", itemId, 1);
        }

        List<CartView> results = mediatorClient.listCarts();
        assertEquals(1, results.size());
        assertEquals("TC", results.get(0).getCartId());
        assertEquals(1, results.get(0).getItems().size());
        assertEquals("TP", results.get(0).getItems().get(0).getItem().getItemId().getProductId());
    }

    @Test
    public void multipleCartsTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(supplierNames[0]);
            mediatorClient.addToCart("TC1", itemId, 1);
            mediatorClient.addToCart("TC2", itemId, 1);
        }
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC3", itemId, 1);
        List<CartView> results = mediatorClient.listCarts();
        assertEquals(3, results.size());
        for (CartView cart : results) {
            for (CartItemView item : cart.getItems()) {
                assertEquals("TP", item.getItem().getItemId().getProductId());
            }
        }
    }

}
