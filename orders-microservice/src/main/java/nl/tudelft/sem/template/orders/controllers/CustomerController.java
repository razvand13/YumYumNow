package nl.tudelft.sem.template.orders.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.api.CustomerApi;
import nl.tudelft.sem.template.model.UpdateDishQtyRequest;
import nl.tudelft.sem.template.orders.entities.Dish;
import nl.tudelft.sem.template.orders.entities.Order;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import nl.tudelft.sem.template.orders.repositories.VendorRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class CustomerController implements CustomerApi {

    private final static String USERS_URL = "https://localhost:8088";
    private final static String DELIVERY_URL = "https://localhost:8081";
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    VendorRepository vendorRepository;
    OrderRepository orderRepository;
    DishRepository dishRepository;

    public CustomerController(VendorRepository vendorRepository, OrderRepository orderRepository, DishRepository dishRepository) {
        this.vendorRepository = vendorRepository;
        this.orderRepository = orderRepository;
        this.dishRepository = dishRepository;
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
     * or Unauthorized - Order does not belong to user/dish does not belong to current vendor/user is not a customer. (status code 401)
     * or Not Found - Dish, order or customer not found. (status code 404)
     * or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    // TODO: Adapt with the change of entities
    @Override
    public ResponseEntity<Order> addDishToOrder(UUID customerId, UUID orderId, UUID dishId, UpdateDishQtyRequest updateDishQtyRequest){
        // Validate parameters
        if (updateDishQtyRequest.getQuantity() <= 0) {
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Fetch order
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (!orderOptional.isPresent() || !orderOptional.get().getCustomerId().equals(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Order order = orderOptional.get();
        final HttpClient client = HttpClient.newBuilder().build();

        String url = USERS_URL + "/orders/customer/" + customerId + "/order/" + orderId + "/vendor";
        HttpRequest requestDishes = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(requestDishes, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Process response to check if the dish belongs to the vendor
        if (!response.body().contains(dishId.toString())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Dish> dishList;
        try {
            dishList = OBJECT_MAPPER.readValue(response.body(), new TypeReference<List<Dish>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON", e);
        }

        // Check if the dish is in the dishList
        boolean dishExists = dishList.stream()
                .anyMatch(dish -> dish.getID().equals(dishId));
        if (!dishExists) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Find the dish in the list
        Dish dish = dishList.stream()
                .filter(d -> d.getID().equals(dishId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dish not found in the vendor's list"));

        // Add the dish to the order with the right quantity
        for(int i = 0; i < updateDishQtyRequest.getQuantity(); i++){
            order.addDishesItem(dish);
        }


        Order updatedOrder = orderRepository.save(order);

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
     * or Unauthorized - Order does not belong to user/dish does not belong to current vendor/user is not a customer. (status code 401)
     * or Not Found - Dish, order, or customer not found. (status code 404)
     * or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    // TODO: Adapt with the change of entities
    @Override
    public ResponseEntity<Order> removeDishFromOrder(UUID customerId, UUID orderId, UUID dishId) {
        // Fetch order
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (!orderOptional.isPresent() || !orderOptional.get().getCustomerId().equals(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Order order = orderOptional.get();

        // Check if the dish is part of the order
        if (!order.getDishes().stream().anyMatch(dish -> dish.getID().equals(dishId))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Remove the dish from the order
        order.setDishes(order.getDishes().stream()
                .filter(dish -> !dish.getID().equals(dishId))
                .collect(Collectors.toList()));

        // Save the updated order
        Order updatedOrder = orderRepository.save(order);

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
     * or Unauthorized - Order does not belong to user/dish does not belong to current vendor/user is not a customer. (status code 401)
     * or Not Found - Dish, order, or customer not found. (status code 404)
     * or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    // TODO: Adapt with the change of entities
    @Override
    public ResponseEntity<Order> updateDishQuantityInOrder(UUID customerId, UUID orderId, UUID dishId, UpdateDishQtyRequest updateDishQtyRequest) {
        // Validate parameters
        if (updateDishQtyRequest.getQuantity() < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Fetch order
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (!orderOptional.isPresent() || !orderOptional.get().getCustomerId().equals(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Order order = orderOptional.get();

        // Check if the dish is in the order
        long existingCount = order.getDishes().stream()
                .filter(dish -> dish.getID().equals(dishId))
                .count();

        if (existingCount == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (updateDishQtyRequest.getQuantity() == 0) {
            // Remove the dish if the quantity is 0
            order.setDishes(order.getDishes().stream()
                    .filter(dish -> !dish.getID().equals(dishId))
                    .collect(Collectors.toList()));
        } else {
            // Update the quantity of the dish
            List<Dish> updatedDishes = new ArrayList<>();
            for (Dish dish : order.getDishes()) {
                if (dish.getID().equals(dishId)) {
                    if (updatedDishes.size() < updateDishQtyRequest.getQuantity()) {
                        updatedDishes.add(dish);
                    }
                } else {
                    updatedDishes.add(dish);
                }
            }
            order.setDishes(updatedDishes);
        }

        // Save the updated order
        Order updatedOrder = orderRepository.save(order);

        return ResponseEntity.ok(updatedOrder);
    }



}
