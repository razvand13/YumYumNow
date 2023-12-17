package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.api.CustomerApi;
import nl.tudelft.sem.template.model.UpdateDishQtyRequest;
import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.entities.Order;
import nl.tudelft.sem.template.orders.mappers.DishMapper;
import nl.tudelft.sem.template.orders.mappers.VendorMapper;
import nl.tudelft.sem.template.orders.services.OrderService;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
import nl.tudelft.sem.template.orders.services.VendorService;
import nl.tudelft.sem.template.orders.services.DishService;
import nl.tudelft.sem.template.orders.services.CustomerService;
import nl.tudelft.sem.template.orders.services.CustomerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
public class CustomerController implements CustomerApi {
    private final transient VendorMapper vendorMapper;
    private final transient DishMapper dishMapper;
    private final transient VendorService vendorService;
    private final transient DishService dishService;
    private final transient OrderService orderService;
    private final transient CustomerService customerService;
    private final transient CustomerAdapter customerAdapter;
    private final transient VendorAdapter vendorAdapter;

    /**
     * Constructor for this controller
     *
     * @param vendorMapper    vendor mapper
     * @param dishMapper      dish mapper
     * @param vendorService   vendor service
     * @param dishService     dish service
     * @param orderService    order service
     * @param customerAdapter customer adapter
     * @param vendorAdapter   vendor adapter
     */
    @Autowired
    public CustomerController(VendorMapper vendorMapper, DishMapper dishMapper, VendorService vendorService,
                              DishService dishService, OrderService orderService, CustomerService customerService,
                              CustomerAdapter customerAdapter, VendorAdapter vendorAdapter) {
        this.vendorMapper = vendorMapper;
        this.dishMapper = dishMapper;
        this.vendorService = vendorService;
        this.dishService = dishService;
        this.orderService = orderService;
        this.customerService = customerService;
        this.customerAdapter = customerAdapter;
        this.vendorAdapter = vendorAdapter;
    }



