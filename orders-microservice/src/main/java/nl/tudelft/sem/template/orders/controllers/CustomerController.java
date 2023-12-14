package nl.tudelft.sem.template.orders.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.api.CustomerApi;
import nl.tudelft.sem.template.model.CreateOrderRequest;
import nl.tudelft.sem.template.orders.entities.Address;
import nl.tudelft.sem.template.orders.entities.Vendor;
import nl.tudelft.sem.template.orders.entities.Order;
import nl.tudelft.sem.template.orders.entities.Dish;
import nl.tudelft.sem.template.orders.entities.Status;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import nl.tudelft.sem.template.orders.repositories.VendorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class CustomerController implements CustomerApi {

    private final static String USERS_URL = "https://localhost:8088";
    private final static String DELIVERY_URL = "https://localhost:8081";
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    VendorRepository vendorRepository;
    OrderRepository orderRepository;

    public CustomerController(VendorRepository vendorRepository, OrderRepository orderRepository) {
        this.vendorRepository = vendorRepository;
        this.orderRepository = orderRepository;
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
     * or Bad Request - Invalid request parameters. (status code 400)
     * or Unauthorized - Not a customer user. (status code 401)
     * or Not Found - User does not exist. (status code 404)
     * or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<List<Vendor>> getVendors(UUID customerId, String name,
                                                   Integer minAvgPrice, Integer maxAvgPrice) {
        if (customerId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Check that user exists
        // TODO external check method is to be done by someone else, edit when added
        boolean exists = true;
        if (!exists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Authorize customer
        // TODO external check method is to be done by someone else, edit when added
          boolean isCustomer = true;
        if (!isCustomer) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final HttpClient client = HttpClient.newBuilder().build();

        // Request vendors from the Users microservice
        final HttpRequest requestVendors = HttpRequest.newBuilder()
                .uri(URI.create(USERS_URL + "/vendors"))
                .GET()
                .build();

        final HttpResponse<String> responseVendors;
        try {
            responseVendors = client.send(requestVendors, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Vendor> vendors;
        try {
            vendors = OBJECT_MAPPER.readValue(responseVendors.body(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<Vendor> filteredVendors = new ArrayList<>();

        for (Vendor vendor : vendors) {
            // TODO filter input by distance
            // TODO filter input by name and average price
            // name : use vendor.name.contains(name)
            // avg price : SQL query in repo, get average of dish price per vendorId (todo CALLED BY SERVICE)
            filteredVendors.add(vendor);
        }

        return ResponseEntity.ok(filteredVendors);
    }

    /**
     * POST /customer/{customerId}/order : Create a new order
     * Creates a new order for the customer with a specified vendor. This is done by selecting a vendor from
     * the list of all vendors basically, so we are sure that the customer is in the range of the vendor.
     *
     * @param customerId         (required)
     * @param createOrderRequest (required)
     * @return Newly created order object with orderId, customerId, vendorId and address populated. (status code 200)
     * or Bad Request - No location present or other input errors (invalid format). (status code 400)
     * or Unauthorized - Not a customer user. (status code 401)
     * or Not Found - User does not exist. (status code 404)
     * or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<Order> createOrder(UUID customerId, CreateOrderRequest createOrderRequest) {
        Integer vendorId = createOrderRequest.getVendorId();
        Address address = createOrderRequest.getAddress();

        if(customerId == null || vendorId == null || address == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Check that user exists
        // TODO external check method is to be done by someone else, edit when added
        boolean exists = true;
        if (!exists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Authorize customer
        // TODO external check method is to be done by someone else, edit when added
        boolean isCustomer = true;
        if (!isCustomer) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Order order = new Order();
        order.setLocation(address);
        order.setStatus(Status.PENDING);
        order.setOrderTime(OffsetDateTime.now());
//        order.setVendorId(vendorId); // TODO this should be an UUID
        order.setCustomerId(customerId);

        return ResponseEntity.ok(order);
    }

    /**
     * GET /customer/{customerId}/order/{orderId}/vendor : Get all dishes of the selected vendor for the order.
     * Get a list of all of the dishes of the vendor associated with the order (the selected vendor) as a customer.
     * Only show dishes that don&#39;t include the customer&#39;s allergies.
     *
     * @param customerId (required)
     * @param orderId    (required)
     * @return A list of dishes offered by the vendor. (status code 200)
     * or Bad Request - Invalid request parameters. (status code 400)
     * or Unauthorized - User is not a customer/order does not belong to user. (status code 401)
     * or User or order not found (status code 404)
     * or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<List<Dish>> getVendorDishes(UUID customerId, UUID orderId) {
        if(customerId == null || orderId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Check that user exists
        // TODO external check method is to be done by someone else, edit when added
        boolean exists = true;
        if (!exists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Authorize customer
        // TODO external check method is to be done by someone else, edit when added
        boolean isCustomer = true;
        if (!isCustomer) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final HttpClient client = HttpClient.newBuilder().build();

        // Request customer from the Users microservice
        final HttpRequest requestVendors = HttpRequest.newBuilder()
                .uri(URI.create(USERS_URL + "/customers/" + customerId))
                .GET()
                .build();

        final HttpResponse<String> responseCustomer;
        try {
            responseCustomer = client.send(requestVendors, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // TODO order service: remove this by just returning the order
        Optional<Order> orderOptional = orderRepository.findById(orderId);

        // Check that order exists
        if(orderOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Order order = orderOptional.get();

        // Check that order has customerId and vendorId inside
        if(order.getCustomerId() == null || order.getVendorId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Check that order belongs to this customer
        if(order.getCustomerId() != customerId) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID vendorId = order.getVendorId();
        List<Dish> vendorDishes = new ArrayList<>();

        // TODO filter dishes by allergens
        // TODO customer (or order) service: get allergens of customer
        List<String> allergens = new ArrayList<>();

        // TODO filter dishes by allergens (SQL?)
        List<Dish> filteredDishes = new ArrayList<>();

        return ResponseEntity.ok(filteredDishes);
    }
}
