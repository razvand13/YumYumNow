package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.integration.AdminFacade;
import nl.tudelft.sem.template.orders.services.OrderService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AdminControllerTest {

    private AdminFacade adminFacade;
    private OrderService orderService;
    private ApplicationContext applicationContext;
    private AdminController adminController;
    private UUID adminId;
    private UUID orderId;
    private Order order;

    @BeforeEach
    void setUp() {
        adminFacade = mock(AdminFacade.class);
        orderService = mock(OrderService.class);
        applicationContext = mock(ApplicationContext.class);
        adminController = new AdminController(orderService, applicationContext);

        adminId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        //Set up validators
        DataValidator dataValidatorUser = new DataValidator(orderService, null, null,
                List.of(DataValidationField.USER));
        DataValidator dataValidatorUserOrder = new DataValidator(orderService, null, null,
                List.of(DataValidationField.USER, DataValidationField.ORDER));
        when(applicationContext.getBean(eq(DataValidator.class), eq(List.of(DataValidationField.USER))))
                .thenReturn(dataValidatorUser);
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER, DataValidationField.ORDER))))
                .thenReturn(dataValidatorUserOrder);

        UserAuthorizationValidator userAuthorizationValidator =
                new UserAuthorizationValidator(null, null, null, null, adminFacade);
        when(applicationContext.getBean(UserAuthorizationValidator.class)).thenReturn(userAuthorizationValidator);


        //Fill in example order
        this.order = new Order();
        order.setID(orderId);
        order.setCustomerId(UUID.randomUUID());
        order.setVendorId(UUID.randomUUID());
        when(orderService.findById(orderId)).thenReturn(order);
    }


    // Tests for adminGetAllOrders
    @Test
    void adminGetAllOrdersWhenAdminIdIsNull() {
        ResponseEntity<List<Order>> response = adminController.adminGetAllOrders(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminFacade);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminGetAllOrdersWhenUserIsNotAdmin() {
        when(adminFacade.checkRoleById(adminId)).thenReturn(false);

        ResponseEntity<List<Order>> response = adminController.adminGetAllOrders(adminId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(adminFacade).checkRoleById(adminId);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminGetAllOrdersWhenAdminNotFound() {
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(false);

        ResponseEntity<List<Order>> response = adminController.adminGetAllOrders(adminId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(adminFacade).checkRoleById(adminId);
        verify(adminFacade).existsById(adminId);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminGetAllOrdersSuccess() {
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(true);
        List<Order> mockOrders = new ArrayList<>();
        when(orderService.findAll()).thenReturn(mockOrders);

        ResponseEntity<List<Order>> response = adminController.adminGetAllOrders(adminId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockOrders);
        verify(adminFacade).checkRoleById(adminId);
        verify(adminFacade).existsById(adminId);
        verify(orderService).findAll();
    }

    @Test
    void adminGetAllOrderThrowsException() {
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(true);
        when(orderService.findAll()).thenThrow(RuntimeException.class);

        ResponseEntity<List<Order>> response = adminController.adminGetAllOrders(adminId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(adminFacade).checkRoleById(adminId);
        verify(adminFacade).existsById(adminId);
    }

    // Tests for adminGetOrder

    @Test
    void adminGetOrderWhenAdminIdIsNull() {
        ResponseEntity<Order> response = adminController.adminGetOrder(null, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminFacade);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminGetOrderWhenOrderIdIsNull() {
        ResponseEntity<Order> response = adminController.adminGetOrder(adminId, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminFacade);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminGetOrderWhenUserIsNotAdmin() {
        when(adminFacade.checkRoleById(adminId)).thenReturn(false);

        ResponseEntity<Order> response = adminController.adminGetOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(adminFacade).checkRoleById(adminId);
    }

    @Test
    void adminGetOrderWhenAdminNotFound() {
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(false);

        ResponseEntity<Order> response = adminController.adminGetOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(adminFacade).checkRoleById(adminId);
        verify(adminFacade).existsById(adminId);
    }

    @Test
    void adminGetOrderWhenOrderNotFound() {
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> response = adminController.adminGetOrder(adminId, UUID.randomUUID());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void adminGetOrderSuccess() {
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(true);

        ResponseEntity<Order> response = adminController.adminGetOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(order);
        verify(adminFacade).checkRoleById(adminId);
        verify(adminFacade).existsById(adminId);
        verify(orderService, atLeast(1)).findById(orderId);
    }


    // Tests for adminUpdateOrder

    @Test
    void adminUpdateOrderWhenAdminIdIsNull() {
        Order updatedOrder = new Order(); // Assuming Order is a valid class
        ResponseEntity<Order> response = adminController.adminUpdateOrder(null, orderId, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminFacade);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminUpdateOrderWhenOrderIdIsNull() {
        Order updatedOrder = new Order();
        ResponseEntity<Order> response = adminController.adminUpdateOrder(adminId, null, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminFacade);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminUpdateOrderWhenUserIsNotAdmin() {
        Order updatedOrder = new Order();
        when(adminFacade.checkRoleById(adminId)).thenReturn(false);

        ResponseEntity<Order> response = adminController.adminUpdateOrder(adminId, orderId, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(adminFacade).checkRoleById(adminId);
    }

    @Test
    void adminUpdateOrderWhenAdminNotFound() {
        Order updatedOrder = new Order();
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(false);

        ResponseEntity<Order> response = adminController.adminUpdateOrder(adminId, orderId, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(adminFacade).checkRoleById(adminId);
        verify(adminFacade).existsById(adminId);
    }

    @Test
    void adminUpdateOrderWhenOrderNotFound() {
        Order updatedOrder = new Order();
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(true);

        UUID invalidOrder = UUID.randomUUID();
        ResponseEntity<Order> response = adminController.adminUpdateOrder(adminId, invalidOrder, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(orderService).findById(invalidOrder);
    }

    @Test
    void adminUpdateOrderSuccess() {
        Order updatedOrder = new Order();
        Order savedOrder = new Order();
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(true);
        when(orderService.save(updatedOrder)).thenReturn(savedOrder);

        ResponseEntity<Order> response = adminController.adminUpdateOrder(adminId, orderId, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminFacade).checkRoleById(adminId);
        verify(adminFacade).existsById(adminId);
        verify(orderService, atLeast(1)).findById(orderId);
        verify(orderService).save(updatedOrder);
    }

    @Test
    void adminUpdateOrderThrowsException() {
        Order updatedOrder = new Order();
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(true);
        when(orderService.save(updatedOrder)).thenThrow(RuntimeException.class);

        ResponseEntity<Order> response = adminController.adminUpdateOrder(adminId, orderId, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(adminFacade).checkRoleById(adminId);
        verify(adminFacade).existsById(adminId);
        verify(orderService, atLeast(1)).findById(orderId);
        verify(orderService).save(updatedOrder);
    }


    // Tests for adminRemoveOrder

    @Test
    void adminRemoveOrderWhenAdminIdIsNull() {
        ResponseEntity<Void> response = adminController.adminRemoveOrder(null, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminFacade);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminRemoveOrderWhenOrderIdIsNull() {
        ResponseEntity<Void> response = adminController.adminRemoveOrder(adminId, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminFacade);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminRemoveOrderWhenUserIsNotAdmin() {
        when(adminFacade.checkRoleById(adminId)).thenReturn(false);

        ResponseEntity<Void> response = adminController.adminRemoveOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(adminFacade).checkRoleById(adminId);
    }

    @Test
    void adminRemoveOrderWhenAdminNotFound() {
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(false);

        ResponseEntity<Void> response = adminController.adminRemoveOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(adminFacade).checkRoleById(adminId);
        verify(adminFacade).existsById(adminId);
    }

    @Test
    void adminRemoveOrderWhenOrderNotFound() {
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        UUID invalidOrder = UUID.randomUUID();
        ResponseEntity<Void> response = adminController.adminRemoveOrder(adminId, invalidOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(orderService).findById(invalidOrder);
    }

    @Test
    void adminRemoveOrderSuccess() {
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(true);

        ResponseEntity<Void> response = adminController.adminRemoveOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminFacade).checkRoleById(adminId);
        verify(adminFacade).existsById(adminId);
        verify(orderService, atLeast(1)).findById(orderId);
        verify(orderService).delete(orderId);
    }

    @Test
    void adminRemoveOrderThrowsException() {
        when(adminFacade.checkRoleById(adminId)).thenReturn(true);
        when(adminFacade.existsById(adminId)).thenReturn(true);
        doThrow(RuntimeException.class).when(orderService).delete(orderId);

        ResponseEntity<Void> response = adminController.adminRemoveOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(adminFacade).checkRoleById(adminId);
        verify(adminFacade).existsById(adminId);
        verify(orderService, atLeast(1)).findById(orderId);
        verify(orderService).delete(orderId);
    }

}
