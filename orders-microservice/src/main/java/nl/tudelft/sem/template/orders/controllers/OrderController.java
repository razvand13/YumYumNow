package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.api.OrderApi;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.UpdateOrderStatusRequest;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.validator.DataValidationField;
import nl.tudelft.sem.template.orders.validator.DataValidator;
import nl.tudelft.sem.template.orders.validator.ValidationFailureException;
import nl.tudelft.sem.template.orders.validator.ValidatorRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class OrderController implements OrderApi {
    private final transient IOrderService orderService;
    private final transient ApplicationContext applicationContext;

    @Autowired
    public OrderController(IOrderService orderService, ApplicationContext applicationContext) {
        this.orderService = orderService;
        this.applicationContext = applicationContext;
    }

    /**
     * PUT /order/{orderId}/status : Update the status of an order
     * Allows a vendor, courier or an admin to update the status of a specific order.
     *
     * @param orderId                  (required)
     * @param updateOrderStatusRequest (required)
     * @return Order status updated successfully. (status code 200)
     *      or Bad request - Invalid status or request format. (status code 400)
     *      or Not Found - Order ID does not exist. (status code 404)
     *      or Internal Server Error - An unexpected error occurred. (status code 500)
     */
    @Override
    public ResponseEntity<Order> updateOrderStatus(UUID orderId, UpdateOrderStatusRequest updateOrderStatusRequest) {
        /*
        Every user is authorized to change order status, assuming there is no malicious intent.
        This makes it possible for the Delivery Microservice to change order status freely.
         */

        if (orderId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Order order = orderService.findById(orderId);

        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class,
                List.of(DataValidationField.UPDATEORDERSTATUSREQUEST));
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest();
        request.setUpdateOrderStatusRequest(updateOrderStatusRequest);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
        }

        order.setStatus(updateOrderStatusRequest.getStatus());
        Order savedOrder = orderService.save(order);

        return ResponseEntity.ok(savedOrder);
    }
}
