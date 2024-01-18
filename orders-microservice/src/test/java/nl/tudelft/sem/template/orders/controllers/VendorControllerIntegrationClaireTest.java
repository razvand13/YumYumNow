package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.model.Address;
import nl.tudelft.sem.template.model.CreateOrderRequest;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.external.PaymentMock;
import nl.tudelft.sem.template.orders.services.OrderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class VendorControllerIntegrationClaireTest {
    @Autowired
    private VendorController vendorController;
    @Autowired
    private CustomerController customerController;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentMock paymentMock;

    private final UUID vendorUUID1 = UUID.fromString("5db89f20-bb0e-4166-af07-ebef17dd78a9");
    private final UUID vendorUUID2 = UUID.fromString("94f95e69-81b7-4c35-932e-9e4baa8dcec2");
    private final UUID customerUUID1 = UUID.fromString("c00b0bcf-189a-45c7-afff-28a130e661a0");
    private final UUID customerUUID2 = UUID.fromString("258dce56-56dc-402c-8fc7-7375d6715b0c");

    List<UUID> orderIDList = new ArrayList<>();

    /**
     * Set up some orders in the database
     */
    @BeforeAll
    public void setup() {
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setVendorId(vendorUUID1);

        Address address = new Address();
        address.setLatitude(34.02);
        address.setLongitude(34.00);
        address.setZipCode("2222AB");
        address.setHouseNumber(42);

        createOrderRequest.setAddress(address);

        Order order = customerController.createOrder(customerUUID1,
                createOrderRequest).getBody();
        orderIDList.add(order.getID());

        Order order2 = customerController.createOrder(customerUUID2,
                createOrderRequest).getBody();
        orderIDList.add(order2.getID());

        Order order3 = customerController.createOrder(customerUUID1,
                createOrderRequest).getBody();

        paymentMock.pay(order.getID(), null);
        paymentMock.pay(order2.getID(), null);
    }

    @Test
    public void testGetOrderDetailsSingleOrder() {
        ResponseEntity<Order> response = vendorController.getOrderDetails(vendorUUID1, orderIDList.get(0));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(orderService.findById(orderIDList.get(0)));
    }

    @Test
    public void testGetOrderDetailsOrderNotFound() {
        ResponseEntity<Order> response = vendorController.getOrderDetails(vendorUUID1, UUID.randomUUID());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testGetOrderDetailsOrderNotFromVendor() {
        ResponseEntity<Order> response = vendorController.getOrderDetails(vendorUUID2, orderIDList.get(0));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testGetVendorOrders() {
        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(vendorUUID1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(orderService.findById(orderIDList.get(0)));
        assertThat(response.getBody()).contains(orderService.findById(orderIDList.get(1)));
    }

    @Test
    public void testGetVendorOrdersNoOrders() {
        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(vendorUUID2);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    public void testGetVendorOrdersNonexistentVendor() {
        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(UUID.randomUUID());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
