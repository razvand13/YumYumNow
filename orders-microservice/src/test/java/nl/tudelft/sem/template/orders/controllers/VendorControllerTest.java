package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.VendorNotFoundException;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import nl.tudelft.sem.template.orders.services.DishService;
import nl.tudelft.sem.template.orders.services.OrderService;
import nl.tudelft.sem.template.orders.integration.VendorFacade;
import nl.tudelft.sem.template.orders.services.VendorService;
import nl.tudelft.sem.template.orders.validator.DataValidationField;
import nl.tudelft.sem.template.orders.validator.DataValidator;
import nl.tudelft.sem.template.orders.validator.UserAuthorizationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class VendorControllerTest {

    private VendorFacade vendorFacade;
    private DishService dishService;
    private VendorController vendorController;
    private OrderService orderService;
    private VendorService vendorService;
    private OrderRepository orderRepository;
    private UUID vendorId;
    private UUID orderId;


    @BeforeEach
    void setUp() {
        vendorFacade = mock(VendorFacade.class);
        dishService = mock(DishService.class);
        orderService = mock(OrderService.class);
        vendorService = mock(VendorService.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);

        //Validators need to be defined for the mocked application context, including each combination of used
        //DataValidationFields. Unfortunately capturing the argument that getBean is called with is not an option
        when(applicationContext.getBean(eq(DataValidator.class), eq(List.of(DataValidationField.USER))))
                .thenReturn(new DataValidator(List.of(DataValidationField.USER), orderService, dishService, vendorFacade));
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER, DataValidationField.ORDER))))
                .thenReturn(new DataValidator(List.of(DataValidationField.USER,
                        DataValidationField.ORDER), orderService, dishService, vendorFacade));
        when(applicationContext.getBean(UserAuthorizationValidator.class))
                .thenReturn(new UserAuthorizationValidator(null, vendorFacade, orderService, dishService));


        vendorController = new VendorController(dishService, orderService, vendorService, applicationContext);

        orderRepository = mock(OrderRepository.class);

        vendorId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    void addDishToMenuWhenVendorIdIsNull() {
        Dish dish = new Dish();

        ResponseEntity<Dish> response = vendorController.addDishToMenu(null, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        verifyNoInteractions(vendorFacade);
        verifyNoInteractions(dishService);
    }

    @Test
    void addDishToMenuWhenVendorRoleIsInvalid() {
        Dish dish = new Dish();

        when(vendorFacade.checkRoleById(vendorId)).thenReturn(false);

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(vendorFacade).checkRoleById(vendorId);
        verifyNoInteractions(dishService);
    }

    @Test
    void addDishToMenuWhenVendorNotFound() {
        Dish dish = new Dish();

        when(vendorFacade.checkRoleById(vendorId)).thenReturn(true);
        when(vendorFacade.existsById(vendorId)).thenReturn(false);

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(vendorFacade).checkRoleById(vendorId);
        verify(vendorFacade).existsById(vendorId);
        verifyNoInteractions(dishService);
    }

    @Test
    void addDishToMenuSuccessful() {
        Dish dish = new Dish();
        Dish dishEntity = new Dish();
        Dish addedDishEntity = new Dish();

        when(vendorFacade.checkRoleById(vendorId)).thenReturn(true);
        when(vendorFacade.existsById(vendorId)).thenReturn(true);
        when(dishService.addDish(vendorId, dishEntity)).thenReturn(addedDishEntity);

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dish);
        verify(vendorFacade).checkRoleById(vendorId);
        verify(vendorFacade).existsById(vendorId);
        verify(dishService).addDish(vendorId, dishEntity);

    }

    @Test
    void addDishToMenuVendorNotFound() {
        UUID nonExistentVendorId = UUID.randomUUID();
        Dish dish = new Dish();

        when(vendorFacade.checkRoleById(any(UUID.class))).thenReturn(true);
        when(vendorFacade.existsById(any(UUID.class))).thenReturn(true);
        when(dishService.addDish(any(UUID.class), any(Dish.class)))
            .thenThrow(new VendorNotFoundException());

        ResponseEntity<Dish> response = vendorController.addDishToMenu(nonExistentVendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Verify interactions
        verify(vendorFacade).checkRoleById(nonExistentVendorId);
        verify(vendorFacade).existsById(nonExistentVendorId);
        verify(dishService).addDish(nonExistentVendorId, dish);
    }

    @Test
    void addDishToMenuBadRequest() {
        Dish dish = new Dish();
        Dish dishEntity = new Dish();

        when(vendorFacade.checkRoleById(vendorId)).thenReturn(true);
        when(vendorFacade.existsById(vendorId)).thenReturn(true);
        when(dishService.addDish(vendorId, dishEntity)).thenThrow(new IllegalArgumentException());

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(dishService).addDish(vendorId, dish);
    }

    @Test
    void addDishToMenuInternalServerError() {
        Dish dish = new Dish();

        when(vendorFacade.checkRoleById(vendorId)).thenReturn(true);
        when(vendorFacade.existsById(vendorId)).thenReturn(true);
        when(dishService.addDish(vendorId, dish)).thenThrow(new RuntimeException());
        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void testGetOrderDetailsWrongUserType() {
        UUID customerId = UUID.randomUUID();
        Order order = new Order();
        order.setCustomerId(UUID.randomUUID());
        order.setVendorId(vendorId);

        when(vendorFacade.checkRoleById(customerId)).thenReturn(false);
        when(orderService.findById(orderId)).thenReturn(order);

        ResponseEntity<Order> response = vendorController.getOrderDetails(customerId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testGetOrderDetailsOrderNotExists() {
        when(vendorFacade.checkRoleById(vendorId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> response = vendorController.getOrderDetails(vendorId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetOrderDetailsVendorNotExists() {
        Order order = new Order();
        order.setCustomerId(UUID.randomUUID());
        order.setVendorId(vendorId);

        when(vendorFacade.checkRoleById(vendorId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(vendorFacade.existsById(vendorId)).thenReturn(false);

        ResponseEntity<Order> response = vendorController.getOrderDetails(vendorId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetOrderDetailsOrderDoesNotBelongToVendor() {
        Order order = new Order();
        order.setCustomerId(UUID.randomUUID());
        order.setVendorId(UUID.randomUUID());

        when(vendorFacade.checkRoleById(vendorId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(vendorFacade.existsById(vendorId)).thenReturn(true);

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
        order.setCustomerId(UUID.randomUUID());
        order.setID(orderId);

        when(vendorFacade.checkRoleById(vendorId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(vendorFacade.existsById(vendorId)).thenReturn(true);

        ResponseEntity<Order> response = vendorController.getOrderDetails(vendorId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(order);
    }

    @Test
    void testGetVendorOrdersWrongUserType() {
        when(vendorFacade.checkRoleById(vendorId)).thenReturn(false);

        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(vendorId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testGetVendorOrdersVendorDoesNotExist() {
        when(vendorFacade.checkRoleById(vendorId)).thenReturn(true);
        when(vendorFacade.existsById(vendorId)).thenReturn(false);

        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(vendorId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetVendorOrdersEmpty() {
        when(vendorFacade.checkRoleById(vendorId)).thenReturn(true);
        when(vendorFacade.existsById(vendorId)).thenReturn(true);
        when(orderRepository.findByVendorId(vendorId)).thenReturn(new ArrayList<Order>());

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

        when(vendorFacade.checkRoleById(vendorId)).thenReturn(true);
        when(vendorFacade.existsById(vendorId)).thenReturn(true);
        when(vendorService.getVendorOrders(vendorId)).thenReturn(orders);

        ResponseEntity<List<Order>> response = vendorController.getVendorOrders(vendorId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyInAnyOrder(order1, order2, order3);
    }
}
