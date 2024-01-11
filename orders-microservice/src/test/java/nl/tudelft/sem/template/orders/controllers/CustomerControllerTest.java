package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.model.OrderedDish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.orders.domain.ICustomerService;
import nl.tudelft.sem.template.orders.domain.IDishService;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.domain.IVendorService;
import nl.tudelft.sem.template.orders.mappers.VendorMapper;
import nl.tudelft.sem.template.orders.services.CustomerAdapter;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void getOrderSuccess() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);

        Order order = new Order();
        order.setCustomerId(customerId);
        when(orderService.findById(orderId)).thenReturn(order);

        ResponseEntity<Order> responseEntity = customerController.getOrder(customerId, orderId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(order);
    }

    @Test
    void getOrderBadRequest() {
        ResponseEntity<Order> responseEntity = customerController.getOrder(null, null);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getOrderUnauthorized() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<Order> responseEntity = customerController.getOrder(customerId, orderId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getOrderNotFoundCustomer() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(false);

        ResponseEntity<Order> responseEntity = customerController.getOrder(customerId, orderId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getOrderNotFoundOrder() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> responseEntity = customerController.getOrder(customerId, orderId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getOrderUnauthorizedCustomerMismatch() {
        UUID anotherCustomerId = UUID.randomUUID();
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);

        Order order = new Order();
        order.setCustomerId(anotherCustomerId);
        when(orderService.findById(orderId)).thenReturn(order);

        ResponseEntity<Order> responseEntity = customerController.getOrder(customerId, orderId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getDishFromOrderSuccess() {
        Dish dish = new Dish();
        dish.setID(dishId);
        Order order = new Order();
        order.setCustomerId(customerId);
        OrderedDish orderedDish = new OrderedDish();
        orderedDish.setDish(dish);
        order.setDishes(Collections.singletonList(orderedDish));

        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(dishService.findById(dishId)).thenReturn(dish);

        ResponseEntity<OrderedDish> responseEntity = customerController.getDishFromOrder(customerId, orderId, dishId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(orderedDish);
    }

    @Test
    void getDishFromOrderBadRequest() {
        ResponseEntity<OrderedDish> responseEntity =
                customerController.getDishFromOrder(customerId, UUID.randomUUID(), null);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getDishFromOrderUnauthorized() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<OrderedDish> responseEntity = customerController.getDishFromOrder(customerId, orderId, dishId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getDishFromOrderNotFoundDish() {
        Order order = new Order();
        order.setCustomerId(customerId);

        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(dishService.findById(dishId)).thenReturn(null);

        ResponseEntity<OrderedDish> responseEntity = customerController.getDishFromOrder(customerId, orderId, dishId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getDishFromOrderNotFoundOrder() {
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<OrderedDish> responseEntity = customerController.getDishFromOrder(customerId, orderId, dishId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getDishFromOrderDishDoesNotBelongToOrder() {
        Dish dish = new Dish();
        dish.setID(dishId);
        dish.setName("Chicken Tikka Masala");
        Order order = new Order();
        order.setCustomerId(customerId);
        OrderedDish orderedDish = new OrderedDish();
        orderedDish.setDish(dish);
        order.setDishes(Collections.singletonList(orderedDish));
        Dish anotherDish = new Dish();
        UUID anotherDishId = UUID.randomUUID();
        dish.setID(anotherDishId);

        when(dishService.findById(anotherDishId)).thenReturn(anotherDish);
        when(orderService.findById(orderId)).thenReturn(order);
        when(dishService.findById(dishId)).thenReturn(dish);
        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);

        ResponseEntity<OrderedDish> responseEntity =
                customerController.getDishFromOrder(customerId, orderId, anotherDishId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getDishesFromOrderEmptyDishList() {
        Dish dish = new Dish();
        dish.setID(dishId);
        dish.setName("Chicken Tikka Masala");
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setDishes(Collections.emptyList());

        when(customerAdapter.checkRoleById(customerId)).thenReturn(true);
        when(customerAdapter.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(dishService.findById(dishId)).thenReturn(dish);

        ResponseEntity<OrderedDish> responseEntity =
                customerController.getDishFromOrder(customerId, orderId, dishId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
