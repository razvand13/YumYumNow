package nl.tudelft.sem.template.orders.controllers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import nl.tudelft.sem.template.api.CustomerApi;
import nl.tudelft.sem.template.model.Address;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Status;
import nl.tudelft.sem.template.model.UpdateDishQtyRequest;
import nl.tudelft.sem.template.model.Vendor;
import nl.tudelft.sem.template.model.OrderedDish;
import nl.tudelft.sem.template.orders.domain.ICustomerService;
import nl.tudelft.sem.template.orders.domain.IDishService;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.domain.IVendorService;
import nl.tudelft.sem.template.orders.mappers.VendorMapper;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
import nl.tudelft.sem.template.orders.services.CustomerAdapter;
import nl.tudelft.sem.template.model.CreateOrderRequest;
import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.orders.external.CustomerDTO;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import nl.tudelft.sem.template.orders.validator.DataValidationField;
import nl.tudelft.sem.template.orders.validator.DataValidator;
import nl.tudelft.sem.template.orders.validator.UserAuthorizationValidator;
import nl.tudelft.sem.template.orders.validator.UserType;
import nl.tudelft.sem.template.orders.validator.ValidationFailureException;
import nl.tudelft.sem.template.orders.validator.ValidatorRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CustomerController implements CustomerApi {
    private final transient VendorMapper vendorMapper;
    private final transient IVendorService vendorService;
    private final transient IDishService dishService;
    private final transient IOrderService orderService;
    private final transient ICustomerService customerService;
    private final transient CustomerAdapter customerAdapter;
    private final transient VendorAdapter vendorAdapter;
    private final transient ApplicationContext applicationContext;

    /**
     * Constructor for this controller
     *
     * @param vendorMapper    vendor mapper
     * @param vendorService   vendor service
     * @param dishService     dish service
     * @param orderService    order service
     * @param customerAdapter customer adapter
     * @param vendorAdapter   vendor adapter
     */
    @Autowired
    public CustomerController(VendorMapper vendorMapper, IVendorService vendorService,
                              IDishService dishService, IOrderService orderService, ICustomerService customerService,
                              CustomerAdapter customerAdapter, VendorAdapter vendorAdapter,
                              ApplicationContext applicationContext) {
        this.vendorMapper = vendorMapper;
        this.vendorService = vendorService;
        this.dishService = dishService;
        this.orderService = orderService;
        this.customerService = customerService;
        this.customerAdapter = customerAdapter;
        this.vendorAdapter = vendorAdapter;
        this.applicationContext = applicationContext;
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
        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class, List.of(DataValidationField.USER));
        UserAuthorizationValidator userAuthorizationValidator = applicationContext.getBean(UserAuthorizationValidator.class);
        //Set validation chain
        dataValidator.setNext(userAuthorizationValidator);
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(customerId);
        request.setUserType(UserType.CUSTOMER);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
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
        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class,
                List.of(DataValidationField.USER, DataValidationField.CREATEORDERREQUEST));
        UserAuthorizationValidator userAuthorizationValidator = applicationContext.getBean(UserAuthorizationValidator.class);
        //Set validation chain
        dataValidator.setNext(userAuthorizationValidator);
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(customerId);
        request.setUserType(UserType.CUSTOMER);
        request.setCreateOrderRequest(createOrderRequest);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
        }


        UUID vendorId = createOrderRequest.getVendorId();
        Address address = createOrderRequest.getAddress();


        Order order = new Order();
        order.setLocation(address);
        order.setStatus(Status.PENDING);
        order.setOrderTime(OffsetDateTime.now());
        order.setVendorId(vendorId);
        order.setCustomerId(customerId);
        Order savedOrder = orderService.save(order);

        return ResponseEntity.ok(savedOrder);
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
        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class,
                List.of(DataValidationField.USER, DataValidationField.ORDER));
        UserAuthorizationValidator userAuthorizationValidator = applicationContext.getBean(UserAuthorizationValidator.class);
        //Set validation chain
        dataValidator.setNext(userAuthorizationValidator);
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(customerId);
        request.setUserType(UserType.CUSTOMER);
        request.setOrderUUID(orderId);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
        }

        Order order = orderService.findById(orderId);

        // Get the vendor's dishes from the repository
        List<Dish> vendorDishes = dishService.findAllByVendorId(order.getVendorId());

        return ResponseEntity.ok(vendorDishes);
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
     *     or Bad Request - Dish not added to order. (status code 400)
     *     or Unauthorized - Order does not belong to user/dish does not belong to
     *     current vendor/user is not a customer. (status code 401)
     *     or Not Found - Dish, order or customer not found. (status code 404)
     *     or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<Order> addDishToOrder(UUID customerId, UUID orderId,
                                                UUID dishId, UpdateDishQtyRequest updateDishQtyRequest) {
        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class,
                List.of(DataValidationField.USER, DataValidationField.DISH,
                        DataValidationField.ORDER, DataValidationField.UPDATEDISHQTYREQUEST));
        UserAuthorizationValidator userAuthorizationValidator = applicationContext.getBean(UserAuthorizationValidator.class);
        //Set validation chain
        dataValidator.setNext(userAuthorizationValidator);
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest(customerId, UserType.CUSTOMER, orderId, dishId,
                updateDishQtyRequest, null);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
        }

        // Fetch order
        Order order = orderService.findById(orderId);

        Dish dishToAdd = dishService.findById(dishId);

        Optional<OrderedDish> existingOrderedDish = orderService.orderedDishInOrder(order, dishId);

        if (existingOrderedDish.isPresent()) {
            // Dish is already in the order, update the quantity
            OrderedDish orderedDish = existingOrderedDish.get();
            orderedDish.setQuantity(orderedDish.getQuantity() + updateDishQtyRequest.getQuantity());
        } else {
            // Dish is not in the order, add it as a new OrderedDish
            OrderedDish newOrderedDish = new OrderedDish();
            newOrderedDish.setDish(dishToAdd);
            newOrderedDish.setQuantity(updateDishQtyRequest.getQuantity());
            order.addDishesItem(newOrderedDish);
        }


        // Recalculate total price
        double newTotalPrice = orderService.calculateOrderPrice(order.getDishes());

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
     *     or Bad Request - Dish not removed from order. (status code 400)
     *     or Unauthorized - Order does not belong to user/dish does not belong to
     *     current vendor/user is not a customer. (status code 401)
     *     or Not Found - Dish, order, or customer not found. (status code 404)
     *     or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<Order> removeDishFromOrder(UUID customerId, UUID orderId, UUID dishId) {
        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class,
                List.of(DataValidationField.USER, DataValidationField.ORDER, DataValidationField.DISH));
        UserAuthorizationValidator userAuthorizationValidator = applicationContext.getBean(UserAuthorizationValidator.class);
        //Set validation chain
        dataValidator.setNext(userAuthorizationValidator);
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(customerId);
        request.setUserType(UserType.CUSTOMER);
        request.setOrderUUID(orderId);
        request.setDishUUID(dishId);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
        }

        Order order = orderService.findById(orderId);

        // Check if the dish is part of the order
        List<OrderedDish> orderedDishes = order.getDishes();
        Optional<OrderedDish> dishToRemove = orderedDishes.stream()
                .filter(orderedDish -> orderedDish.getDish().getID().equals(dishId))
                .findFirst();

        if (!dishToRemove.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Dish not found in order
        }

        // Remove the dish from the order
        orderedDishes.remove(dishToRemove.get());

        order.setDishes(orderedDishes);

        // Calculate new price of order
        double newTotalPrice = orderService.calculateOrderPrice(orderedDishes);

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
     *     or Bad Request - Invalid quantity provided. (status code 400)
     *     or Unauthorized - Order does not belong to user/dish does not belong to
     *     current vendor/user is not a customer. (status code 401)
     *     or Not Found - Dish, order, or customer not found. (status code 404)
     *     or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<Order> updateDishQty(UUID customerId, UUID orderId,
                                               UUID dishId, UpdateDishQtyRequest updateDishQtyRequest) {
        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class,
                List.of(DataValidationField.USER, DataValidationField.ORDER,
                        DataValidationField.DISH, DataValidationField.UPDATEDISHQTYREQUEST));
        UserAuthorizationValidator userAuthorizationValidator = applicationContext.getBean(UserAuthorizationValidator.class);
        //Set validation chain
        dataValidator.setNext(userAuthorizationValidator);
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest(customerId, UserType.CUSTOMER,
                orderId, dishId, updateDishQtyRequest, null);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
        }

        Order order = orderService.findById(orderId);

        // Get the current list of ordered dishes in the order
        List<OrderedDish> orderedDishes = order.getDishes();

        // Find the OrderedDish that matches the dishId, and update its quantity
        orderedDishes.stream()
                .filter(orderedDish -> orderedDish.getDish().getID().equals(dishId))
                .findFirst()
                .ifPresent(orderedDish -> orderedDish.setQuantity(updateDishQtyRequest.getQuantity()));

        // Set the updated list of ordered dishes in the order
        order.setDishes(orderedDishes);

        // Recalculate the total price of the order
        double newTotalPrice = orderService.calculateOrderPrice(orderedDishes);

        // Update the total price of the order
        order.setTotalPrice(newTotalPrice);

        // Save the order with the updated list of ordered dishes
        Order updatedOrder = orderService.save(order);

        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * GET /customer/{customerId}/order/{orderId} : Get all details of the order for a customer (updated price as well)
     * Get all the details of a specific order for a customer based on the order id
     *
     * @param customerId (required)
     * @param orderId    (required)
     * @return Details of the specified order (status code 200)
     *     or Bad Request - Invalid request parameters. (status code 400)
     *     or Unauthorized - User is not a customer/order does not belong to user. (status code 401)
     *     or Order or customer not found. (status code 404)
     *     or Internal Server Error - An unexpected error occurred. (status code 500)
     */
    @Override
    public ResponseEntity<Order> getOrder(UUID customerId, UUID orderId) {
        if (customerId == null || orderId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (!customerAdapter.checkRoleById(customerId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!customerAdapter.existsById(customerId) || orderService.findById(orderId) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Order order = orderService.findById(orderId);

        if (!order.getCustomerId().equals(customerId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(order);
    }


    /**
     * GET /customer/{customerId}/history : Get list of previous orders
     * Returns a list of previous orders for the specified user.
     *
     * @param customerId  (required)
     * @return List of previous orders. (status code 200)
     *         or Bad Request - Invalid request parameters. (status code 400)
     *         or Unauthorized - Not a customer user. (status code 401)
     *         or Not Found - User does not exist. (status code 404)
     *         or Internal Server Error - An unexpected error occured on the server. (status code 500)
     */
    @Override
    public ResponseEntity<List<Order>> getPersonalOrderHistory(UUID customerId) {
        // Validate customerId
        if (customerId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Authorize customer
        if (!customerAdapter.checkRoleById(customerId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Check if the customer exists
        if (!customerAdapter.existsById(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Fetch the list of previous orders for the customer
        List<Order> orders = orderService.findOrdersByCustomerId(customerId);
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Return the list of orders
        return ResponseEntity.ok(orders);
    }

    /**
     * POST /customer/{customerId}/reorder/{orderId} : Reorder based on a previous order
     * Creates a new order with identical contents as a previous order.
     *
     * @param customerId  (required)
     * @param orderId  (required)
     * @param address  (optional)
     * @return Successfully created duplicate order. (status code 200)
     *         or Bad Request - Invalid request parameters. (status code 400)
     *         or Unauthorized - order does not belong to customer/user is not a customer. (status code 401)
     *         or Forbidden - Reordering this order is not allowed (discontinued items). (status code 403)
     *         or Not Found - Order/customer does not exist. (status code 404)
     *         or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<Order> reorder(UUID customerId, UUID orderId, Address address) {
        // Validate input
        if (customerId == null || orderId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Authorize customer
        if (!customerAdapter.checkRoleById(customerId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Check if the customer exists
        if (!customerAdapter.existsById(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Fetch the previous order
        Order previousOrder = orderService.findById(orderId);
        if (previousOrder == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Check if the order belongs to the customer
        if (!previousOrder.getCustomerId().equals(customerId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Create a new order with identical contents
        Order newOrder = new Order();
        newOrder.setCustomerId(customerId);
        newOrder.setVendorId(previousOrder.getVendorId());
        newOrder.setDishes(new ArrayList<>(previousOrder.getDishes()));
        newOrder.setOrderTime(OffsetDateTime.now());
        newOrder.setStatus(Status.PENDING);
        newOrder.setLocation(address != null ? address : previousOrder.getLocation());

        // Save the new order
        Order savedOrder = orderService.save(newOrder);

        return ResponseEntity.ok(savedOrder);
    }




}

