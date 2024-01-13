package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.model.*;
import nl.tudelft.sem.template.orders.external.CustomerDTO;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CustomerIntegrationTest {

    @Autowired
    private OrderController orderController;
    @Autowired
    private CustomerController customerController;

    @Autowired
    private OrderRepository orderRepo;
    @Autowired
    private DishRepository dishRepo;

    private final UUID customerId = UUID.fromString("c00b0bcf-189a-45c7-afff-28a130e661a0");
    private final UUID vendorId = UUID.fromString("5db89f20-bb0e-4166-af07-ebef17dd78a9");

    /**
     * https://gyyl7.wiremockapi.cloud/vendors/5db89f20-bb0e-4166-af07-ebef17dd78a9
     * @return example vendor
     */
    Vendor exampleVendor() {
        Vendor vendor = new Vendor();
        vendor.setID(vendorId);
        vendor.setName("Arthur Dent");

        Address address = new Address();
        address.setLatitude(34.092);
        address.setLongitude(34.092);
        address.setZip("2554EZ");
        address.setHouseNumber(24);

        vendor.setLocation(address);

        // TODO add dishes to menu

        return vendor;
    }

    /**
     * https://gyyl7.wiremockapi.cloud/customers/c00b0bcf-189a-45c7-afff-28a130e661a0
     * @return example customer
     */
    CustomerDTO exampleCustomer() {
        Address homeAddress = new Address();
        homeAddress.setLatitude(34.092);
        homeAddress.setLongitude(34.092);
        homeAddress.setZip("2554EZ");
        homeAddress.setHouseNumber(24);

        Address currentLocation = new Address();
        currentLocation.setLatitude(34.092);
        currentLocation.setLongitude(34.092);
        currentLocation.setZip("2554EZ");
        currentLocation.setHouseNumber(24);

        CustomerDTO customer = new CustomerDTO(customerId, "Arthur Dent", "Tempie.Farrell@email.example.mocklab.io", true,
                "Visa Debit", homeAddress, List.of("9r4x25i52y7dayiodddgk0bfb2yx3z15pgtk0atrfa6a2u0azp8sshnpt"), currentLocation);

        return customer;
    }

    Order createOrder() {
        // Create an order object
        Order order = new Order();

        Address address = new Address();
        address.setHouseNumber(1);
        address.setLongitude(10.0);
        address.setLatitude(15.0);
        address.setZip("1234AB");

        // TODO add dishes to order
//        Dish dish = new Dish();
//        dish.setName("Chicken & Rice");
//        dish.setVendorId(vendorId);
//        dish.setPrice(10.0);
//        dish.setIngredients(List.of("Chicken", "Rice"));
//        dish.setDescription("yum");
//        dish.setImageLink("www.images.com/chicken-and-rice");
//
//        OrderedDish orderedDish = new OrderedDish();
//        orderedDish.setId(UUID.randomUUID());
//        orderedDish.setDish(dish);
//        orderedDish.setQuantity(1);
//
//        order.addDishesItem(orderedDish);
        order.setLocation(address);
        order.setSpecialRequirements("Leave it at the door");
        order.setVendorId(vendorId);
        order.setCustomerId(customerId);
        order.setStatus(Status.GIVENTOCOURIER);
        order.setTotalPrice(0.0);

        return orderRepo.save(order);
    }

    @Test
    void testDependencyInjection() {
        assertThat(orderController).isNotNull();
        assertThat(customerController).isNotNull();

        assertThat(orderRepo).isNotNull();
        assertThat(dishRepo).isNotNull();
    }

    @Test
    void testUpdateOrderStatusOk() {
        // Create order and set (some) fields, including the status field
        Order order = createOrder();
        UUID orderId = order.getID();

        // Create request object
        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(Status.DELIVERED);

        // Assert that it was saved
        Optional<Order> orderOpt = orderRepo.findById(orderId);
        assertThat(orderOpt.isPresent()).isTrue();

        // Call the method
        var res = orderController.updateOrderStatus(orderId, req);
        Order orderRes = res.getBody();

        // Assert that the only status was changed
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(orderRes).isNotNull();
        assertThat(orderRes.getStatus()).isEqualTo(Status.DELIVERED);
    }

    @Test
    void testGetVendorsOkNoFilters() {
        // Call method
        var res = customerController.getVendors(customerId, null, null ,null);
        var vendors = res.getBody();

        Vendor exampleVendor = exampleVendor();

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(vendors).isNotNull();
        assertThat(vendors).isNotEmpty();
        assertThat(vendors).contains(exampleVendor);
    }

    @Test
    void testGetVendorsOkFiltersContains() {
        // Call method
        var res = customerController.getVendors(customerId, "Arth", null ,null);
        var vendors = res.getBody();

        Vendor exampleVendor = exampleVendor();

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(vendors).isNotNull();
        assertThat(vendors).isNotEmpty();
        assertThat(vendors).contains(exampleVendor);
    }

    @Test
    void testGetVendorsOkNoFiltersNotContains() {
        // Call method
        var res = customerController.getVendors(customerId, "Arthr Dent", null ,null);
        List<Vendor> vendors = res.getBody();

        Vendor exampleVendor = exampleVendor();

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(vendors).isNotNull();
        assertThat(vendors).isNotEmpty();
        assertThat(vendors).contains(exampleVendor);
    }

    @Test
    void testCreateOrderOk() {
        Address deliveryAddress = new Address();
        deliveryAddress.setLongitude(34.0);
        deliveryAddress.setLatitude(34.0);

        // Create request object
        CreateOrderRequest req = new CreateOrderRequest();
        req.setVendorId(vendorId);
        req.setAddress(deliveryAddress);

        // Call method
        var res = customerController.createOrder(customerId, req);
        Order order = res.getBody();

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(order).isNotNull();
        assertThat(order.getID()).isNotNull();
        assertThat(order.getVendorId()).isEqualTo(vendorId);
        assertThat(order.getLocation()).isEqualTo(deliveryAddress);
    }

    @Test
    void testGetVendorDishesOk() {
        // TODO
    }

    @Test
    void testGetOrderOk() {
        // TODO
    }

    @Test
    void testGetDishFromOrderOk() {
        // TODO
    }

    // TODO write some integration tests for error responses
}
