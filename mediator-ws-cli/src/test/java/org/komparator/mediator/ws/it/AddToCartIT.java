package org.komparator.mediator.ws.it;

import java.util.List;

import org.komparator.mediator.ws.*;
import org.komparator.supplier.ws.*;
import org.komparator.supplier.ws.cli.SupplierClient;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * Test suite
 */
public class AddToCartIT extends BaseWithSuppliersIT {

    @Before
    public void setUp() throws BadProductId_Exception, BadProduct_Exception {
	    // clear remote service state before each test
	    mediatorClient.clear();

        {
            ProductView product = new ProductView();
    		product.setId("TP");
    		product.setDesc("TestProd");
    		product.setPrice(1);
    		product.setQuantity(5);
            supplierClients[0].createProduct(product);
        }
        {
            ProductView product = new ProductView();
    		product.setId("TP2");
    		product.setDesc("TestProd2");
    		product.setPrice(1);
    		product.setQuantity(7);
            supplierClients[1].createProduct(product);
        }
	    // fill-in test products
	    // (since addToCart is a read/write operation
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
        mediatorClient.clear();
        supplierClients[0].clear();
        supplierClients[1].clear();
    }

    // Input tests: test for bad input

    @Test(expected = InvalidCartId_Exception.class)
    public void nullCartNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart(null, itemId, 1);
    }

    @Test
    public void differentCaseCartNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
        mediatorClient.addToCart("tc", itemId, 1);
        assertEquals(2, mediatorClient.listCarts().size());
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void whitespaceCartNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart(" ", itemId, 1);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void containsSpacesCartNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TE ST", itemId, 1);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void containsSymbolsCartNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("T&ST", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void nullItemIdTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart("TC", null, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void nullItemNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId(null);
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void differentCaseItemNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("tP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void whitespaceItemNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId(" ");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void containsSpacesItemNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("Te st");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void containsSymbolsItemNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("T&P");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void nullSupplierNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(null);
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void differentCaseSupplierNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId("t04_supplier1");
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void whitespaceSupplierNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(" ");
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void containsSpacesSupplierNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId("T04 Supplier1");
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void containsSymbolsSupplierNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId("T04&Supplier1");
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidQuantity_Exception.class)
    public void negativeQuantityTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, -1);
    }

    @Test(expected = InvalidQuantity_Exception.class)
    public void zeroQuantityTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 0);
    }

    @Test(expected = NotEnoughItems_Exception.class)
    public void tooManyQuantityTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 10);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void validateCartBeforeItemTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId(null);
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart(null, itemId, 1);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void validateCartBeforeSupplierTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(null);
        mediatorClient.addToCart(null, itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void validateItemBeforeQuantityTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId(null);
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, -1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void validateSupplierBeforeQuantityTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(null);
        mediatorClient.addToCart("TC", itemId, -1);
    }

    // Behavioral tests: check if it actually does what we want

    @Test
    public void newCartTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(supplierNames[0]);
            mediatorClient.addToCart("TC", itemId, 3);
        }
        List<CartView> results = mediatorClient.listCarts();
        assertEquals(1, results.size());
        assertEquals("TC", results.get(0).getCartId());
        assertEquals(1, results.get(0).getItems().size());
        assertEquals(3, results.get(0).getItems().get(0).getQuantity());
        assertEquals("TP", results.get(0).getItems().get(0).getItem().getItemId().getProductId());
        assertEquals(supplierNames[0], results.get(0).getItems().get(0).getItem().getItemId().getSupplierId());
    }

    @Test
    public void existingCartTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(supplierNames[0]);
            mediatorClient.addToCart("TC", itemId, 3);
        }
        assertEquals(1, mediatorClient.listCarts().size());

        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP2");
            itemId.setSupplierId(supplierNames[1]);
            mediatorClient.addToCart("TC", itemId, 2);
        }
        List<CartView> results = mediatorClient.listCarts();
        assertEquals(1, results.size());
        assertEquals("TC", results.get(0).getCartId());
        assertEquals(2, results.get(0).getItems().size());
    }

    // Teacher

	@Test(expected = InvalidCartId_Exception.class)
	public void testNullCartId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception,
			NotEnoughItems_Exception {
		ItemIdView id = new ItemIdView();
		id.setProductId("prod0001");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart(null, id, 2);
	}

	@Test(expected = InvalidCartId_Exception.class)
	public void testEmptyCartId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception,
			NotEnoughItems_Exception {
		ItemIdView id = new ItemIdView();
		id.setProductId("prod0001");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("", id, 2);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void testNullItemId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception,
			NotEnoughItems_Exception {
		mediatorClient.addToCart("xyz", null, 2);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void testNullProductId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception,
			NotEnoughItems_Exception {
		ItemIdView id = new ItemIdView();
		id.setProductId(null);
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("xyz", id, 2);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void testEmptyProductId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception,
			NotEnoughItems_Exception {
		ItemIdView id = new ItemIdView();
		id.setProductId("");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("xyz", id, 2);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void testNullSupplierId() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception,
			NotEnoughItems_Exception {
		ItemIdView id = new ItemIdView();
		id.setProductId("prod0001");
		id.setSupplierId(null);
		mediatorClient.addToCart("xyz", id, 2);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void testEmptySupplierId() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		ItemIdView id = new ItemIdView();
		id.setProductId("prod0001");
		id.setSupplierId("");
		mediatorClient.addToCart("xyz", id, 2);
	}

	@Test(expected = InvalidItemId_Exception.class)
	public void testInexistingItem() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception,
			NotEnoughItems_Exception {
		ItemIdView id = new ItemIdView();
		id.setProductId("banana");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("xyz", id, 5);
	}

	@Test(expected = InvalidQuantity_Exception.class)
	public void testNegativeQuantity() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		ItemIdView id = new ItemIdView();
		id.setProductId("p2");
		id.setSupplierId(supplierNames[1]);
		mediatorClient.addToCart("xyz", id, -4);
	}

	@Test(expected = InvalidQuantity_Exception.class)
	public void testZeroQuantity() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception,
			NotEnoughItems_Exception {
		ItemIdView id = new ItemIdView();
		id.setProductId("p2");
		id.setSupplierId(supplierNames[1]);
		mediatorClient.addToCart("xyz", id, 0);
	}

	@Test(expected = NotEnoughItems_Exception.class)
	public void testExceedingQuantity1() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		ItemIdView id = new ItemIdView();
		id.setProductId("p1");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("xyz", id, 11);
	}

	@Test(expected = NotEnoughItems_Exception.class)
	public void testExceedingQuantity2() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		// addToCart should not decrease available quantity,
		// but should check if a single cart has more items than
		// the available ones.
		ItemIdView id = new ItemIdView();
		id.setProductId("p1");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("xyz", id, 10);
		mediatorClient.addToCart("xyz", id, 1);
	}

	// ------ Normal cases -----

	@Test
	public void testSingleItemCheckCartState() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		ItemIdView id = new ItemIdView();
		id.setProductId("p2");
		id.setSupplierId(supplierNames[1]);
		mediatorClient.addToCart("xyz", id, 1);

		List<CartView> cartList = mediatorClient.listCarts();
		assertEquals("Cart list size:", 1, cartList.size());

		CartView cart = cartList.get(0);
		assertEquals("Cart id:", "xyz", cart.getCartId());
		assertEquals("Number of items in cart:", 1, cart.getItems().size());

		CartItemView cartItem = cart.getItems().get(0);
		assertEquals("CartItem quantity:", 1, cartItem.getQuantity());
		assertEquals("Product id:", "p2", cartItem.getItem().getItemId().getProductId());
		assertEquals("Supplier id:", supplierNames[1], cartItem.getItem().getItemId().getSupplierId());
		assertEquals("Product description:", "10x AAA battery", cartItem.getItem().getDesc());
		assertEquals("Price:", 8, cartItem.getItem().getPrice());
	}

	@Test
	public void testSingleItemAddedTwoTimes() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		// adds an item 2 times (quantity = 1 + 1 = 2)
		ItemIdView id = new ItemIdView();
		id.setProductId("p1");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("xyz", id, 1);
		mediatorClient.addToCart("xyz", id, 1);

		List<CartView> cartList = mediatorClient.listCarts();
		assertEquals(1, cartList.size());

		CartView cart = cartList.get(0);
		assertEquals("xyz", cart.getCartId());
		assertEquals(1, cart.getItems().size());

		CartItemView cartItem = cart.getItems().get(0);
		assertEquals(2, cartItem.getQuantity());
		assertEquals(3, cartItem.getItem().getPrice());
	}

	@Test
	public void testSingleItemAddAll() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		// resulting quantity in supplier is 0, still valid
		ItemIdView id = new ItemIdView();
		id.setProductId("p1");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("xyz", id, 10);
	}

	@Test
	public void testSingleItemAddAllTwoTimesDifferentCarts() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		// addToCart should not decrease available quantity
		ItemIdView id = new ItemIdView();
		id.setProductId("p1");
		id.setSupplierId(supplierNames[0]);
		mediatorClient.addToCart("oneCart", id, 10);
		mediatorClient.addToCart("anotherCart", id, 10);
	}

	@Test
	public void testMultipleItemsOneCart() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		// -- add products --
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

		// -- assert number of carts --
		List<CartView> cartList = mediatorClient.listCarts();
		assertEquals(1, cartList.size());

		// -- assert state of each cart --
		// there are no ordering guarantees, and therefore
		// the code below assumes no ordering
		CartView cart = cartList.get(0);
		assertEquals("xyz", cart.getCartId());
		assertEquals(3, cart.getItems().size());

		// check first product added
		{
			final String prodId = "p1";
			final String supplierId = supplierNames[0];
			final String desc = "AAA bateries (pack of 3)";
			final int price = 3;
			final int quantity = 2;
			final boolean itemStateIsCorrect = checkItemInCart(cart, prodId, supplierId, desc, price, quantity);
			assertTrue(itemStateIsCorrect);
		}

		// check second product added
		{
			final String prodId = "p1";
			final String supplierId = supplierNames[1];
			final String desc = "3batteries";
			final int price = 4;
			final int quantity = 1;
			final boolean itemStateIsCorrect = checkItemInCart(cart, prodId, supplierId, desc, price, quantity);
			assertTrue(itemStateIsCorrect);
		}

		// check third product added
		{
			final String prodId = "p2";
			final String supplierId = supplierNames[0];
			final String desc = "AAA bateries (pack of 10)";
			final int price = 9;
			final int quantity = 3;
			final boolean itemStateIsCorrect = checkItemInCart(cart, prodId, supplierId, desc, price, quantity);
			assertTrue(itemStateIsCorrect);
		}
	}

	@Test
	public void testMultipleItemsMultipleCarts() throws InvalidCartId_Exception, InvalidItemId_Exception,
			InvalidQuantity_Exception, NotEnoughItems_Exception {
		// -- add products --
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
			mediatorClient.addToCart("OtherCart", id, 1);
		}

		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p2");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("xyz", id, 3);
		}

		// -- assert number of carts and total number of items --
		List<CartView> cartList = mediatorClient.listCarts();
		assertEquals(2, cartList.size());
		assertEquals(3, cartList.get(0).getItems().size() + cartList.get(1).getItems().size());

		// -- assert state of each cart --
		// there are no ordering guarantees, and therefore
		// the code below assumes no ordering

		// check first product added
		{
			final String cartId = "xyz";
			final String prodId = "p1";
			final String supplierId = supplierNames[0];
			final String desc = "AAA bateries (pack of 3)";
			final int price = 3;
			final int quantity = 2;
			CartView cart = findCart(cartList, cartId);
			final boolean itemStateIsCorrect = checkItemInCart(cart, prodId, supplierId, desc, price, quantity);
			assertTrue(itemStateIsCorrect);
		}

		// check second product added
		{
			final String cartId = "OtherCart";
			final String prodId = "p1";
			final String supplierId = supplierNames[1];
			final String desc = "3batteries";
			final int price = 4;
			final int quantity = 1;
			CartView cart = findCart(cartList, cartId);
			final boolean itemStateIsCorrect = checkItemInCart(cart, prodId, supplierId, desc, price, quantity);
			assertTrue(itemStateIsCorrect);
		}

		// check third product added
		{
			final String cartId = "xyz";
			final String prodId = "p2";
			final String supplierId = supplierNames[0];
			final String desc = "AAA bateries (pack of 10)";
			final int price = 9;
			final int quantity = 3;
			CartView cart = findCart(cartList, cartId);
			final boolean itemStateIsCorrect = checkItemInCart(cart, prodId, supplierId, desc, price, quantity);
			assertTrue(itemStateIsCorrect);
		}
	}

	/**
	 * Helper method to find a cart in the given cart list. Returns null if not
	 * found.
	 */
	private CartView findCart(List<CartView> cartList, String cartId) {
		// Find cart by id
		CartView cart = null;
		for (CartView c : cartList) {
			if (c.getCartId().equals(cartId)) {
				cart = c;
				break;
			}
		}
		return cart;
	}

	/** Helper method to check if an item is found in the given cart. */
	private boolean checkItemInCart(CartView cart, String prodId, String supplierId, String desc, int price,
	                                int quantity) {

		// Check if there is a matching item in the cart.
		// We cannot assume any particular order.
		if (cart != null) {
			for (CartItemView cartItemView : cart.getItems()) {
				boolean found = cartItemView.getItem().getItemId().getProductId().equals(prodId)
						&& cartItemView.getItem().getItemId().getSupplierId().equals(supplierId)
						&& cartItemView.getItem().getDesc().equals(desc) && cartItemView.getItem().getPrice() == price
						&& cartItemView.getQuantity() == quantity;

				if (found)
					return true;
			}
		}

		return false;
	}

}
