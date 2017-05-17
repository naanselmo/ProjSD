package org.komparator.mediator.ws.it;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.komparator.mediator.client.ws.*;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test suite
 */
public class ClearIT extends BaseWithSuppliersIT {

    @Before
    public void setUp() throws BadProductId_Exception, BadProduct_Exception {
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
        for ( SupplierClient client : supplierClients ) {
            client.clear();
        }
    }

    @Test
    public void productClearTest() throws BadProductId_Exception {
        mediatorClient.clear();
        for ( SupplierClient client : supplierClients ) {
            assertNull(client.getProduct("TP"));
        }
    }

    @Test
    public void addToCartClearTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        for ( String name : supplierNames ) {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(name);
            mediatorClient.addToCart("TC", itemId, 1);
        }
        mediatorClient.clear();
        assertTrue(mediatorClient.listCarts().isEmpty());
    }

    @Test
    public void addToCartBuyCartClearTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, EmptyCart_Exception, InvalidCreditCard_Exception, NotEnoughItems_Exception {
        for ( String name : supplierNames ) {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(name);
            mediatorClient.addToCart("TC", itemId, 1);
        }
        mediatorClient.buyCart("TC", "4024007102923926");
        mediatorClient.clear();
        assertTrue(mediatorClient.shopHistory().isEmpty());
    }

}
