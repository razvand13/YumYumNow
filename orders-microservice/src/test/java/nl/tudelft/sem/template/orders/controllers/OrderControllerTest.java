package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Status;
import nl.tudelft.sem.template.model.UpdateOrderStatusRequest;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.validator.DataValidationField;
import nl.tudelft.sem.template.orders.validator.DataValidator;
import nl.tudelft.sem.template.orders.validator.UserAuthorizationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

class OrderControllerTest {

    @Mock
    private IOrderService orderService;
    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private OrderController orderController;

    private UUID orderId;
    private UpdateOrderStatusRequest updateOrderStatusRequest;

    @BeforeEach
    void setup() {
        orderService = mock(IOrderService.class);
        applicationContext = mock(ApplicationContext.class);

        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.UPDATEORDERSTATUSREQUEST))))
                .thenReturn(new DataValidator(List.of(DataValidationField.UPDATEORDERSTATUSREQUEST),
                        orderService, null, null));

        when(applicationContext.getBean(UserAuthorizationValidator.class))
                .thenReturn(new UserAuthorizationValidator(null, null, orderService, null));

        orderController = new OrderController(orderService, applicationContext);
        orderId = UUID.randomUUID();
        updateOrderStatusRequest = new UpdateOrderStatusRequest();
        updateOrderStatusRequest.setStatus(Status.PENDING);
    }

    @Test
    void updateOrderStatusSuccess() {
        Order existingOrder = new Order();
        existingOrder.setStatus(Status.DELIVERED);

        when(orderService.findById(orderId)).thenReturn(existingOrder);
        when(orderService.save(any(Order.class))).thenReturn(existingOrder);

        ResponseEntity<Order> responseEntity = orderController.updateOrderStatus(orderId, updateOrderStatusRequest);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(existingOrder);
    }

    @Test
    void updateOrderStatusBadRequestId() {
        ResponseEntity<Order> responseEntity = orderController.updateOrderStatus(null, updateOrderStatusRequest);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateOrderStatusBadRequestStatusUpdateOrderStatus() {
        when(orderService.findById(orderId)).thenReturn(new Order());

        ResponseEntity<Order> responseEntity = orderController.updateOrderStatus(orderId, null);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateOrderStatusBadRequestStatus() {
        updateOrderStatusRequest.setStatus(null);
        when(orderService.findById(orderId)).thenReturn(new Order());
        
        ResponseEntity<Order> responseEntity = orderController.updateOrderStatus(orderId, updateOrderStatusRequest);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateOrderStatusNotFound() {
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> responseEntity = orderController.updateOrderStatus(orderId, updateOrderStatusRequest);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
