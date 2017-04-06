package org.komparator.mediator.ws.it;

import java.util.List;

import org.komparator.mediator.ws.*;
import org.komparator.supplier.ws.*;
import org.komparator.supplier.ws.cli.SupplierClient;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * Test suite
 */
public class AddToCartIT extends BaseWithSuppliersIT {

    @Before
    public void setUp() throws BadProductId_Exception, BadProduct_Exception {
        {
            ProductView product = new ProductView();
    		product.setId("TP");
    		product.setDesc("TestProd");
    		product.setPrice(1);
    		product.setQuantity(5);
            supplierClients[0].createProduct(product);
        }
        {
            ProductView product = new ProductView();
    		product.setId("TP2");
    		product.setDesc("TestProd2");
    		product.setPrice(1);
    		product.setQuantity(7);
            supplierClients[1].createProduct(product);
        }
    }

    @After
    public void tearDown() {
        mediatorClient.clear();
    }

    // Input tests: test for bad input

    @Test(expected = InvalidCartId_Exception.class)
    public void nullCartNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart(null, itemId, 1);
    }

    @Test
    public void differentCaseCartNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
        mediatorClient.addToCart("tc", itemId, 1);
        assertEquals(2, mediatorClient.listCarts().size());
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void whitespaceCartNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart(" ", itemId, 1);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void containsSpacesCartNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TE ST", itemId, 1);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void containsSymbolsCartNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("T&ST", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void nullItemIdTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        mediatorClient.addToCart("TC", null, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void nullItemNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId(null);
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void differentCaseItemNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("tP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void whitespaceItemNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId(" ");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void containsSpacesItemNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("Te st");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void containsSymbolsItemNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("T&P");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void nullSupplierNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(null);
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void differentCaseSupplierNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId("t04_supplier1");
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void whitespaceSupplierNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(" ");
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void containsSpacesSupplierNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId("T04 Supplier1");
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void containsSymbolsSupplierNameTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId("T04&Supplier1");
        mediatorClient.addToCart("TC", itemId, 1);
    }

    @Test(expected = InvalidQuantity_Exception.class)
    public void negativeQuantityTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, -1);
    }

    @Test(expected = InvalidQuantity_Exception.class)
    public void zeroQuantityTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 0);
    }

    @Test(expected = NotEnoughItems_Exception.class)
    public void tooManyQuantityTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, 10);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void validateCartBeforeItemTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId(null);
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart(null, itemId, 1);
    }

    @Test(expected = InvalidCartId_Exception.class)
    public void validateCartBeforeSupplierTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(null);
        mediatorClient.addToCart(null, itemId, 1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void validateItemBeforeQuantityTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId(null);
        itemId.setSupplierId(supplierNames[0]);
        mediatorClient.addToCart("TC", itemId, -1);
    }

    @Test(expected = InvalidItemId_Exception.class)
    public void validateSupplierBeforeQuantityTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        ItemIdView itemId = new ItemIdView();
        itemId.setProductId("TP");
        itemId.setSupplierId(null);
        mediatorClient.addToCart("TC", itemId, -1);
    }

    // Behavioral tests: check if it actually does what we want

    @Test
    public void newCartTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(supplierNames[0]);
            mediatorClient.addToCart("TC", itemId, 3);
        }
        List<CartView> results = mediatorClient.listCarts();
        assertEquals(1, results.size());
        assertEquals("TC", results.get(0).getCartId());
        assertEquals(1, results.get(0).getItems().size());
        assertEquals(3, results.get(0).getItems().get(0).getQuantity());
        assertEquals("TP", results.get(0).getItems().get(0).getItem().getItemId().getProductId());
        assertEquals(supplierNames[0], results.get(0).getItems().get(0).getItem().getItemId().getSupplierId());
    }

    @Test
    public void existingCartTest() throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP");
            itemId.setSupplierId(supplierNames[0]);
            mediatorClient.addToCart("TC", itemId, 3);
        }
        assertEquals(1, mediatorClient.listCarts().size());

        {
            ItemIdView itemId = new ItemIdView();
            itemId.setProductId("TP2");
            itemId.setSupplierId(supplierNames[1]);
            mediatorClient.addToCart("TC", itemId, 2);
        }
        List<CartView> results = mediatorClient.listCarts();
        assertEquals(1, results.size());
        assertEquals("TC", results.get(0).getCartId());
        assertEquals(2, results.get(0).getItems().size());
    }

}
