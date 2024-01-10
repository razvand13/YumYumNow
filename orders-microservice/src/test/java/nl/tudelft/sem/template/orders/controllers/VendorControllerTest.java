package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.VendorNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class VendorControllerTest {

    private VendorAdapter vendorAdapter;
    private DishService dishService;
    private VendorController vendorController;
    private OrderService orderService;
    private VendorService vendorService;
    private OrderRepository orderRepository;
    private UUID vendorId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        vendorAdapter = mock(VendorAdapter.class);
        dishService = mock(DishService.class);
        orderService = mock(OrderService.class);
        vendorService = mock(VendorService.class);
        vendorController = new VendorController(vendorAdapter, dishService, orderService, vendorService);

        orderRepository = mock(OrderRepository.class);

        vendorId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    void addDishToMenuWhenVendorIdIsNull() {
        Dish dish = new Dish();

        ResponseEntity<Dish> response = vendorController.addDishToMenu(null, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        verifyNoInteractions(vendorAdapter);
        verifyNoInteractions(dishService);
    }

    @Test
    void addDishToMenuWhenVendorRoleIsInvalid() {
        Dish dish = new Dish();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(false);

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(vendorAdapter).checkRoleById(vendorId);
        verifyNoInteractions(dishService);
    }

    @Test
    void addDishToMenuWhenVendorNotFound() {
        Dish dish = new Dish();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(false);

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(vendorAdapter).checkRoleById(vendorId);
        verify(vendorAdapter).existsById(vendorId);
        verifyNoInteractions(dishService);
    }

    @Test
    void addDishToMenuSuccessful() {
        Dish dish = new Dish();
        Dish dishEntity = new Dish();
        Dish addedDishEntity = new Dish();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(dishService.addDish(vendorId, dishEntity)).thenReturn(addedDishEntity);

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dish);
        verify(vendorAdapter).checkRoleById(vendorId);
        verify(vendorAdapter).existsById(vendorId);
        verify(dishService).addDish(vendorId, dishEntity);

    }

    @Test
    void addDishToMenuVendorNotFound() {
        UUID nonExistentVendorId = UUID.randomUUID();
        Dish dish = new Dish();

        when(vendorAdapter.checkRoleById(any(UUID.class))).thenReturn(true);
        when(vendorAdapter.existsById(any(UUID.class))).thenReturn(true);
        when(dishService.addDish(any(UUID.class), any(Dish.class)))
            .thenThrow(new VendorNotFoundException());

        ResponseEntity<Dish> response = vendorController.addDishToMenu(nonExistentVendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Verify interactions
        verify(vendorAdapter).checkRoleById(nonExistentVendorId);
        verify(vendorAdapter).existsById(nonExistentVendorId);
        verify(dishService).addDish(nonExistentVendorId, dish);
    }

    @Test
    void addDishToMenuBadRequest() {
        Dish dish = new Dish();
        Dish dishEntity = new Dish();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(dishService.addDish(vendorId, dishEntity)).thenThrow(new IllegalArgumentException());

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(dishService).addDish(vendorId, dish);
    }

    @Test
    void addDishToMenuInternalServerError() {
        Dish dish = new Dish();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(dishService.addDish(vendorId, dish)).thenThrow(new RuntimeException());
        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void removeDishFromMenuWhenVendorIdIsNull() {
        UUID dishId = UUID.randomUUID();

        ResponseEntity<Void> response = vendorController.removeDishFromMenu(null, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(vendorAdapter, dishService);
    }

    @Test
    void removeDishFromMenuWhenVendorRoleIsInvalid() {
        UUID dishId = UUID.randomUUID();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(false);

        ResponseEntity<Void> response = vendorController.removeDishFromMenu(vendorId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(vendorAdapter).checkRoleById(vendorId);
        verifyNoInteractions(dishService);
    }

    @Test
    void removeDishFromMenuWhenVendorNotFound() {
        UUID dishId = UUID.randomUUID();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(false);

        ResponseEntity<Void> response = vendorController.removeDishFromMenu(vendorId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(vendorAdapter).checkRoleById(vendorId);
        verify(vendorAdapter).existsById(vendorId);
        verifyNoInteractions(dishService);
    }

    @Test
    void removeDishFromMenuWhenDishNotFound() {
        UUID dishId = UUID.randomUUID();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(dishService.removeDish(vendorId, dishId)).thenReturn(false);

        ResponseEntity<Void> response = vendorController.removeDishFromMenu(vendorId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(dishService).removeDish(vendorId, dishId);
    }

    @Test
    void removeDishFromMenuInternalServerError() {
        UUID dishId = UUID.randomUUID();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(dishService.removeDish(vendorId, dishId)).thenThrow(new RuntimeException());

        ResponseEntity<Void> response = vendorController.removeDishFromMenu(vendorId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void removeDishFromMenuSuccessful() {
        UUID dishId = UUID.randomUUID();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(dishService.removeDish(vendorId, dishId)).thenReturn(true);

        ResponseEntity<Void> response = vendorController.removeDishFromMenu(vendorId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(vendorAdapter).checkRoleById(vendorId);
        verify(vendorAdapter).existsById(vendorId);
        verify(dishService).removeDish(vendorId, dishId);
    }


    @Test
    void testGetOrderDetailsWrongUserType() {
        UUID customerId = UUID.randomUUID();

        when(vendorAdapter.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<Order> response = vendorController.getOrderDetails(customerId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testGetOrderDetailsOrderNotExists() {
        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> response = vendorController.getOrderDetails(vendorId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetOrderDetailsVendorNotExists() {
        Order order = new Order();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(vendorAdapter.existsById(vendorId)).thenReturn(false);

        ResponseEntity<Order> response = vendorController.getOrderDetails(vendorId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetOrderDetailsOrderDoesNotBelongToVendor() {
        Order order = new Order();
        order.setVendorId(UUID.randomUUID());

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);

        ResponseEntity<Order> response = vendorController.getOrderDetails(vendorId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testGetOrderDetailsBadRequest() {
        ResponseEntity<Order> response1 = vendorController.getOrderDetails(null, orderId);
        ResponseEntity<Order> response2 = vendorController.getOrderDetails(vendorId, null);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testGetOrderDetails() {
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
        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(false);

        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(vendorId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testGetVendorOrdersVendorDoesNotExist() {
        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(false);

        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(vendorId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetVendorOrdersEmpty() {
        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(orderRepository.findByVendorId(vendorId)).thenReturn(new ArrayList<>());

        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(vendorId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void testGetVendorOrdersBadResponse() {
        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testGetVendorOrders() {
        Order order1 = new Order();
        order1.setID(UUID.randomUUID());
        Order order2 = new Order();
        order2.setID(UUID.randomUUID());
        Order order3 = new Order();
        order3.setID(UUID.randomUUID());

        List<Order> orders = List.of(order1, order2, order3);

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(vendorService.getVendorOrders(vendorId)).thenReturn(orders);

        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(vendorId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyInAnyOrder(order1, order2, order3);
    }
}
