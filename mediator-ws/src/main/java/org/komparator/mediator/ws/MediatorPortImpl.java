package org.komparator.mediator.ws;

import org.komparator.mediator.domain.*;
import org.komparator.supplier.ws.SupplierPortType;
import org.komparator.supplier.ws.SupplierService;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

import javax.jws.WebService;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

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
		return null;
	}

	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		return null;
	}

	@Override
	public ShoppingResultView buyCart(String cartId, String creditCardNr) throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		return null;
	}

	@Override
	public void addToCart(String cartId, ItemIdView itemId, int itemQty) throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {

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

	// Proxy helpers ---------------------------------------------------------

	private SupplierPortType createSupplierProxy(String wsUrl) {
		if (wsUrl == null)
			throw new IllegalArgumentException("Webservice URL can't be null.");
		SupplierService supplierService = new SupplierService();
		SupplierPortType port = supplierService.getSupplierPort();
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsUrl);
		return port;
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

	// TODO

}