package nl.tudelft.sem.template.orders.controllers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

import nl.tudelft.sem.template.model.CreateOrderRequest;
import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.orders.entities.Address;
import nl.tudelft.sem.template.orders.entities.Status;
import nl.tudelft.sem.template.orders.entities.Vendor;
import nl.tudelft.sem.template.orders.external.CustomerDTO;
import nl.tudelft.sem.template.orders.external.VendorDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


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
     * GET /customer/{customerId}/vendors : Get list of vendors
     * Get a list of vendors within a specified radius around the customer's current location or default address.
     * The radius is predefined - 5 kilometers). The current location is retrieved from the Users microservice.
     * If there is no current location, we check if the user has a home address and use that.
     * If there is no home address either, we send an error response.
     * Additionally, searching and filtering restaurants through query parameters is possible.
     *
     * @param customerId  (required)
     * @param name        (optional)
     * @param minAvgPrice (optional)
     * @param maxAvgPrice (optional)
     * @return List of vendors. (status code 200)
     *         or Bad Request - Invalid request parameters. (status code 400)
     *         or Unauthorized - Not a customer user. (status code 401)
     *         or Not Found - User does not exist. (status code 404)
     *         or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<List<Vendor>> getVendors(UUID customerId, String name,
                                                   Integer minAvgPrice, Integer maxAvgPrice) {
        if (customerId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Check that user exists
        if (!customerAdapter.existsById(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Authorize customer
        if (!customerAdapter.checkRoleById(customerId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get the customer's delivery location
        CustomerDTO customer = customerAdapter.requestCustomer(customerId);
        Address customerLocation = customerService.getDeliveryLocation(customer);

        if (customerLocation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Filter vendors by name, average price and distance to delivery location
        List<VendorDTO> vendors = vendorAdapter.requestVendors();
        List<VendorDTO> filteredVendors = vendorService.filterVendors(
                vendors, name, minAvgPrice, maxAvgPrice, customerLocation);
        var filteredVendorEntities = filteredVendors.stream().map(vendorMapper::toEntity).collect(Collectors.toList());

        return ResponseEntity.ok(filteredVendorEntities);
    }

    /**
     * POST /customer/{customerId}/order : Create a new order
     * Creates a new order for the customer with a specified vendor. This is done by selecting a vendor from
     * the list of all vendors basically, so we are sure that the customer is in the range of the vendor.
     * The newly created order is saved in the database.
     *
     * @param customerId         (required)
     * @param createOrderRequest (required)
     * @return Newly created order object with orderId, customerId, vendorId and address populated. (status code 200)
     *         or Bad Request - No location present or other input errors (invalid format). (status code 400)
     *         or Unauthorized - Not a customer user. (status code 401)
     *         or Not Found - User does not exist. (status code 404)
     *         or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<Order> createOrder(UUID customerId, CreateOrderRequest createOrderRequest) {
        Integer vendorId = createOrderRequest.getVendorId();
        Address address = createOrderRequest.getAddress();

        if (customerId == null || vendorId == null || address == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Check that user exists
        if (!customerAdapter.existsById(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Authorize customer
        if (!customerAdapter.checkRoleById(customerId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Order order = new Order();
        order.setLocation(address);
        order.setStatus(Status.PENDING);
        order.setOrderTime(OffsetDateTime.now());
        order.setVendorId(UUID.fromString(Integer.toString(vendorId))); // TODO change API for this to be UUID
        order.setCustomerId(customerId);
        orderService.save(order);

        return ResponseEntity.ok(order);
    }

    /**
     * GET /customer/{customerId}/order/{orderId}/vendor : Get all dishes of the selected vendor for the order.
     * Get a list of all the dishes of the vendor associated with the order (the selected vendor) as a customer.
     * Only show dishes that don&#39;t include the customer&#39;s allergies.
     *
     * @param customerId (required)
     * @param orderId    (required)
     * @return A list of dishes offered by the vendor. (status code 200)
     *         or Bad Request - Invalid request parameters. (status code 400)
     *         or Unauthorized - User is not a customer/order does not belong to user. (status code 401)
     *         or User or order not found (status code 404)
     *         or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<List<Dish>> getVendorDishes(UUID customerId, UUID orderId) {
        if (customerId == null || orderId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Check that user exists
        if (!customerAdapter.existsById(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Authorize customer
        if (!customerAdapter.checkRoleById(customerId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get order from repository, check if order exists
        Order order = orderService.findById(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Check that order has customerId and vendorId inside
        if (order.getCustomerId() == null || order.getVendorId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Check that order belongs to this customer
        if (!order.getCustomerId().equals(customerId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Get the vendor's dishes from the repository
        List<DishEntity> vendorDishes = dishService.findAllByVendorId(order.getVendorId());

        // When filtering by allergens, move this to DishService
        List<Dish> filteredDishes = vendorDishes.stream().map(dishMapper::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(filteredDishes);
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


        // Verify if the order belongs to the given customer
        if (!customerAdapter.checkRoleById(customerId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Unauthorized access
        }

        // Fetch dish
        Optional<DishEntity> dishOptional = Optional.ofNullable(dishService.findById(dishId));
        if (!dishOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Order order = orderOptional.get();

        // Add dish to the order
        for (int i = 0; i < updateDishQtyRequest.getQuantity(); i++) {
            DishEntity dishEntity = dishOptional.get();
            order.addDishesItem(dishEntity);
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


        if (!customerAdapter.existsById(customerId)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }


        // Verify if the order belongs to the given customer
        if (!customerAdapter.checkRoleById(customerId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (dishService.findById(dishId) == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Order order = orderOptional.get();

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Order not found or doesn't belong to customer
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

