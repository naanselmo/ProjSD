package org.komparator.mediator.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Cart {

	private String id;
	private Map<ItemId, CartItem> items;

	Cart(String id) {
		this.id = id;
		this.items = new HashMap<>();
	}

	public void add(Item item, int quantity) {
		if (containsItem(item.getId())) {
			CartItem cartItem = getItem(item.getId());
			cartItem.updateQuantity(quantity);
		} else {
			items.put(item.getId(), new CartItem(item, quantity));
		}
	}

	boolean containsItem(ItemId itemId) {
		return items.containsKey(itemId);
	}

	CartItem getItem(ItemId itemId) {
		return items.get(itemId);
	}

	public String getId() {
		return id;
	}

	public Collection<CartItem> getItems() {
		return items.values();
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Cart{");
		sb.append("id='").append(id).append('\'');
		sb.append(", items=").append(items);
		sb.append('}');
		return sb.toString();
	}
}
