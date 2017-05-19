package org.komparator.mediator.ws.it;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.komparator.mediator.client.ws.*;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;

import java.util.List;

public class RedundancyIT extends BaseWithSuppliersIT {

	private static final String VALID_CC = "1234567890123452";

	@Before
	public void setUp() throws BadProductId_Exception, BadProduct_Exception {
		// clear remote service state before each test
		mediatorClient.clear();
		{
			ProductView product = new ProductView();
			product.setId("silver");
			product.setDesc("Some silver");
			product.setPrice(50);
			product.setQuantity(300);
			supplierClients[0].createProduct(product);
		}
		{
			ProductView product = new ProductView();
			product.setId("gold");
			product.setDesc("Some gold");
			product.setPrice(100);
			product.setQuantity(200);
			supplierClients[1].createProduct(product);
		}
	}

	@Test
	public void success() throws InvalidItemId_Exception, NotEnoughItems_Exception, InvalidCartId_Exception, InvalidQuantity_Exception, EmptyCart_Exception, InvalidCreditCard_Exception, InterruptedException {
		System.out.println("Adding items to cart1 and cart2...");
		addToCart("cart1", supplierNames[0], "silver", 20);
		addToCart("cart1", supplierNames[1], "gold", 30);

		addToCart("cart2", supplierNames[0], "silver", 30);

		System.out.println("Buying cart1...");
		ShoppingResultView primaryShoppingResultView = mediatorClient.buyCart("cart1", VALID_CC);

		System.out.println("\n\nPlease shutdown the primary mediator server. You have 10 seconds until the test resumes...\n\n");
		Thread.sleep(10000);
		System.out.println("Resuming tests...");

		List<CartView> cartViews = mediatorClient.listCarts();
		List<ShoppingResultView> shoppingResultViews = mediatorClient.shopHistory();
		Assert.assertEquals(2, cartViews.size());
		Assert.assertEquals(1, shoppingResultViews.size());

		System.out.println("Testing cart info...");
		CartView cart1 = getCartById(cartViews, "cart1");
		CartView cart2 = getCartById(cartViews, "cart2");
		Assert.assertNotNull(cart1);
		Assert.assertNotNull(cart2);
		Assert.assertTrue(cartContainsProduct(cart1, supplierNames[0], "silver", "Some silver", 50, 20));
		Assert.assertTrue(cartContainsProduct(cart1, supplierNames[1], "gold", "Some gold", 100, 30));
		Assert.assertTrue(cartContainsProduct(cart2, supplierNames[0], "silver", "Some silver", 50, 30));

		System.out.println("Testing shop history info...");
		ShoppingResultView secondaryShoppingResultView = shoppingResultViews.get(0);
		Assert.assertEquals(primaryShoppingResultView.getId(), secondaryShoppingResultView.getId());
		Assert.assertEquals(primaryShoppingResultView.getResult(), secondaryShoppingResultView.getResult());
		Assert.assertEquals(primaryShoppingResultView.getTotalPrice(), secondaryShoppingResultView.getTotalPrice());
		Assert.assertTrue(equalsCartItemViewLists(primaryShoppingResultView.getDroppedItems(), secondaryShoppingResultView.getDroppedItems()));
		Assert.assertTrue(equalsCartItemViewLists(primaryShoppingResultView.getPurchasedItems(), secondaryShoppingResultView.getPurchasedItems()));

		System.out.println("Buying cart1 again just to make sure the shopping result ids are synchronized.");
		ShoppingResultView lastSecondaryShoppingResultView = mediatorClient.buyCart("cart1", VALID_CC);
		Assert.assertEquals(Integer.parseInt(secondaryShoppingResultView.getId()) + 1,
				Integer.parseInt(lastSecondaryShoppingResultView.getId()));
	}

	private void addToCart(String cartId, String supplierId, String productId, int quantity) throws InvalidItemId_Exception, NotEnoughItems_Exception, InvalidCartId_Exception, InvalidQuantity_Exception {
		ItemIdView itemIdView = new ItemIdView();
		itemIdView.setSupplierId(supplierId);
		itemIdView.setProductId(productId);
		mediatorClient.addToCart(cartId, itemIdView, quantity);
	}

	private CartView getCartById(List<CartView> carts, String id) {
		for (CartView cartView : carts) {
			if (cartView.getCartId().equals(id))
				return cartView;
		}
		return null;
	}

	private boolean cartContainsProduct(CartView cartView, String supplierId, String productId, String productDesc, int price, int quantityInCart) {
		for (CartItemView cartItemView : cartView.getItems()) {
			ItemIdView itemIdView = cartItemView.getItem().getItemId();
			if (itemIdView.getSupplierId().equals(supplierId) && itemIdView.getProductId().equals(productId)) {
				Assert.assertEquals(quantityInCart, cartItemView.getQuantity());
				Assert.assertEquals(productDesc, cartItemView.getItem().getDesc());
				Assert.assertEquals(price, cartItemView.getItem().getPrice());
				return true;
			}
		}
		return false;
	}

	private boolean equalsCartItemViewLists(List<CartItemView> list1, List<CartItemView> list2) {
		if (list1.size() != list2.size()) return false;
		for (int i = 0; i < list1.size(); i++) {
			CartItemView cartItemView1 = list1.get(i);
			CartItemView cartItemView2 = list2.get(i);
			if (!equalsCartItemView(cartItemView1, cartItemView2))
				return false;
		}
		return true;
	}


	private boolean equalsCartItemView(CartItemView cartItemView1, CartItemView cartItemView2) {
		if (cartItemView1.getQuantity() != cartItemView2.getQuantity()) return false;
		if (!cartItemView1.getItem().getItemId().getSupplierId().equals(cartItemView2.getItem().getItemId().getSupplierId()))
			return false;
		if (!cartItemView1.getItem().getItemId().getProductId().equals(cartItemView2.getItem().getItemId().getProductId()))
			return false;
		if (!cartItemView1.getItem().getDesc().equals(cartItemView2.getItem().getDesc())) return false;
		if (cartItemView1.getItem().getPrice() != cartItemView2.getItem().getPrice()) return false;
		return true;
	}

	@After
	public void cleanUp() {
		mediatorClient.clear();
	}

}
