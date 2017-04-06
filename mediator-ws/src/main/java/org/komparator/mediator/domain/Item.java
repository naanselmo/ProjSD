package org.komparator.mediator.domain;

public class Item {

	private ItemId id;
	private String desc;
	private int price;

	Item(String productId, String supplierId, String desc, int price) {
		this(new ItemId(productId, supplierId), desc, price);
	}

	Item(ItemId id, String desc, int price) {
		this.id = id;
		this.desc = desc;
		this.price = price;
	}

	public ItemId getId() {
		return id;
	}

	public String getDesc() {
		return desc;
	}

	public int getPrice() {
		return price;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Item{");
		sb.append("id=").append(id);
		sb.append(", desc='").append(desc).append('\'');
		sb.append(", price=").append(price);
		sb.append('}');
		return sb.toString();
	}

}
