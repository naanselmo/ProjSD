package org.komparator.mediator.domain;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ShoppingResult {

	public enum Result {
		COMPLETE,
		PARTIAL,
		EMPTY;

		public String value() {
			return name();
		}

		public static Result fromValue(String v) {
			return valueOf(v);
		}
	}

	private String id;
	private Result result;
	private List<CartItem> purchasedItems;
	private List<CartItem> droppedItems;
	private int totalPrice;
	private Date timestamp = new Date();

	public ShoppingResult(String id, Result result, List<CartItem> purchasedItems, List<CartItem> droppedItems, int totalPrice) {
		this.id = id;
		this.result = result;
		this.purchasedItems = Collections.unmodifiableList(purchasedItems);
		this.droppedItems = Collections.unmodifiableList(droppedItems);
		this.totalPrice = totalPrice;
	}

	public String getId() {
		return id;
	}

	public List<CartItem> getPurchasedItems() {
		return purchasedItems;
	}

	public List<CartItem> getDroppedItems() {
		return droppedItems;
	}

	public int getTotalPrice() {
		return totalPrice;
	}

	public Result getResult() {
		return result;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("ShoppingResult{");
		sb.append("id='").append(id).append('\'');
		sb.append(", result=").append(result);
		sb.append(", purchasedItems=").append(purchasedItems);
		sb.append(", droppedItems=").append(droppedItems);
		sb.append(", totalPrice=").append(totalPrice);
		sb.append(", timestamp=").append(timestamp);
		sb.append('}');
		return sb.toString();
	}

}
