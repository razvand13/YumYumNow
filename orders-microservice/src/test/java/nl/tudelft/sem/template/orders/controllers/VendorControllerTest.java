package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.entities.Order;
import nl.tudelft.sem.template.orders.external.PaymentMock;
import nl.tudelft.sem.template.orders.mappers.DishMapper;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import nl.tudelft.sem.template.orders.services.DishService;
import nl.tudelft.sem.template.orders.services.OrderService;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
import nl.tudelft.sem.template.orders.services.VendorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class VendorControllerTest {

    private VendorAdapter vendorAdapter;
    private DishService dishService;
    private DishMapper dishMapper;
    private VendorController vendorController;
    private OrderService orderService;
    private VendorService vendorService;
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        vendorAdapter = mock(VendorAdapter.class);
        dishService = mock(DishService.class);
        dishMapper = mock(DishMapper.class);
        orderService = mock(OrderService.class);
        vendorService = mock(VendorService.class);
        vendorController = new VendorController(vendorAdapter, dishService, dishMapper, orderService, vendorService);

        orderRepository = mock(OrderRepository.class);
    }

    @Test
    void addDishToMenuWhenVendorRoleIsInvalid() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(false);

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(vendorAdapter).checkRoleById(vendorId);
        verifyNoInteractions(dishService);
        verifyNoInteractions(dishMapper);
    }

    @Test
    void addDishToMenuWhenVendorNotFound() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(false);

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(vendorAdapter).checkRoleById(vendorId);
        verify(vendorAdapter).existsById(vendorId);
        verifyNoInteractions(dishService);
        verifyNoInteractions(dishMapper);
    }

    @Test
    void addDishToMenuSuccessful() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();
        DishEntity dishEntity = new DishEntity();
        DishEntity addedDishEntity = new DishEntity();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(dishMapper.toEntity(dish)).thenReturn(dishEntity);
        when(dishService.addDish(vendorId, dishEntity)).thenReturn(addedDishEntity);
        when(dishMapper.toDTO(addedDishEntity)).thenReturn(dish);

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dish);
        verify(vendorAdapter).checkRoleById(vendorId);
        verify(vendorAdapter).existsById(vendorId);
        verify(dishMapper).toEntity(dish);
        verify(dishService).addDish(vendorId, dishEntity);
        verify(dishMapper).toDTO(addedDishEntity);
    }

    @Test
    void addDishToMenuBadRequest() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();
        DishEntity dishEntity = new DishEntity();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(dishMapper.toEntity(dish)).thenReturn(dishEntity);
        when(dishService.addDish(vendorId, dishEntity)).thenThrow(new IllegalArgumentException());

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(dishService).addDish(vendorId, dishEntity);
    }

    @Test
    void addDishToMenuInternalServerError() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(dishMapper.toEntity(dish)).thenThrow(new RuntimeException());

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(dishMapper).toEntity(dish);
    }

    @Test
    void testGetOrderDetailsWrongUserType() {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(vendorAdapter.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<Order> response = vendorController.getOrderDetails(customerId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testGetOrderDetailsOrderNotExists() {
        UUID vendorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> response = vendorController.getOrderDetails(vendorId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetOrderDetailsVendorNotExists() {
        UUID vendorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Order order = new Order();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(vendorAdapter.existsById(vendorId)).thenReturn(false);

        ResponseEntity<Order> response = vendorController.getOrderDetails(vendorId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetOrderDetailsOrderDoesNotBelongToVendor() {
        UUID vendorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setVendorId(UUID.randomUUID());

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);

        ResponseEntity<Order> response = vendorController.getOrderDetails(vendorId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testGetOrderDetails() {
        UUID vendorId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setVendorId(vendorId);
        order.setID(orderId);

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);

        ResponseEntity<Order> response = vendorController.getOrderDetails(vendorId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(order);
    }

    @Test
    void testGetVendorOrdersWrongUserType() {
        UUID vendorId = UUID.randomUUID();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(false);

        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(vendorId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testGetVendorOrdersVendorDoesNotExist() {
        UUID vendorId = UUID.randomUUID();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(false);

        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(vendorId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetVendorOrdersEmpty() {
        UUID vendorId = UUID.randomUUID();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(orderRepository.getOrdersByVendorId(vendorId)).thenReturn(new ArrayList<Order>());

        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(vendorId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void testGetVendorOrders() {
        Order order1 = new Order();
        order1.setID(UUID.randomUUID());
        Order order2 = new Order();
        order2.setID(UUID.randomUUID());
        Order order3 = new Order();
        order3.setID(UUID.randomUUID());

        UUID vendorId = UUID.randomUUID();

        List<Order> orders = List.of(order1, order2, order3);

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(vendorService.getVendorOrders(vendorId)).thenReturn(orders);

        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(vendorId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyInAnyOrder(order1, order2, order3);
    }
}
