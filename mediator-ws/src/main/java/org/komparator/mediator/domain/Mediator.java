package org.komparator.mediator.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Mediator {

	/**
	 * Carts map.
	 */
	private Map<String, Cart> carts = new ConcurrentHashMap<>();

	/**
	 * Counter for unique shopping result ids.
	 */
	private AtomicInteger shoppingResultIdCounter = new AtomicInteger(0);

	/**
	 * Shopping results map.
	 */
	private Map<String, ShoppingResult> shoppingResults = new ConcurrentHashMap<>();

	/* Private constructor prevents instantiation from other classes */
	private Mediator() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final Mediator INSTANCE = new Mediator();
	}

	public static synchronized Mediator getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void reset() {
		carts.clear();
		shoppingResults.clear();
		shoppingResultIdCounter.set(0);
	}

	// Carts -----------------------------------------------------------------------------------

	private int getItemQuantity(String cartId, ItemId itemId) {
		if (!carts.containsKey(cartId)) return 0;
		Cart cart = carts.get(cartId);
		if (!cart.containsItem(itemId)) return 0;
		return cart.getItem(itemId).getQuantity();
	}

	public synchronized boolean testAndAddToCart(String cartId, String productId, String supplierId, String desc, int price, int itemQuantity, int supplierQuantity) {
		Cart cart = carts.get(cartId);
		if (cart == null) {
			cart = new Cart(cartId);
			carts.put(cart.getId(), cart);
		}
		ItemId itemId = new ItemId(productId, supplierId);
		if (supplierQuantity >= getItemQuantity(cartId, itemId) + itemQuantity) {
			Item item = new Item(itemId, desc, price);
			cart.add(item, itemQuantity);
			return true;
		}
		return false;
	}

	public Cart getCart(String cartId) {
		return carts.get(cartId);
	}

	public Collection<Cart> getCarts() {
		return carts.values();
	}

	// Shopping results -------------------------------------------------------------------------

	private String generateShoppingResultId() {
		// relying on AtomicInteger to make sure assigned number is unique
		int purchaseId = shoppingResultIdCounter.incrementAndGet();
		return Integer.toString(purchaseId);
	}

	public ShoppingResult registerPurchase(String cartId, List<CartItem> purchasedItems, List<CartItem> droppedItems) {
		int totalPrice = 0;
		for (CartItem item : purchasedItems) {
			totalPrice += item.getQuantity() * item.getItem().getPrice();
		}
		ShoppingResult.Result result;
		if (purchasedItems.size() == 0) result = ShoppingResult.Result.EMPTY;
		else if (droppedItems.size() == 0) result = ShoppingResult.Result.COMPLETE;
		else result = ShoppingResult.Result.PARTIAL;
		ShoppingResult shoppingResult = new ShoppingResult(generateShoppingResultId(), result, purchasedItems, droppedItems, totalPrice);
		shoppingResults.put(shoppingResult.getId(), shoppingResult);
		return shoppingResult;
	}

	public ShoppingResult getShoppingResult(String id) {
		return shoppingResults.get(id);
	}

	public Collection<ShoppingResult> getShoppingResults() {
		return shoppingResults.values();
	}

}
