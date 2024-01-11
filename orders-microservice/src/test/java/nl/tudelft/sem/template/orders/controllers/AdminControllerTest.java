package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.services.AdminAdapter;
import nl.tudelft.sem.template.orders.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

class AdminControllerTest {

    private AdminAdapter adminAdapter;
    private OrderService orderService;
    private AdminController adminController;
    private UUID adminId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        adminAdapter = mock(AdminAdapter.class);
        orderService = mock(OrderService.class);
        adminController = new AdminController(orderService, adminAdapter);

        adminId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }


    // Tests for adminGetAllOrders
    @Test
    void adminGetAllOrdersWhenAdminIdIsNull() {
        ResponseEntity<List<Order>> response = adminController.adminGetAllOrders(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminAdapter);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminGetAllOrdersWhenUserIsNotAdmin() {
        when(adminAdapter.checkRoleById(adminId)).thenReturn(false);

        ResponseEntity<List<Order>> response = adminController.adminGetAllOrders(adminId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(adminAdapter).checkRoleById(adminId);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminGetAllOrdersWhenAdminNotFound() {
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(false);

        ResponseEntity<List<Order>> response = adminController.adminGetAllOrders(adminId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminGetAllOrdersSuccess() {
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(true);
        List<Order> mockOrders = new ArrayList<>();
        when(orderService.findAll()).thenReturn(mockOrders);

        ResponseEntity<List<Order>> response = adminController.adminGetAllOrders(adminId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockOrders);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verify(orderService).findAll();
    }


    // Tests for adminGetOrder

    @Test
    void adminGetOrderWhenAdminIdIsNull() {
        ResponseEntity<Order> response = adminController.adminGetOrder(null, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminAdapter);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminGetOrderWhenOrderIdIsNull() {
        ResponseEntity<Order> response = adminController.adminGetOrder(adminId, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminAdapter);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminGetOrderWhenUserIsNotAdmin() {
        when(adminAdapter.checkRoleById(adminId)).thenReturn(false);

        ResponseEntity<Order> response = adminController.adminGetOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(adminAdapter).checkRoleById(adminId);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminGetOrderWhenAdminNotFound() {
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(false);

        ResponseEntity<Order> response = adminController.adminGetOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminGetOrderWhenOrderNotFound() {
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> response = adminController.adminGetOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verify(orderService).findById(orderId);
    }

    @Test
    void adminGetOrderSuccess() {
        Order mockOrder = new Order(); // Assuming Order is a valid class
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(mockOrder);

        ResponseEntity<Order> response = adminController.adminGetOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockOrder);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verify(orderService).findById(orderId);
    }

    @Test
    void adminGetOrderThrowsException() {
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(true);
        when(orderService.findById(orderId)).thenThrow(RuntimeException.class);

        ResponseEntity<Order> response = adminController.adminGetOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verify(orderService).findById(orderId);
    }


    // Tests for adminUpdateOrder

    @Test
    void adminUpdateOrderWhenAdminIdIsNull() {
        Order updatedOrder = new Order(); // Assuming Order is a valid class
        ResponseEntity<Order> response = adminController.adminUpdateOrder(null, orderId, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminAdapter);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminUpdateOrderWhenOrderIdIsNull() {
        Order updatedOrder = new Order();
        ResponseEntity<Order> response = adminController.adminUpdateOrder(adminId, null, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminAdapter);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminUpdateOrderWhenUserIsNotAdmin() {
        Order updatedOrder = new Order();
        when(adminAdapter.checkRoleById(adminId)).thenReturn(false);

        ResponseEntity<Order> response = adminController.adminUpdateOrder(adminId, orderId, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(adminAdapter).checkRoleById(adminId);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminUpdateOrderWhenAdminNotFound() {
        Order updatedOrder = new Order();
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(false);

        ResponseEntity<Order> response = adminController.adminUpdateOrder(adminId, orderId, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminUpdateOrderWhenOrderNotFound() {
        Order updatedOrder = new Order();
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> response = adminController.adminUpdateOrder(adminId, orderId, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verify(orderService).findById(orderId);
    }

    @Test
    void adminUpdateOrderSuccess() {
        Order updatedOrder = new Order();
        Order savedOrder = new Order();
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(new Order());
        when(orderService.save(updatedOrder)).thenReturn(savedOrder);

        ResponseEntity<Order> response = adminController.adminUpdateOrder(adminId, orderId, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(savedOrder);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verify(orderService).findById(orderId);
        verify(orderService).save(updatedOrder);
    }

    @Test
    void adminUpdateOrderThrowsException() {
        Order updatedOrder = new Order();
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(new Order());
        when(orderService.save(updatedOrder)).thenThrow(RuntimeException.class);

        ResponseEntity<Order> response = adminController.adminUpdateOrder(adminId, orderId, updatedOrder);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verify(orderService).findById(orderId);
        verify(orderService).save(updatedOrder);
    }


    // Tests for adminRemoveOrder

    @Test
    void adminRemoveOrderWhenAdminIdIsNull() {
        ResponseEntity<Void> response = adminController.adminRemoveOrder(null, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminAdapter);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminRemoveOrderWhenOrderIdIsNull() {
        ResponseEntity<Void> response = adminController.adminRemoveOrder(adminId, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(adminAdapter);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminRemoveOrderWhenUserIsNotAdmin() {
        when(adminAdapter.checkRoleById(adminId)).thenReturn(false);

        ResponseEntity<Void> response = adminController.adminRemoveOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(adminAdapter).checkRoleById(adminId);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminRemoveOrderWhenAdminNotFound() {
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(false);

        ResponseEntity<Void> response = adminController.adminRemoveOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verifyNoInteractions(orderService);
    }

    @Test
    void adminRemoveOrderWhenOrderNotFound() {
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Void> response = adminController.adminRemoveOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verify(orderService).findById(orderId);
    }

    @Test
    void adminRemoveOrderSuccess() {
        Order mockOrder = new Order(); // Assuming Order is a valid class
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(mockOrder);

        ResponseEntity<Void> response = adminController.adminRemoveOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verify(orderService).findById(orderId);
        verify(orderService).delete(orderId);
    }

    @Test
    void adminRemoveOrderThrowsException() {
        Order mockOrder = new Order();
        when(adminAdapter.checkRoleById(adminId)).thenReturn(true);
        when(adminAdapter.existsById(adminId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(mockOrder);
        doThrow(RuntimeException.class).when(orderService).delete(orderId);

        ResponseEntity<Void> response = adminController.adminRemoveOrder(adminId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(adminAdapter).checkRoleById(adminId);
        verify(adminAdapter).existsById(adminId);
        verify(orderService).findById(orderId);
        verify(orderService).delete(orderId);
    }

}
