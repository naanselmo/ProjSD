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
public class ShopHistoryIT extends BaseWithSuppliersIT {

    @Before
    public void setUp() throws BadProductId_Exception, BadProduct_Exception {
        mediatorClient.clear();
        {
            ProductView product = new ProductView();
    		product.setId("TP");
    		product.setDesc("TestProd");
    		product.setPrice(1);
    		product.setQuantity(10);
            for ( SupplierClient client : supplierClients ) {
                client.createProduct(product);
            }
        }
    }

    @After
    public void tearDown() {
        mediatorClient.clear();
    }

    @Test
    public void noHistoryTest() {
        assertTrue(mediatorClient.shopHistory().isEmpty());
    }

    @Test
    public void singleBuyTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, EmptyCart_Exception, InvalidCreditCard_Exception, NotEnoughItems_Exception {
        for ( String name : supplierNames ) {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(name);
            mediatorClient.addToCart("TC", itemId, 1);
        }
        mediatorClient.buyCart("TC", "4024007102923926");
        assertEquals(1, mediatorClient.shopHistory().size());
    }

    @Test
    public void multipleBuyOrderTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, EmptyCart_Exception, InvalidCreditCard_Exception, NotEnoughItems_Exception {
        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(supplierNames[0]);
            mediatorClient.addToCart("T1", itemId, 1);
        }
        mediatorClient.buyCart("T1", "4024007102923926");

        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(supplierNames[0]);
            mediatorClient.addToCart("T2", itemId, 5);
        }
        mediatorClient.buyCart("T2", "4024007102923926");

        List<ShoppingResultView> results = mediatorClient.shopHistory();
        assertEquals(2, results.size());
        assertEquals(5, results.get(0).getTotalPrice());
        assertEquals(1, results.get(1).getTotalPrice());
    }

}
