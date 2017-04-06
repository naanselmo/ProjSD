package org.komparator.mediator.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Mediator {

	/**
	 * Cart lists.
	 */
	Map<String, Cart> carts = new ConcurrentHashMap<>();

	/**
	 * Counter for purchase counter.
	 */
	AtomicInteger cartPurchaseIdCounter = new AtomicInteger(0);

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

	public int getItemQuantity(String cartId, String productId, String supplierId) {
		if (!carts.containsKey(cartId)) return 0;
		Cart cart = carts.get(cartId);
		ItemId itemId = new ItemId(productId, supplierId);
		if (!cart.containsItem(itemId)) return 0;
		return cart.getItem(itemId).getQuantity();
	}

	public void addCart(String cartId, String productId, String supplierId, String desc, int price, int quantity) {
		Cart cart = carts.get(cartId);
		if (cart == null) {
			cart = new Cart(cartId);
			carts.put(cart.getId(), cart);
		}
		Item item = new Item(productId, supplierId, desc, price);
		cart.add(item, quantity);
	}

}
