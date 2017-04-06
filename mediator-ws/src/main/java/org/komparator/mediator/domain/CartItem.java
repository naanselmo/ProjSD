package org.komparator.mediator.domain;

public class CartItem {

	private Item item;
	private int quantity;

	public CartItem(Item item, int quantity) {
		this.item = item;
		this.quantity = quantity;
	}

	public Item getItem() {
		return item;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void updateQuantity(int delta) {
		this.quantity += delta;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("CartItem{");
		sb.append("item=").append(item);
		sb.append(", quantity=").append(quantity);
		sb.append('}');
		return sb.toString();
	}
}
