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
public class BuyCartIT extends BaseWithSuppliersIT {

    @Before
    public void setUp() throws BadProductId_Exception, BadProduct_Exception {
        {
            ProductView product = new ProductView();
    		product.setId("TP");
    		product.setDesc("TestProd");
    		product.setPrice(2);
    		product.setQuantity(5);
            supplierClients[0].createProduct(product);
        }
        {
            ProductView product = new ProductView();
    		product.setId("TP2");
    		product.setDesc("TestProd2");
    		product.setPrice(3);
    		product.setQuantity(7);
            supplierClients[1].createProduct(product);
        }
    }

    @After
    public void tearDown() {
        mediatorClient.clear();
    }

    // Input tests: test for bad input

    @Test(expected = InvalidCartId_Exception.class)
    public void nullCartNameTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(null, "4024007102923926");
    }

    @Test
    public void differentCaseCartNameTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
        mediatorClient.addToCart("tc", itemId, 1);
        mediatorClient.buyCart("TC", "4024007102923926");
        mediatorClient.buyCart("tc", "4024007102923926");
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void whitespaceCartNameTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart(" ", "4024007102923926");
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void containsSpacesCartNameTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("Test Cart", "4024007102923926");
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void containsSymbolsCartNameTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("T&C", "4024007102923926");
    }

    @Test(expected = InvalidCreditCard_Exception.class)
    public void nullCreditCardTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        // Just to make sure this module doesn't try to manipulate the null reference
        // All other checks are assumed to be handled by cc-ws
        mediatorClient.buyCart("tc", null);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void validateCartBeforeCreditCardTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        // Just to make sure this module doesn't try to manipulate the null reference
        // All other checks are assumed to be handled by cc-ws
        mediatorClient.buyCart(null, null);
    }

    // Behavioral tests: check if it actually does what we want

    @Test(expected = InvalidCartId_Exception.class)
    public void nonExistentCartTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("TC", "4024007102923926");
    }

    @Test
    public void allItemsValidTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(supplierNames[0]);
            mediatorClient.addToCart("TC", itemId, 1);
        }
        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP2");
            itemId.setSupplierId(supplierNames[1]);
            mediatorClient.addToCart("TC", itemId, 1);
        }

        ShoppingResultView resultComplete = mediatorClient.buyCart("TC", "4024007102923926");
        assertEquals(1, mediatorClient.shopHistory().size());

        assertEquals(Result.COMPLETE, resultComplete.getResult());
        assertEquals(2, resultComplete.getPurchasedItems().size());
        assertTrue(resultComplete.getDroppedItems().isEmpty());
        assertEquals(5, resultComplete.getTotalPrice());
    }

    @Test
    public void singleItemInvalidTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(supplierNames[0]);
            mediatorClient.addToCart("TC", itemId, 1);
            mediatorClient.addToCart("TC2", itemId, 5);
        }
        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP2");
            itemId.setSupplierId(supplierNames[1]);
            mediatorClient.addToCart("TC", itemId, 1);
        }

        mediatorClient.buyCart("TC2", "4024007102923926");

        ShoppingResultView resultPartial = mediatorClient.buyCart("TC", "4024007102923926");
        assertEquals(2, mediatorClient.shopHistory().size());

        assertEquals(Result.PARTIAL, resultPartial.getResult());
        assertEquals(1, resultPartial.getPurchasedItems().size());
        assertEquals(1, resultPartial.getDroppedItems().size());
        assertEquals(3, resultPartial.getTotalPrice());
    }

    @Test
    public void allItemsInvalidTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(supplierNames[0]);
            mediatorClient.addToCart("TC", itemId, 1);
            mediatorClient.addToCart("TC2", itemId, 5);
        }
        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP2");
            itemId.setSupplierId(supplierNames[1]);
            mediatorClient.addToCart("TC", itemId, 1);
            mediatorClient.addToCart("TC2", itemId, 7);
        }

        mediatorClient.buyCart("TC2", "4024007102923926");

        ShoppingResultView resultEmpty = mediatorClient.buyCart("TC", "4024007102923926");
        assertEquals(2, mediatorClient.shopHistory().size());

        assertEquals(Result.EMPTY, resultEmpty.getResult());
        assertTrue(resultEmpty.getPurchasedItems().isEmpty());
        assertEquals(2, resultEmpty.getDroppedItems().size());
        assertEquals(0, resultEmpty.getTotalPrice());
    }

    @Test
    public void creditCardInvalidTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
        try {
            mediatorClient.buyCart("TC", null);
        } catch (InvalidCreditCard_Exception e) {
        }

        assertTrue(mediatorClient.shopHistory().isEmpty());
    }

}
