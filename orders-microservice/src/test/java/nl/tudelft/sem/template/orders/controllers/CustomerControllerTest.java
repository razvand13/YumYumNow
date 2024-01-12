package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.model.*;
import nl.tudelft.sem.template.orders.domain.ICustomerService;
import nl.tudelft.sem.template.orders.domain.IDishService;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.domain.IVendorService;
import nl.tudelft.sem.template.orders.external.CustomerDTO;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import nl.tudelft.sem.template.orders.mappers.VendorMapper;
import nl.tudelft.sem.template.orders.services.CustomerAdapter;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CustomerControllerTest {

    @Mock
    private VendorMapper vendorMapper;
    @Mock
    private IVendorService vendorService;
    @Mock
    private IDishService dishService;
    @Mock
    private IOrderService orderService;
    @Mock
    private ICustomerService customerService;
    @Mock
    private CustomerAdapter customerAdapter;
    @Mock
    private VendorAdapter vendorAdapter;

    @InjectMocks
    private CustomerController customerController;

    private UUID customerId;
    private UUID orderId;
    private UUID dishId;

    @BeforeEach
    void setup() {
        vendorMapper = mock(VendorMapper.class);
        vendorService = mock(IVendorService.class);
        dishService = mock(IDishService.class);
        orderService = mock(IOrderService.class);
        customerService = mock(ICustomerService.class);
        customerAdapter = mock(CustomerAdapter.class);
        vendorAdapter = mock(VendorAdapter.class);

        customerController = new CustomerController(vendorMapper, vendorService, dishService, orderService,
                customerService, customerAdapter, vendorAdapter);

        customerId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        dishId = UUID.randomUUID();
    }

    @Test
    void getVendorsSuccess() {
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);

        CustomerDTO customer = new CustomerDTO();
        customer.setCurrentLocation(new Address());
        when(customerService.getDeliveryLocation(customer)).thenReturn(new Address());
        when(customerAdapter.requestCustomer(customerId)).thenReturn(customer);

        List<VendorDTO> vendors = Collections.singletonList(new VendorDTO());
        when(vendorAdapter.requestVendors()).thenReturn(vendors);
        when(vendorService.filterVendors(vendors, null, null, null, customer.getCurrentLocation()))
                .thenReturn(vendors);

        when(vendorMapper.toEntity(new VendorDTO())).thenReturn(new Vendor());

        ResponseEntity<List<Vendor>> response = customerController.getVendors(customerId, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getVendorsBadRequest() {
        ResponseEntity<List<Vendor>> response = customerController.getVendors(null, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getVendorsNotFound() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(false);

        ResponseEntity<List<Vendor>> response = customerController.getVendors(customerId, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getVendorsUnauthorized() {
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(customerAdapter.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<List<Vendor>> response = customerController.getVendors(customerId, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getVendorDishesBadRequestNullParams() {
        ResponseEntity<List<Dish>> response = customerController.getVendorDishes(null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getVendorDishesUnauthorizedCustomer() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<List<Dish>> response = customerController.getVendorDishes(customerId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getVendorDishesCustomerNotFound() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(false);

        ResponseEntity<List<Dish>> response = customerController.getVendorDishes(customerId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getVendorDishesOrderNotFound() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<List<Dish>> response = customerController.getVendorDishes(customerId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getVendorDishesOrderUnauthorized() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(false);
        when(customerAdapter.existsById(customerId)).thenReturn(true);

        Order order = new Order();
        order.setCustomerId(UUID.randomUUID()); // Different customer ID
        when(orderService.findById(orderId)).thenReturn(order);

        ResponseEntity<List<Dish>> response = customerController.getVendorDishes(customerId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getVendorDishesSuccess() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);

        Order order = new Order();
        order.setCustomerId(customerId);
        UUID vendorId = UUID.randomUUID();
        order.setVendorId(vendorId);
        when(orderService.findById(orderId)).thenReturn(order);

        List<Dish> dishes = Collections.singletonList(new Dish());
        when(dishService.findAllByVendorId(vendorId)).thenReturn(dishes);

        ResponseEntity<List<Dish>> response = customerController.getVendorDishes(customerId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dishes);
    }

    @Test
    void addDishToOrderOrderNotFound() {
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> response = customerController.addDishToOrder(customerId, orderId, dishId, new UpdateDishQtyRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addDishToOrderUnauthorized() {
        Order order = new Order();
        when(orderService.findById(orderId)).thenReturn(order);
        when(customerAdapter.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<Order> response = customerController.addDishToOrder(customerId, orderId, dishId, new UpdateDishQtyRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void addDishToOrderDishNotFound() {
        Order order = new Order();
        order.setCustomerId(customerId);
        when(orderService.findById(orderId)).thenReturn(order);
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(dishService.findById(dishId)).thenReturn(null);

        ResponseEntity<Order> response = customerController.addDishToOrder(customerId, orderId, dishId, new UpdateDishQtyRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addDishToOrderUpdateExistingDish() {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setDishes(new ArrayList<>());

        Dish dish = new Dish();
        dish.setID(dishId);

        OrderedDish existingOrderedDish = new OrderedDish();
        existingOrderedDish.setDish(dish);
        existingOrderedDish.setQuantity(1);
        order.addDishesItem(existingOrderedDish);


        when(orderService.findById(orderId)).thenReturn(order);
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(dishService.findById(dishId)).thenReturn(dish);
        when(orderService.orderedDishInOrder(order, dishId)).thenReturn(Optional.of(existingOrderedDish));

        when(orderService.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            return savedOrder;
        });

        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(2);

        ResponseEntity<Order> response = customerController.addDishToOrder(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDishes().size()).isEqualTo(1);
        assertThat(response.getBody().getDishes().get(0).getDish()).isEqualTo(dish);
        assertThat(response.getBody().getDishes().get(0).getQuantity()).isEqualTo(3);
    }


    @Test
    void addDishToOrderAddNewDish() {
        Order order = new Order();
        order.setCustomerId(customerId);
        Dish dish = new Dish();
        List<OrderedDish> orderedDishes = new ArrayList<>();

        OrderedDish newOrderedDish = new OrderedDish();
        newOrderedDish.setDish(dish);
        newOrderedDish.setQuantity(1);
        orderedDishes.add(newOrderedDish);

        when(orderService.findById(orderId)).thenReturn(order);
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(dishService.findById(dishId)).thenReturn(dish);
        when(orderService.orderedDishInOrder(order, dishId)).thenReturn(Optional.empty());
        when(orderService.save(order)).thenReturn(order);

        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(2);

        order.setDishes(orderedDishes);

        ResponseEntity<Order> response = customerController.addDishToOrder(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDishes().size()).isEqualTo(2);
        assertThat(response.getBody().getDishes().get(0).getDish()).isEqualTo(dish);
        assertThat(response.getBody().getDishes().get(0).getQuantity()).isEqualTo(1);
    }

    @Test
    void removeDishFromOrderOrderNotFound() {
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> response = customerController.removeDishFromOrder(customerId, orderId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void removeDishFromOrderUnauthorizedCustomer() {
        Order order = new Order();
        when(orderService.findById(orderId)).thenReturn(order);
        when(customerAdapter.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<Order> response = customerController.removeDishFromOrder(customerId, orderId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void removeDishFromOrderCustomerNotFound() {
        when(customerAdapter.existsById(customerId)).thenReturn(false);

        ResponseEntity<Order> response = customerController.removeDishFromOrder(customerId, orderId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    void removeDishFromOrderDishNotFound() {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setDishes(new ArrayList<>());
        when(orderService.findById(orderId)).thenReturn(order);
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(dishService.findById(dishId)).thenReturn(null);

        ResponseEntity<Order> response = customerController.removeDishFromOrder(customerId, orderId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void removeDishFromOrderDishNotInOrder() {
        Order order = new Order();
        order.setCustomerId(customerId);
        List<OrderedDish> dishes = new ArrayList<>();
        order.setDishes(dishes);
        when(orderService.findById(orderId)).thenReturn(order);
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(dishService.findById(dishId)).thenReturn(new Dish());

        ResponseEntity<Order> response = customerController.removeDishFromOrder(customerId, orderId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void removeDishFromOrderSuccess() {
        Order order = new Order();
        order.setCustomerId(customerId);
        Dish dish = new Dish();
        dish.setID(dishId);
        OrderedDish orderedDish = new OrderedDish();
        orderedDish.setDish(dish);
        List<OrderedDish> dishes = new ArrayList<>();
        dishes.add(orderedDish);
        order.setDishes(dishes);

        when(orderService.findById(orderId)).thenReturn(order);
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(dishService.findById(dishId)).thenReturn(dish);
        when(orderService.calculateOrderPrice(any())).thenReturn(0.0);
        when(orderService.save(any(Order.class))).thenReturn(order);

        ResponseEntity<Order> response = customerController.removeDishFromOrder(customerId, orderId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDishes()).doesNotContain(orderedDish);
    }

    @Test
    void updateDishQtyNegativeQuantity() {
        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(-1);

        ResponseEntity<Order> response = customerController.updateDishQty(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateDishQtyOrderNotFound() {
        when(orderService.findById(orderId)).thenReturn(null);

        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(1); // Set a valid quantity

        ResponseEntity<Order> response = customerController.updateDishQty(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    void updateDishQtyUnauthorizedCustomer() {
        when(orderService.findById(orderId)).thenReturn(new Order());
        when(customerAdapter.checkRoleById(customerId)).thenReturn(false);

        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(1);

        ResponseEntity<Order> response = customerController.updateDishQty(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    @Test
    void updateDishQtySuccess() {
        Order order = new Order();
        order.setCustomerId(customerId);
        Dish dish = new Dish();
        dish.setID(dishId);
        OrderedDish orderedDish = new OrderedDish();
        orderedDish.setDish(dish);
        orderedDish.setQuantity(1);
        List<OrderedDish> dishes = new ArrayList<>();
        dishes.add(orderedDish);
        order.setDishes(dishes);

        when(orderService.findById(orderId)).thenReturn(order);
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(orderService.calculateOrderPrice(any())).thenReturn(0.0);
        when(orderService.save(any(Order.class))).thenReturn(order);

        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(2);

        ResponseEntity<Order> response = customerController.updateDishQty(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDishes().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void getPersonalOrderHistoryWithNullCustomerId() {
        ResponseEntity<List<Order>> response = customerController.getPersonalOrderHistory(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getPersonalOrderHistoryWithUnauthorizedCustomer() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<List<Order>> response = customerController.getPersonalOrderHistory(customerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getPersonalOrderHistoryWithNonExistingCustomer() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(false);

        ResponseEntity<List<Order>> response = customerController.getPersonalOrderHistory(customerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPersonalOrderHistoryWithNoOrders() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(orderService.findOrdersByCustomerId(customerId)).thenReturn(new ArrayList<>());

        ResponseEntity<List<Order>> response = customerController.getPersonalOrderHistory(customerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPersonalOrderHistoryWithOrders() {
        List<Order> orders = List.of(new Order());
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(orderService.findOrdersByCustomerId(customerId)).thenReturn(orders);

        ResponseEntity<List<Order>> response = customerController.getPersonalOrderHistory(customerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(orders);
    }

    @Test
    void reorderWithNullInputs() {
        ResponseEntity<Order> response = customerController.reorder(null, null, new Address());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void reorderWithUnauthorizedCustomer() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<Order> response = customerController.reorder(customerId, orderId, new Address());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void reorderWithNonExistingCustomer() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(false);

        ResponseEntity<Order> response = customerController.reorder(customerId, orderId, new Address());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void reorderWithNonExistingPreviousOrder() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> response = customerController.reorder(customerId, orderId, new Address());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void reorderWithOrderNotBelongingToCustomer() {
        Order previousOrder = createOrder();
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(previousOrder);
        previousOrder.setCustomerId(UUID.randomUUID());

        ResponseEntity<Order> response = customerController.reorder(customerId, orderId, new Address());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void reorderSuccess() {
        Order previousOrder = createOrder();
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(previousOrder);
        when(orderService.save(any(Order.class))).thenReturn(new Order());

        ResponseEntity<Order> response = customerController.reorder(customerId, orderId, new Address());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private Order createOrder() {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setVendorId(UUID.randomUUID());
        order.setDishes(new ArrayList<>());
        order.setOrderTime(OffsetDateTime.now());
        order.setStatus(Status.PENDING);
        return order;
    }


}