    /**
     * POST /customer/{customerId}/order/{orderId}/dish/{dishId} : Add dish to order
     * Adds the specified dish to the order.
     *
     * @param customerId           (required)
     * @param orderId              (required)
     * @param dishId               (required)
     * @param updateDishQtyRequest (required)
     * @return Dish added successfully, updated order returned. (status code 200)
     * or Bad Request - Dish not added to order. (status code 400)
     * or Unauthorized - Order does not belong to user/dish does not belong to
     * current vendor/user is not a customer. (status code 401)
     * or Not Found - Dish, order or customer not found. (status code 404)
     * or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<Order> addDishToOrder(UUID customerId, UUID orderId,
                                                UUID dishId, UpdateDishQtyRequest updateDishQtyRequest) {
        // Fetch order
        Optional<Order> orderOptional = Optional.ofNullable(orderService.findById(orderId));
        if (!orderOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Order order = orderOptional.get();

        // Verify if the order belongs to the given customer
        if (!customerId.equals(order.getCustomerId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // Unauthorized access
        }

        // Fetch dish
        Optional<DishEntity> dishOptional = Optional.ofNullable(dishService.findById(dishId));
        if (!dishOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        DishEntity dish = dishOptional.get();

        // Add dish to the order
        for (int i = 0; i < updateDishQtyRequest.getQuantity(); i++) {
            order.addDishesItem(dish);
        }

        // Recalculate total price
        double newTotalPrice = order.getDishes().stream()
                .mapToDouble(DishEntity::getPrice)
                .sum();

        order.setTotalPrice(newTotalPrice);

        // Save the updated order
        Order updatedOrder = orderService.save(order);

        return ResponseEntity.ok(updatedOrder);
    }


    /**
     * DELETE /customer/{customerId}/order/{orderId}/dish/{dishId} : Remove dish from order
     * Removes the specified dish from the order.
     *
     * @param customerId  (required)
     * @param orderId     (required)
     * @param dishId      (required)
     * @return Dish removed successfully, updated order returned. (status code 200)
     * or Bad Request - Dish not removed from order. (status code 400)
     * or Unauthorized - Order does not belong to user/dish does not belong to
     * current vendor/user is not a customer. (status code 401)
     * or Not Found - Dish, order, or customer not found. (status code 404)
     * or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<Order> removeDishFromOrder(UUID customerId, UUID orderId, UUID dishId) {
        // Fetch order
        Optional<Order> orderOptional = Optional.ofNullable(orderService.findById(orderId));
        if (!orderOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Order order = orderOptional.get();

        // Verify if the order belongs to the given customer
        if (!customerId.equals(order.getCustomerId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Check if the dish is part of the order
        List<DishEntity> currentDishes = order.getDishes();
        boolean dishExists = currentDishes.stream().anyMatch(dish -> dish.getID().equals(dishId));

        if (!dishExists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Dish not found in order
        }

        // Remove the dish from the order
        List<DishEntity> updatedDishes = currentDishes.stream()
                .filter(dish -> !dish.getID().equals(dishId))
                .collect(Collectors.toList());

        order.setDishes(updatedDishes);

        // Calculate new price of order
        double newTotalPrice = updatedDishes.stream()
                .mapToDouble(DishEntity::getPrice)
                .sum();

        order.setTotalPrice(newTotalPrice);

        // Save the updated order
        Order updatedOrder = orderService.save(order);

        return ResponseEntity.ok(updatedOrder);
    }


    /**
     * PUT /customer/{customerId}/order/{orderId}/dish/{dishId} : Update dish quantity in order
     * Updates the quantity of the specified dish in the order. If the quantity is 0, the dish is removed.
     *
     * @param customerId           (required)
     * @param orderId              (required)
     * @param dishId               (required)
     * @param updateDishQtyRequest (required)
     * @return Dish quantity updated successfully, updated order returned. (status code 200)
     * or Bad Request - Invalid quantity provided. (status code 400)
     * or Unauthorized - Order does not belong to user/dish does not belong to
     * current vendor/user is not a customer. (status code 401)
     * or Not Found - Dish, order, or customer not found. (status code 404)
     * or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<Order> updateDishQty(UUID customerId, UUID orderId,
                                               UUID dishId, UpdateDishQtyRequest updateDishQtyRequest) {
        // Validate the requested quantity - it should not be negative
        if (updateDishQtyRequest.getQuantity() < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Retrieve the order by ID and check if it exists and belongs to the given customer
        Optional<Order> orderOptional = Optional.ofNullable(orderService.findById(orderId));
        if (!orderOptional.isPresent() || !orderOptional.get().getCustomerId().equals(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Order not found or doesn't belong to customer
        }

        Order order = orderOptional.get();

        // Get the current list of dishes in the order
        List<DishEntity> currentDishes = order.getDishes();
        List<DishEntity> updatedDishes = new ArrayList<>();

        // Add all dishes to the updated list, except for the one we want to change the quantity of
        currentDishes.stream()
                .filter(dish -> !dish.getID().equals(dishId))
                .forEach(updatedDishes::add);

        // Now add the specified dish in the desired quantity
        currentDishes.stream()
                .filter(dish -> dish.getID().equals(dishId))
                .limit(updateDishQtyRequest.getQuantity())
                .forEach(updatedDishes::add);

        // Set the updated list of dishes in the order
        order.setDishes(updatedDishes);

        // Recalculate the total price of the order
        double newTotalPrice = updatedDishes.stream()
                .mapToDouble(DishEntity::getPrice)
                .sum();

        // Update the total price of the order
        order.setTotalPrice(newTotalPrice);

        // Save the order with the updated list of dishes
        Order updatedOrder = orderService.save(order);

        // Return the updated order
        return ResponseEntity.ok(updatedOrder);
    }



}
