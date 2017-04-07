package org.komparator.mediator.ws;

import org.komparator.mediator.domain.*;
import org.komparator.supplier.ws.*;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClientException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

import javax.jws.WebService;
import javax.xml.ws.WebServiceException;
import java.util.*;
import java.util.stream.Collectors;

@WebService(
		endpointInterface = "org.komparator.mediator.ws.MediatorPortType",
		wsdlLocation = "mediator.wsdl",
		name = "MediatorWebService",
		portName = "MediatorPort",
		targetNamespace = "http://ws.mediator.komparator.org/",
		serviceName = "MediatorService"
)
public class MediatorPortImpl implements MediatorPortType {

	// end point manager
	private MediatorEndpointManager endpointManager;

	// Suppliers uddi naming server
	private UDDINaming suppliersUddi;

	// Suppliers uddi url
	private String suppliersUddiUrl = MediatorConfig.getProperty(MediatorConfig.PROPERTY_SUPPLIERS_UDDI_URL);

	// Suppliers ws name format
	private String suppliersWsNameFormat = MediatorConfig.getProperty(MediatorConfig.PROPERTY_WS_NAME_FORMAT);

	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}

	// Main operations -------------------------------------------------------

	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		if (productId == null) {
			throwInvalidItemId("Product id cannot be null!");
		}
		productId = productId.trim();
		if (productId.length() == 0) {
			throwInvalidItemId("Product id cannot be empty or whitespace!");
		}
		if (!isAlphanumeric(productId)) {
			throwInvalidItemId("Product id must be alphanumeric!");
		}

		List<ItemView> result = new ArrayList<>();
		Collection<UDDIRecord> uddiRecords = listSupplierRecords();
		SupplierClient client;
		for (UDDIRecord record : uddiRecords) {
			try {
				client = new SupplierClient(record.getUrl());
				try {
					ProductView productView = client.getProduct(productId);
					if (productView == null)
						continue;
					Item item = new Item(productView.getId(), record.getOrgName(), productView.getDesc(), productView.getPrice());
					result.add(newItemView(item));
				} catch (BadProductId_Exception e) {
					// Just because one supplier finds the product id invalid, doesn't mean others will.
					// So we decided not to throw the exception and rather just skip the supplier, warning only the mediator.
//					throwInvalidItemId("Invalid product id!");
					System.err.printf("%s says %s is an invalid product id.", record.getOrgName(), productId);
				}
			} catch (SupplierClientException | WebServiceException ignored) {
				System.err.printf("%s didn't not respond to getProduct in the getItems operation.", record.getOrgName());
			}
		}
		result.sort(Comparator.comparingInt(ItemView::getPrice));
		return result;
	}

	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		if (descText == null) {
			throwInvalidText("Search text cannot be null!");
		}
		descText = descText.trim();
		if (descText.length() == 0) {
			throwInvalidText("Search text cannot be empty or whitespace!");
		}

		List<ItemView> result = new ArrayList<>();
		Collection<UDDIRecord> uddiRecords = listSupplierRecords();
		SupplierClient client;
		for (UDDIRecord record : uddiRecords) {
			try {
				client = new SupplierClient(record.getUrl());
				try {
					List<ProductView> productsList = client.searchProducts(descText);
					for (ProductView productView : productsList) {
						Item item = new Item(productView.getId(), record.getOrgName(), productView.getDesc(), productView.getPrice());
						result.add(newItemView(item));
					}
				} catch (BadText_Exception e) {
					// Just because one supplier finds the search text invalid, doesn't mean others will.
					// So we decided not to throw the exception and rather just skip the supplier, warning only the mediator.
					// throwInvalidText("Invalid product text!");
					System.err.printf("%s says %s is an invalid product description text.", record.getOrgName(), descText);
				}
			} catch (SupplierClientException | WebServiceException e) {
				System.err.printf("%s didn't not respond to searchProducts in the searchItems operation.", record.getOrgName());
			}
		}

		Comparator<ItemView> comparator = Comparator.comparing(itemView -> itemView.getItemId().getProductId());
		comparator = comparator.thenComparing(ItemView::getPrice);
		result.sort(comparator);
		return result;
	}

	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr) throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		if (cartId == null) {
			throwInvalidCartId("Cart id cannot be null!");
		}
		cartId = cartId.trim();
		if (cartId.length() == 0) {
			throwInvalidCartId("Cart id cannot be empty or whitespace!");
		}
		if (!isAlphanumeric(cartId)) {
			throwInvalidCartId("Cart id must be alphanumeric!");
		}

		if (creditCardNr == null) {
			throwInvalidCreditCard("Credit card cannot be null!");
		}
		creditCardNr = creditCardNr.trim();
		if (creditCardNr.length() == 0) {
			throwInvalidCreditCard("Credit card cannot be empty or whitespace!");
		}
		try {
			CreditCardClient creditCardClient = new CreditCardClient(MediatorConfig.getProperty(MediatorConfig.PROPERTY_CC_WS_URL));
			if (!creditCardClient.validateNumber(creditCardNr)) {
				throwInvalidCreditCard("Invalid credit card!");
			}
		} catch (CreditCardClientException | WebServiceException e) {
			throwInvalidCreditCard("Couldn't connect to the credit card verifier!");
		}
		Cart cart = Mediator.getInstance().getCart(cartId);
		if (cart == null) {
			throwInvalidCartId("There is no cart with that id!");
		}
		if (cart.getItems().size() == 0) {
			throwEmptyCartException("The cart can't be empty!");
		}

		List<CartItem> purchasedItems = new ArrayList<>();
		List<CartItem> droppedItems = new ArrayList<>();
		for (CartItem cartItem : cart.getItems()) {
			UDDIRecord record = lookupSupplierRecord(cartItem.getItem().getId().getSupplierId());
			if (record == null) {
				droppedItems.add(cartItem);
				continue;
			}
			try {
				SupplierClient client = new SupplierClient(record.getUrl());
				try {
					client.buyProduct(cartItem.getItem().getId().getProductId(), cartItem.getQuantity());
					purchasedItems.add(cartItem);
				} catch (BadProductId_Exception | BadQuantity_Exception | InsufficientQuantity_Exception e) {
					droppedItems.add(cartItem);
				}
			} catch (SupplierClientException | WebServiceException e) {
				System.err.printf("%s didn't not respond to buyProduct in the buyCart operation.", record.getOrgName());
				droppedItems.add(cartItem);
			}
		}
		return newShoppingResultView(Mediator.getInstance().registerPurchase(cartId, purchasedItems, droppedItems));
	}

	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		if (cartId == null) {
			throwInvalidCartId("Cart id cannot be null!");
		}
		cartId = cartId.trim();
		if (cartId.length() == 0) {
			throwInvalidCartId("Cart id cannot be empty or whitespace!");
		}
		if (!isAlphanumeric(cartId)) {
			throwInvalidCartId("Cart id must be alphanumeric!");
		}
		if (itemId == null) {
			throwInvalidItemId("Item id cannot be null!");
		}
		String productId = itemId.getProductId();
		String supplierId = itemId.getSupplierId();
		if (productId == null || supplierId == null) {
			throwInvalidItemId("Ids cannot be null!");
		}
		productId = productId.trim();
		supplierId = supplierId.trim();
		if (productId.length() == 0 || supplierId.length() == 0) {
			throwInvalidItemId("Ids cannot be empty or whitespace!");
		}
		if (!isAlphanumeric(productId)) {
			throwInvalidItemId("Product id must be alphanumeric!");
		}
		if (!isAlphanumericWithUnderscore(supplierId)) {
			throwInvalidItemId("Supplier id must be alphanumeric and may contain underscores!");
		}
		if (itemQty <= 0) {
			throwInvalidQuantity("Quantity cannot be zero or negative!");
		}

		UDDIRecord record = lookupSupplierRecord(supplierId);
		if (record == null) throwInvalidItemId(String.format("Could't lookup %s", supplierId));
		try {
			SupplierClient client = new SupplierClient(record.getUrl());
			try {
				ProductView productView = client.getProduct(productId);
				if (productView == null) throwInvalidItemId("Supplier doesn't have that product!");
				if (!Mediator.getInstance().testAndAddToCart(cartId, productId, supplierId, productView.getDesc(), productView.getPrice(), itemQty, productView.getQuantity())) {
					throwNotEnoughItems("Supplier doesn't have enough quantity!");
				}
			} catch (BadProductId_Exception e) {
				throwInvalidItemId("Invalid product id!");
			}
		} catch (SupplierClientException | WebServiceException e) {
			System.err.printf("%s didn't not respond to getProduct in the addToCart operation.", record.getOrgName());
			throwInvalidItemId("Couldn't confirm product with supplier!");
		}
	}

	// Auxiliary operations --------------------------------------------------

	@Override
	public String ping(String name) {
		if (name == null || name.trim().length() == 0)
			name = "friend";

		String wsName = endpointManager.getWsName();
		StringBuilder result = new StringBuilder();
		result.append("Hello ").append(name).append(" from ").append(wsName).append(System.lineSeparator());

		Collection<UDDIRecord> uddiRecords = listSupplierRecords();
		result.append("Found ").append(uddiRecords.size()).append(" suppliers:").append(System.lineSeparator());
		Iterator<UDDIRecord> uddiRecordsIterator = uddiRecords.iterator();
		SupplierClient client;
		while (uddiRecordsIterator.hasNext()) {
			UDDIRecord record = uddiRecordsIterator.next();
			result.append("  - ").append(record.getOrgName()).append(": ");
			try {
				client = new SupplierClient(record.getUrl());
				result.append(client.ping(name));
			} catch (SupplierClientException | WebServiceException e) {
				System.err.printf("%s didn't not respond to ping in the ping operation.", record.getOrgName());
				result.append("Not responding...");
			}
			if (uddiRecordsIterator.hasNext())
				result.append(System.lineSeparator());
		}
		return result.toString();
	}

	@Override
	public void clear() {
		Mediator.getInstance().reset();
		Collection<UDDIRecord> uddiRecords = listSupplierRecords();
		SupplierClient client;
		for (UDDIRecord record : uddiRecords) {
			try {
				client = new SupplierClient(record.getUrl());
				client.clear();
			} catch (SupplierClientException | WebServiceException e) {
				System.err.printf("%s didn't not respond to clear in the clear operation.", record.getOrgName());
			}
		}
	}

	@Override
	public List<CartView> listCarts() {
		return Mediator.getInstance().getCarts().stream().map(this::newCartView).collect(Collectors.toList());
	}

	@Override
	public List<ShoppingResultView> shopHistory() {
		return Mediator.getInstance().getShoppingResults().stream()
				.sorted(Comparator.comparing(ShoppingResult::getTimestamp).reversed())
				.map(this::newShoppingResultView).collect(Collectors.toList());
	}

	// Helpers ---------------------------------------------------------

	private UDDIRecord lookupSupplierRecord(String supplierName) {
		try {
			if (suppliersUddi == null)
				suppliersUddi = new UDDINaming(suppliersUddiUrl);
			return suppliersUddi.lookupRecord(supplierName);
		} catch (UDDINamingException e) {
			System.err.printf("Failed to lookup on UDDI at %s!", suppliersUddiUrl);
			e.printStackTrace();
		}
		return null;
	}

	private Collection<UDDIRecord> listSupplierRecords() {
		try {
			if (suppliersUddi == null)
				suppliersUddi = new UDDINaming(suppliersUddiUrl);
			return suppliersUddi.listRecords(suppliersWsNameFormat);
		} catch (UDDINamingException e) {
			System.err.printf("Failed to lookup on UDDI at %s!", suppliersUddiUrl);
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	private boolean isAlphanumericWithUnderscore(String text) {
		return text.matches("[a-zA-Z0-9_]+");
	}

	private boolean isAlphanumeric(String text) {
		return text.matches("[a-zA-Z0-9]+");
	}

	// View helpers -----------------------------------------------------

	private ShoppingResultView newShoppingResultView(ShoppingResult shoppingResult) {
		ShoppingResultView view = new ShoppingResultView();
		view.setId(shoppingResult.getId());
		view.setResult(Result.fromValue(shoppingResult.getResult().value()));
		view.setTotalPrice(shoppingResult.getTotalPrice());
		view.getPurchasedItems().addAll(shoppingResult.getPurchasedItems().stream().map(this::newCartItemView).collect(Collectors.toList()));
		view.getDroppedItems().addAll(shoppingResult.getDroppedItems().stream().map(this::newCartItemView).collect(Collectors.toList()));
		return view;
	}

	private CartView newCartView(Cart cart) {
		CartView view = new CartView();
		view.setCartId(cart.getId());
		view.getItems().addAll(cart.getItems().stream().map(this::newCartItemView).collect(Collectors.toList()));
		return view;
	}

	private CartItemView newCartItemView(CartItem item) {
		CartItemView view = new CartItemView();
		view.setItem(newItemView(item.getItem()));
		view.setQuantity(item.getQuantity());
		return view;
	}

	private ItemView newItemView(Item item) {
		ItemView view = new ItemView();
		view.setDesc(item.getDesc());
		view.setItemId(newItemIdView(item.getId()));
		view.setPrice(item.getPrice());
		return view;
	}

	private ItemIdView newItemIdView(ItemId itemId) {
		ItemIdView view = new ItemIdView();
		view.setProductId(itemId.getProductId());
		view.setSupplierId(itemId.getSupplierId());
		return view;
	}

	// Exception helpers -----------------------------------------------------

	private void throwInvalidItemId(final String message) throws InvalidItemId_Exception {
		InvalidItemId faultInfo = new InvalidItemId();
		faultInfo.setMessage(message);
		throw new InvalidItemId_Exception(message, faultInfo);
	}

	private void throwInvalidText(final String message) throws InvalidText_Exception {
		InvalidText faultInfo = new InvalidText();
		faultInfo.setMessage(message);
		throw new InvalidText_Exception(message, faultInfo);
	}

	private void throwInvalidCartId(final String message) throws InvalidCartId_Exception {
		InvalidCartId faultInfo = new InvalidCartId();
		faultInfo.setMessage(message);
		throw new InvalidCartId_Exception(message, faultInfo);
	}

	private void throwInvalidQuantity(final String message) throws InvalidQuantity_Exception {
		InvalidQuantity faultInfo = new InvalidQuantity();
		faultInfo.setMessage(message);
		throw new InvalidQuantity_Exception(message, faultInfo);
	}

	private void throwInvalidCreditCard(final String message) throws InvalidCreditCard_Exception {
		InvalidCreditCard faultInfo = new InvalidCreditCard();
		faultInfo.setMessage(message);
		throw new InvalidCreditCard_Exception(message, faultInfo);
	}

	private void throwNotEnoughItems(final String message) throws NotEnoughItems_Exception {
		NotEnoughItems faultInfo = new NotEnoughItems();
		faultInfo.setMessage(message);
		throw new NotEnoughItems_Exception(message, faultInfo);
	}

	private void throwEmptyCartException(final String message) throws EmptyCart_Exception {
		EmptyCart faultInfo = new EmptyCart();
		faultInfo.setMessage(message);
		throw new EmptyCart_Exception(message, faultInfo);
	}

}