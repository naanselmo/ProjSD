package org.komparator.mediator.ws.it;

import java.util.List;

import org.komparator.mediator.ws.*;
import org.komparator.supplier.ws.*;
import org.komparator.supplier.ws.cli.SupplierClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * Test suite
 */
public class BuyCartIT extends BaseWithSuppliersIT {

	private static final String VALID_CC = "1234567890123452";

    @Before
    public void setUp() throws BadProductId_Exception, BadProduct_Exception {
	    // clear remote service state before each test
	    mediatorClient.clear();

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

	    // fill-in test products
	    // (since buyProduct is a read/write operation
	    // the initialization below is done for each test)
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

	    {
		    ProductView prod = new ProductView();
		    prod.setId("p4");
		    prod.setDesc("very cheap batteries");
		    prod.setPrice(2);
		    prod.setQuantity(5);
		    supplierClients[0].createProduct(prod);
	    }
    }

    @After
    public void tearDown() {
	    // clear remote service state after each test
	    mediatorClient.clear();
	    // even though mediator clear should have cleared suppliers, clear them
	    // explicitly after use
	    supplierClients[0].clear();
	    supplierClients[1].clear();
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
    public void nullCreditCardTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception, InvalidItemId_Exception {
        // Just to make sure this module doesn't try to manipulate the null reference
        // All other checks are assumed to be handled by cc-ws
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("tc", itemId, 1);
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

    @Test(expected = InvalidCartId_Exception.class)
    public void nonExistentCartTestWithInvalidCreditCardTest() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
        mediatorClient.buyCart("xyz", "0293");
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

    // Teacher

	// ------ WSDL Faults (error cases) ------

	@Test(expected = InvalidCartId_Exception.class)
	public void testNullCart() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		mediatorClient.buyCart(null, "2344223");
	}

	@Test(expected = InvalidCartId_Exception.class)
	public void testEmptyCartId() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		mediatorClient.buyCart("", "2344223");
	}

	@Test(expected = InvalidCartId_Exception.class)
	public void testInexistingCart() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		mediatorClient.buyCart("xyz", VALID_CC);
	}

	@Test(expected = InvalidCreditCard_Exception.class)
	public void testEmptyCardId() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		ItemIdView id = new ItemIdView();
		id.setProductId("p2");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("xyz", id, 1);
		mediatorClient.buyCart("xyz", "");
	}

	@Test(expected = InvalidCreditCard_Exception.class)
	public void testIncompleteCard() throws EmptyCart_Exception, InvalidCartId_Exception,
			InvalidCreditCard_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		// < 16 digits
		ItemIdView id = new ItemIdView();
		id.setProductId("p2");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("xyz", id, 1);
		mediatorClient.buyCart("xyz", "8392320");
	}

	@Test(expected = InvalidCreditCard_Exception.class)
	public void testInvalidCard() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		// > 16 digits, 16 first are a 'valid' card
		ItemIdView id = new ItemIdView();
		id.setProductId("p2");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("xyz", id, 1);
		mediatorClient.buyCart("xyz", VALID_CC + "27282828022802");
	}

	// not used for evaluation
	// @Test(expected = InvalidCreditCard_Exception.class)
	public void testInvalidCardLetters() throws EmptyCart_Exception, InvalidCartId_Exception,
			InvalidCreditCard_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		// string with letters
		ItemIdView id = new ItemIdView();
		id.setProductId("p2");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("xyz", id, 1);
		mediatorClient.buyCart("xyz", "12334567890aaabbb");
	}

	// not used for evaluation
	// @Test(expected = InvalidCreditCard_Exception.class)
	public void testAnotherInvalidCard() throws EmptyCart_Exception, InvalidCartId_Exception,
			InvalidCreditCard_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		ItemIdView id = new ItemIdView();
		id.setProductId("p2");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("xyz", id, 1);
		mediatorClient.buyCart("xyz", "1234567820123452");
	}

	// ------ Normal cases

	@Test
	public void testFullBuy() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		// -- add products to carts --
		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p1");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("xyz", id, 2);
		}

		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p1");
			id.setSupplierId(supplierNames[1]);
			mediatorClient.addToCart("xyz", id, 1);
		}

		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p2");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("xyz", id, 3);
		}

		{ // product in other cart! (will not try to buy this)
			ItemIdView id = new ItemIdView();
			id.setProductId("p1");
			id.setSupplierId(supplierNames[1]);
			mediatorClient.addToCart("DoNotBuyMe", id, 1);
		}

		// -- buy a cart
		ShoppingResultView shpResView = mediatorClient.buyCart("xyz", VALID_CC);
		assertNotNull(shpResView.getId());
		assertEquals(Result.COMPLETE, shpResView.getResult());
		assertEquals(0, shpResView.getDroppedItems().size());
		assertEquals(3, shpResView.getPurchasedItems().size());
		final int expectedTotalPrice = 2 * 3 + 1 * 4 + 3 * 9; // sum(qty*price)
		assertEquals(expectedTotalPrice, shpResView.getTotalPrice());
	}

	@Test
	public void testPartialBuy() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		// -- add products to carts --
		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p1");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("xyz", id, 4);
			mediatorClient.addToCart("otherXyz", id, 3); // Purchased
		}

		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p2");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("xyz", id, 15);
			mediatorClient.addToCart("otherXyz", id, 7); // Dropped
		}

		// -- buy carts
		ShoppingResultView[] shpResViews = new ShoppingResultView[2];
		shpResViews[0] = mediatorClient.buyCart("xyz", VALID_CC);
		shpResViews[1] = mediatorClient.buyCart("otherXyz", VALID_CC);

		// verify id and result of first buy
		assertNotNull(shpResViews[0].getId());
		assertEquals(Result.COMPLETE, shpResViews[0].getResult());

		// verify the entire result of the second buy (i.e., the partial one)
		assertNotNull(shpResViews[1].getId());
		assertEquals(Result.PARTIAL, shpResViews[1].getResult());
		assertEquals(1, shpResViews[1].getDroppedItems().size());
		assertEquals(1, shpResViews[1].getPurchasedItems().size());
		final int expectedTotalPrice = 3 * 3; // sum(qty*price) of purchased
		// items
		assertEquals(expectedTotalPrice, shpResViews[1].getTotalPrice());
	}

	@Test
	public void testEmptyBuy() throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception,
			InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		// -- add products to carts --
		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p1");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("xyz", id, 10);
			mediatorClient.addToCart("otherXyz", id, 10); // Dropped
		}

		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p2");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("xyz", id, 15);
			mediatorClient.addToCart("otherXyz", id, 15); // Dropped
		}

		// -- buy carts
		ShoppingResultView[] shpResViews = new ShoppingResultView[2];
		shpResViews[0] = mediatorClient.buyCart("xyz", VALID_CC);
		shpResViews[1] = mediatorClient.buyCart("otherXyz", VALID_CC);

		// verify id and result of first buy
		assertNotNull(shpResViews[0].getId());
		assertEquals(Result.COMPLETE, shpResViews[0].getResult());

		// verify the entire result of the second buy (i.e., the empty one)
		assertNotNull(shpResViews[1].getId());
		assertEquals(Result.EMPTY, shpResViews[1].getResult());
		assertEquals(2, shpResViews[1].getDroppedItems().size());
		assertEquals(0, shpResViews[1].getPurchasedItems().size());
		assertEquals(0, shpResViews[1].getTotalPrice());
	}

}
