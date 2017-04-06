package org.komparator.mediator.domain;

import java.util.Objects;

public class ItemId {

	private String productId;
	private String supplierId;

	public ItemId(String productId, String supplierId) {
		this.productId = productId;
		this.supplierId = supplierId;
	}

	public String getProductId() {
		return productId;
	}

	public String getSupplierId() {
		return supplierId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ItemId itemId = (ItemId) o;
		return Objects.equals(productId, itemId.productId) &&
				Objects.equals(supplierId, itemId.supplierId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(productId, supplierId);
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("ItemId{");
		sb.append("productId='").append(productId).append('\'');
		sb.append(", supplierId='").append(supplierId).append('\'');
		sb.append('}');
		return sb.toString();
	}

}
