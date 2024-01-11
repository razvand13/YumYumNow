package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.api.OrderApi;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.UpdateOrderStatusRequest;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class OrderController implements OrderApi {
    private final transient IOrderService orderService;

    @Autowired
    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * PUT /order/{orderId}/status : Update the status of an order
     * Allows a vendor, courier or an admin to update the status of a specific order.
     *
     * @param orderId                  (required)
     * @param updateOrderStatusRequest (required)
     * @return Order status updated successfully. (status code 200)
     *      or Bad request - Invalid status or request format. (status code 400)
     *      or Unauthorized - User is not authorized to update order status. (status code 401)
     *      or Not Found - Order ID does not exist. (status code 404)
     *      or Internal Server Error - An unexpected error occurred. (status code 500)
     */
    @Override
    public ResponseEntity<Order> updateOrderStatus(UUID orderId, UpdateOrderStatusRequest updateOrderStatusRequest) {
        if (orderId == null || updateOrderStatusRequest == null || updateOrderStatusRequest.getStatus() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        /*
        Every user is authorized to change order status, assuming there is no malicious intent.
        This makes it possible for the Delivery Microservice to change order status freely.
         */

        Order order = orderService.findById(orderId);

        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        order.setStatus(updateOrderStatusRequest.getStatus());
        Order savedOrder = orderService.save(order);

        return ResponseEntity.ok(savedOrder);
    }
}
