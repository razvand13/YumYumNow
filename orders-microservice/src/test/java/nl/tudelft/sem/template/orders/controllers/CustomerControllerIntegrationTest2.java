//package nl.tudelft.sem.template.orders.controllers;
//
//import nl.tudelft.sem.template.model.Dish;
//import nl.tudelft.sem.template.model.Vendor;
//import nl.tudelft.sem.template.model.Order;
//import nl.tudelft.sem.template.model.OrderedDish;
//import nl.tudelft.sem.template.model.Status;
//import nl.tudelft.sem.template.model.Address;
//import nl.tudelft.sem.template.model.UpdateDishQtyRequest;
//import nl.tudelft.sem.template.model.UpdateSpecialRequirementsRequest;
//import nl.tudelft.sem.template.orders.repositories.DishRepository;
//import nl.tudelft.sem.template.orders.repositories.OrderRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@Transactional
//public class CustomerControllerIntegrationTest2 {
//
//    @Autowired
//    private CustomerController customerController;
//
//    @Autowired
//    private OrderRepository orderRepo;
//    @Autowired
//    private DishRepository dishRepo;
//
//    private final UUID customerId = UUID.fromString("c00b0bcf-189a-45c7-afff-28a130e661a0");
//    private final UUID vendorId = UUID.fromString("5db89f20-bb0e-4166-af07-ebef17dd78a9");
//
//    Order createOrder() {
//        Address address = new Address();
//        address.setHouseNumber(1);
//        address.setLongitude(10.0);
//        address.setLatitude(15.0);
//        address.setZipCode("1234AB");
//
//        Dish dish = createDish();
//
//        OrderedDish orderedDish = new OrderedDish();
//        orderedDish.setDish(dish);
//        orderedDish.setQuantity(1);
//
//        Order order = new Order();
//        order.addDishesItem(orderedDish);
//        order.setLocation(address);
//        order.setSpecialRequirements("Leave it at the door");
//        order.setVendorId(vendorId);
//        order.setCustomerId(customerId);
//        order.setStatus(Status.GIVENTOCOURIER);
//        order.setTotalPrice(0.0);
//
//        return orderRepo.save(order);
//    }
//
//    Dish createDish() {
//        Dish dish = new Dish();
//        dish.setName("Chicken & Rice");
//        dish.setVendorId(vendorId);
//        dish.setPrice(10.0);
//        dish.setIngredients(List.of("Chicken", "Rice"));
//        dish.setDescription("yum");
//        dish.setImageLink("www.images.com/chicken-and-rice");
//        dish.setAllergens(List.of("Gluten"));
//
//        return dishRepo.save(dish);
//    }
//
//    @Test
//    void testAddDishToOrderOk() {
//        Order order = createOrder();
//        Dish dish = createDish();
//
//        UpdateDishQtyRequest req = new UpdateDishQtyRequest();
//        req.setQuantity(2);
//
//        var res = customerController.addDishToOrder(customerId, order.getID(), dish.getID(), req);
//        Order updatedOrder = res.getBody();
//
//        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(updatedOrder).isNotNull();
//        assertThat(updatedOrder.getDishes().size()).isGreaterThan(0);
//    }
//
//    @Test
//    void testAddDishToOrderBadRequest() {
//        var res = customerController.addDishToOrder(null, null, null, null);
//        var body = res.getBody();
//        var statusCode = res.getStatusCode();
//
//        assertThat(res).isNotNull();
//        assertThat(body).isNull();
//        assertThat(statusCode).isEqualTo(HttpStatus.BAD_REQUEST);
//    }
//
//
//    @Test
//    void testRemoveDishFromOrderBadRequest() {
//        var res = customerController.removeDishFromOrder(null, null, null);
//        var statusCode = res.getStatusCode();
//
//        assertThat(res).isNotNull();
//        assertThat(statusCode).isEqualTo(HttpStatus.BAD_REQUEST);
//    }
//
//    @Test
//    void testGetPersonalOrderHistoryOk() {
//        createOrder();
//        createOrder();
//
//        var res = customerController.getPersonalOrderHistory(customerId);
//        List<Order> orders = res.getBody();
//
//        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(orders).isNotEmpty();
//    }
//
//    @Test
//    void testGetPersonalOrderHistoryBadRequest() {
//        var res = customerController.getPersonalOrderHistory(null);
//        var statusCode = res.getStatusCode();
//
//        assertThat(res).isNotNull();
//        assertThat(statusCode).isEqualTo(HttpStatus.BAD_REQUEST);
//    }
//
//    @Test
//    void testReorderOk() {
//        Order originalOrder = createOrder();
//
//        var res = customerController.reorder(customerId, originalOrder.getID(), originalOrder.getLocation());
//        Order newOrder = res.getBody();
//
//        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(newOrder).isNotNull();
//        assertThat(newOrder.getID()).isNotEqualTo(originalOrder.getID());
//        assertThat(newOrder.getVendorId()).isEqualTo(originalOrder.getVendorId());
//    }
//
//    @Test
//    void testReorderBadRequest() {
//        var res = customerController.reorder(null, null, null);
//        var statusCode = res.getStatusCode();
//
//        assertThat(res).isNotNull();
//        assertThat(statusCode).isEqualTo(HttpStatus.BAD_REQUEST);
//    }
//
//    @Test
//    void testUpdateSpecialRequirementsOk() {
//        Order order = createOrder();
//        UUID orderId = order.getID();
//
//        UpdateSpecialRequirementsRequest req = new UpdateSpecialRequirementsRequest();
//        req.setSpecialRequirements("No onions, please");
//
//        var res = customerController.updateSpecialRequirements(customerId, orderId, req);
//        Order updatedOrder = res.getBody();
//
//        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(updatedOrder).isNotNull();
//        assertThat(updatedOrder.getSpecialRequirements()).isEqualTo("No onions, please");
//    }
//
//    @Test
//    void testUpdateSpecialRequirementsBadRequest() {
//        var res = customerController.updateSpecialRequirements(null, null, null);
//        var statusCode = res.getStatusCode();
//
//        assertThat(res).isNotNull();
//        assertThat(statusCode).isEqualTo(HttpStatus.BAD_REQUEST);
//    }
//
//
//    @Test
//    void testRemoveDishFromOrderOk() {
//        Order order = createOrder();
//        Dish dish = createDish();
//
//        UpdateDishQtyRequest addDishReq = new UpdateDishQtyRequest();
//        addDishReq.setQuantity(1);
//        customerController.addDishToOrder(customerId, order.getID(), dish.getID(), addDishReq);
//
//        Order orderWithDish = orderRepo.findById(order.getID()).orElseThrow();
//        assertThat(orderWithDish.getDishes()).extracting(OrderedDish::getDish).contains(dish);
//
//        var res = customerController.removeDishFromOrder(customerId, order.getID(), dish.getID());
//        Order updatedOrder = res.getBody();
//
//        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(updatedOrder).isNotNull();
//        assertThat(updatedOrder.getDishes()).extracting(OrderedDish::getDish).doesNotContain(dish);
//
//    }
//
//    @Test
//    void testUpdateDishQtyBadRequest() {
//        var res = customerController.updateDishQty(null, null, null, null);
//        var statusCode = res.getStatusCode();
//
//        assertThat(res).isNotNull();
//        assertThat(statusCode).isEqualTo(HttpStatus.BAD_REQUEST);
//    }
//
//    @Test
//    void testUpdateDishQtyOk() {
//        Order order = createOrder();
//        Dish dish = createDish();
//
//        OrderedDish orderedDish = new OrderedDish();
//        orderedDish.setDish(dish);
//        orderedDish.setQuantity(1);
//        order.addDishesItem(orderedDish);
//        orderRepo.save(order);
//
//        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
//        int newQuantity = 2;
//        updateDishQtyRequest.setQuantity(newQuantity);
//        UUID orderId = order.getID();
//        UUID dishId = dish.getID();
//        ResponseEntity<Order> response = customerController
//        .updateDishQty(customerId, orderId, dishId, updateDishQtyRequest);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        Order updatedOrder = response.getBody();
//        assertThat(updatedOrder).isNotNull();
//        Optional<OrderedDish> updatedOrderedDishOpt = updatedOrder.getDishes().stream()
//                .filter(d -> d.getDish().getID().equals(dishId))
//                .findFirst();
//
//        assertThat(updatedOrderedDishOpt.isPresent()).isTrue();
//        OrderedDish updatedOrderedDish = updatedOrderedDishOpt.get();
//        assertThat(updatedOrderedDish.getQuantity()).isEqualTo(newQuantity);
//    }
//
//    @Test
//    void testGetPersonalOrderHistoryNoOrdersFound() {
//        UUID customerIdWithNoOrders = UUID.randomUUID();
//
//        ResponseEntity<List<Order>> response = customerController.getPersonalOrderHistory(customerIdWithNoOrders);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//
//        assertThat(response.getBody()).isNull();
//    }
//
//
//
//}
