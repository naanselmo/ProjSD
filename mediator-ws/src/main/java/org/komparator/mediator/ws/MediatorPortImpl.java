package org.komparator.mediator.ws;

import javax.jws.WebService;
import java.util.List;

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
	public void clear() {

	}

	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		return null;
	}

	@Override
	public List<CartView> listCarts() {
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

	@Override
	public String ping(String arg0) {
		// Consultar o UDDI para pesquisar fornecedores.
		// Criar um SupplierClient para cada fornecedor encontrado.
		// Chamar a operação ping de cada um.
		// Juntar as respostas e devolver como resultado.
		return null;
	}

	@Override
	public List<ShoppingResultView> shopHistory() {
		return null;
	}

	// Auxiliary operations --------------------------------------------------	

	// TODO

	// View helpers -----------------------------------------------------

	// TODO


	// Exception helpers -----------------------------------------------------

	// TODO

}