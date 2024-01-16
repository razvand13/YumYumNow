package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.api.AdminApi;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.integration.AdminFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class AdminController implements AdminApi {


    private final transient IOrderService orderService;
    private final transient AdminFacade adminFacade;

    @Autowired
    public AdminController(IOrderService orderService, AdminFacade adminFacade) {
        this.orderService = orderService;
        this.adminFacade = adminFacade;
    }

    /**
     * GET /admin/{adminId}/orders : View all orders
     * Allows an admin to view all orders in the system.
     *
     * @param adminId The UUID of the admin. (required)
     * @return List of all orders. (status code 200)
     *         or Bad Request - Invalid admin UUID. (status code 400)
     *         or Unauthorized - User is not an admin. (status code 403)
     *         or Not Found - Admin not found. (status code 404)
     *         or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */

    @Override
    public ResponseEntity<List<Order>> adminGetAllOrders(UUID adminId) {
        if (adminId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        //verify that user is admin
        if (!adminFacade.checkRoleById(adminId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // verify that admin exists
        if (!adminFacade.existsById(adminId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            List<Order> orders = orderService.findAll();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /admin/{adminId}/order/{orderId} : Get a specific order
     * Allows an admin to retrieve a specific order for review or other administrative purposes.
     *
     * @param adminId The UUID of the admin. (required)
     * @param orderId The UUID of the order to retrieve. (required)
     * @return The requested order. (status code 200)
     *         or Bad Request - Invalid admin or order UUID. (status code 400)
     *         or Unauthorized - User is not an admin. (status code 403)
     *         or Not Found - Admin or order not found. (status code 404)
     *         or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<Order> adminGetOrder(UUID adminId, UUID orderId) {
        if (adminId == null || orderId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        //verify that user is admin
        if (!adminFacade.checkRoleById(adminId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // verify that admin exists
        if (!adminFacade.existsById(adminId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Order order = orderService.findById(orderId);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * PUT /admin/{adminId}/order/{orderId} : Modify a specific order
     * Allows an admin to modify any attribute of a specific order.
     *
     * @param adminId      The UUID of the admin. (required)
     * @param orderId      The UUID of the order to update. (required)
     * @param updatedOrder The updated order details. (required)
     * @return The updated order. (status code 200)
     *         or Bad Request - Invalid admin or order UUID, or invalid order details. (status code 400)
     *         or Unauthorized - User is not an admin. (status code 403)
     *         or Not Found - Admin or order not found. (status code 404)
     *         or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<Order> adminUpdateOrder(UUID adminId, UUID orderId, Order updatedOrder) {
        if (adminId == null || orderId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        //verify that user is admin
        if (!adminFacade.checkRoleById(adminId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // verify that admin exists
        if (!adminFacade.existsById(adminId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Order existingOrder = orderService.findById(orderId);
            if (existingOrder == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Order savedOrder = orderService.save(updatedOrder);
            return ResponseEntity.ok(savedOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /admin/{adminId}/order/{orderId} : Remove an order
     * Allows an admin to remove any order from the system.
     *
     * @param adminId The UUID of the admin. (required)
     * @param orderId The UUID of the order to be removed. (required)
     * @return Confirmation of order deletion. (status code 200)
     *         or Bad Request - Invalid admin or order UUID. (status code 400)
     *         or Unauthorized - User is not an admin. (status code 403)
     *         or Not Found - Admin or order not found. (status code 404)
     *         or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<Void> adminRemoveOrder(UUID adminId, UUID orderId) {
        if (adminId == null || orderId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        //verify that user is admin
        if (!adminFacade.checkRoleById(adminId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // verify that admin exists
        if (!adminFacade.existsById(adminId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            Order order = orderService.findById(orderId);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            orderService.delete(orderId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
