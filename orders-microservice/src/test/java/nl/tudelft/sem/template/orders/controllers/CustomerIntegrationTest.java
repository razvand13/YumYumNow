package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.model.*;
import nl.tudelft.sem.template.orders.external.CustomerDTO;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import nl.tudelft.sem.template.orders.services.CustomerAdapter;
import nl.tudelft.sem.template.orders.services.OrderService;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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

    Order createOrder() {
        // Create an order object
        Order order = new Order();

        Address address = new Address();
        address.setHouseNumber(1);
        address.setLongitude(10.0);
        address.setLatitude(15.0);
        address.setZip("1234AB");

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

    Vendor createVendor() {
        // Creates example vendor

        Vendor vendor = new Vendor();
        vendor.setID(vendorId);
        vendor.setName("Arthur Dent");

        Address address = new Address();
        address.setLatitude(34.092);
        address.setLongitude(34.092);
        address.setZip("2554EZ");
        address.setHouseNumber(24);

        vendor.setLocation(address);

        return vendor;
    }

    @Test
    void testDependencyInjection() {
        assertThat(orderController).isNotNull();
        assertThat(customerController).isNotNull();

        assertThat(orderRepo).isNotNull();
        assertThat(dishRepo).isNotNull();
    }

    @Test
    void testUpdateOrderStatus() {
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
        assertThat(orderRes).isNotNull();
        assertThat(orderRes.getStatus()).isEqualTo(Status.DELIVERED);
    }

    @Test
    void testGetVendorsNoFilters() {
        // Call method
        var res = customerController.getVendors(customerId, null, null ,null);
        var vendors = res.getBody();

        Vendor exampleVendor = createVendor();

        assertThat(vendors).isNotNull();
        assertThat(vendors).isNotEmpty();
        assertThat(vendors).contains(exampleVendor);
    }

    @Test
    void testGetVendorsFiltersContains() {
        // Call method
        var res = customerController.getVendors(customerId, "Arth", null ,null);
        var vendors = res.getBody();

        Vendor exampleVendor = createVendor();

        assertThat(vendors).isNotNull();
        assertThat(vendors).isNotEmpty();
        assertThat(vendors).contains(exampleVendor);
    }

    @Test
    void testGetVendorsNoFiltersNotContains() {
        // Call method
        var res = customerController.getVendors(customerId, "Arthr Dent", null ,null);
        List<Vendor> vendors = res.getBody();

        Vendor exampleVendor = createVendor();

        assertThat(vendors).isNotNull();
        assertThat(vendors).isNotEmpty();
        assertThat(vendors).contains(exampleVendor);
    }

    @Test
    void testCreateOrder() {
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

        assertThat(order).isNotNull();
        assertThat(order.getID()).isNotNull();
        assertThat(order.getVendorId()).isEqualTo(vendorId);
        assertThat(order.getLocation()).isEqualTo(deliveryAddress);
    }
}
