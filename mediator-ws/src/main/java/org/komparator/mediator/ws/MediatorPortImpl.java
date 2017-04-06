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

		List<ItemView> result = new ArrayList<>();
		String suppliersUddiUrl = MediatorConfig.getProperty(MediatorConfig.PROPERTY_SUPPLIERS_UDDI_URL);
		String suppliersWsNameFormat = MediatorConfig.getProperty(MediatorConfig.PROPERTY_WS_NAME_FORMAT);
		try {
			UDDINaming uddiNaming = new UDDINaming(suppliersUddiUrl);
			Collection<UDDIRecord> uddiRecords = uddiNaming.listRecords(suppliersWsNameFormat);
			SupplierClient client;
			for (UDDIRecord record : uddiRecords) {
				try {
					client = new SupplierClient(record.getUrl());
					try {
						ProductView productView = client.getProduct(productId);
						Item item = new Item(productView.getId(), record.getOrgName(), productView.getDesc(), productView.getPrice());
						result.add(newItemView(item));
					} catch (BadProductId_Exception e) {
						throwInvalidItemId("Invalid product id!");
					}
				} catch (SupplierClientException | WebServiceException e) {
					// TODO
				}
			}
		} catch (UDDINamingException e) {
			String msg = String.format("Failed to lookup Suppliers on UDDI at %s!", suppliersUddiUrl);
			System.err.println(msg);
			e.printStackTrace();
		}
		Collections.sort(result, Comparator.comparingInt(ItemView::getPrice));
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
		String suppliersUddiUrl = MediatorConfig.getProperty(MediatorConfig.PROPERTY_SUPPLIERS_UDDI_URL);
		String suppliersWsNameFormat = MediatorConfig.getProperty(MediatorConfig.PROPERTY_WS_NAME_FORMAT);
		try {
			UDDINaming uddiNaming = new UDDINaming(suppliersUddiUrl);
			Collection<UDDIRecord> uddiRecords = uddiNaming.listRecords(suppliersWsNameFormat);
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
						throwInvalidText("Invalid product text!");
					}
				} catch (SupplierClientException | WebServiceException e) {
					//TODO
				}
			}

		} catch (UDDINamingException e) {
			String msg = String.format("Failed to lookup Suppliers on UDDI at %s!", suppliersUddiUrl);
			System.err.println(msg);
			e.printStackTrace();
		}

		Collections.sort(result, (itemView1, itemView2) -> {
			int nameComparison = itemView1.getItemId().getProductId().compareTo(itemView2.getItemId().getProductId());
			if (nameComparison != 0)
				return nameComparison;
			return itemView1.getPrice() - itemView2.getPrice();
		});
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
		if (cart.getItems().size() == 0) {
			throwEmptyCartException("The cart can't be empty!");
		}

		List<CartItem> purchasedItems = new ArrayList<>();
		List<CartItem> droppedItems = new ArrayList<>();
		String suppliersUddiUrl = MediatorConfig.getProperty(MediatorConfig.PROPERTY_SUPPLIERS_UDDI_URL);
		try {
			UDDINaming uddiNaming = new UDDINaming(suppliersUddiUrl);
			for (CartItem cartItem : cart.getItems()) {
				UDDIRecord record = uddiNaming.lookupRecord(cartItem.getItem().getId().getSupplierId());
				try {
					SupplierClient client = new SupplierClient(record.getUrl());
					try {
						client.buyProduct(cartItem.getItem().getId().getProductId(), cartItem.getQuantity());
						purchasedItems.add(cartItem);
					} catch (BadProductId_Exception | BadQuantity_Exception | InsufficientQuantity_Exception e) {
						droppedItems.add(cartItem);
					}
				} catch (SupplierClientException | WebServiceException e) {
					droppedItems.add(cartItem);
				}
			}
		} catch (UDDINamingException e) {
			String msg = String.format("Failed to lookup Suppliers on UDDI at %s!", suppliersUddiUrl);
			System.err.println(msg);
			e.printStackTrace();
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
		if (itemQty <= 0) {
			throwInvalidQuantity("Quantity cannot be zero or negative!");
		}

		String suppliersUddiUrl = MediatorConfig.getProperty(MediatorConfig.PROPERTY_SUPPLIERS_UDDI_URL);
		String suppliersWsNameFormat = MediatorConfig.getProperty(MediatorConfig.PROPERTY_WS_NAME_FORMAT);
		try {
			UDDINaming uddiNaming = new UDDINaming(suppliersUddiUrl);
			UDDIRecord record = uddiNaming.lookupRecord(suppliersWsNameFormat);
			SupplierClient client;
			try {
				client = new SupplierClient(record.getUrl());
				try {
					ProductView productView = client.getProduct(productId);
					int quantity = Mediator.getInstance().getItemQuantity(cartId, productId, supplierId) + itemQty;
					if (productView.getQuantity() > quantity) {
						Mediator.getInstance().addToCart(cartId, productId, supplierId, productView.getDesc(), productView.getPrice(), itemQty);
					} else {
						throwNotEnoughItems("Supplier doesn't have enough quantity!");
					}
				} catch (BadProductId_Exception e) {
					throwInvalidItemId("Invalid product id!");
				}
			} catch (SupplierClientException | WebServiceException e) {
				//TODO
			}
		} catch (UDDINamingException e) {
			String msg = String.format("Failed to lookup Suppliers on UDDI at %s!", suppliersUddiUrl);
			System.err.println(msg);
			e.printStackTrace();
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

		String suppliersUddiUrl = MediatorConfig.getProperty(MediatorConfig.PROPERTY_SUPPLIERS_UDDI_URL);
		String suppliersWsNameFormat = MediatorConfig.getProperty(MediatorConfig.PROPERTY_WS_NAME_FORMAT);
		try {
			UDDINaming uddiNaming = new UDDINaming(suppliersUddiUrl);
			Collection<UDDIRecord> uddiRecords = uddiNaming.listRecords(suppliersWsNameFormat);
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
					result.append("Not responding...");
				}
				if (uddiRecordsIterator.hasNext())
					result.append(System.lineSeparator());
			}
		} catch (UDDINamingException e) {
			System.err.printf("Failed to lookup Suppliers on UDDI at %s!", suppliersUddiUrl);
			e.printStackTrace();
			result.append("Unable to search for suppliers.");
		}
		return result.toString();
	}

	@Override
	public void clear() {
		Mediator.getInstance().reset();
		String suppliersUddiUrl = MediatorConfig.getProperty(MediatorConfig.PROPERTY_SUPPLIERS_UDDI_URL);
		String suppliersWsNameFormat = MediatorConfig.getProperty(MediatorConfig.PROPERTY_WS_NAME_FORMAT);
		try {
			UDDINaming uddiNaming = new UDDINaming(suppliersUddiUrl);
			Collection<UDDIRecord> uddiRecords = uddiNaming.listRecords(suppliersWsNameFormat);
			SupplierClient client;
			for (UDDIRecord record : uddiRecords) {
				try {
					client = new SupplierClient(record.getUrl());
					client.clear();
				} catch (SupplierClientException | WebServiceException ignored) {
				}
			}
		} catch (UDDINamingException e) {
			System.err.printf("Failed to lookup Suppliers on UDDI at %s!", suppliersUddiUrl);
			e.printStackTrace();
		}
	}

	@Override
	public List<CartView> listCarts() {
		return Mediator.getInstance().getCarts().stream().map(this::newCartView).collect(Collectors.toList());
	}

	@Override
	public List<ShoppingResultView> shopHistory() {
		return Mediator.getInstance().getShoppingResults().stream().map(this::newShoppingResultView).collect(Collectors.toList());
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