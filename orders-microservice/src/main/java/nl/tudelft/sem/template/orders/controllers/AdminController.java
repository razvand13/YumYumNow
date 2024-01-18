package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.api.AdminApi;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.integration.AdminFacade;
import nl.tudelft.sem.template.orders.validator.DataValidationField;
import nl.tudelft.sem.template.orders.validator.DataValidator;
import nl.tudelft.sem.template.orders.validator.UserAuthorizationValidator;
import nl.tudelft.sem.template.orders.validator.UserType;
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
public class AdminController implements AdminApi {


    private final transient IOrderService orderService;
    private final transient ApplicationContext applicationContext;

    /**
     * Constructs an AdminController. Should only be called manually during testing
     */
    @Autowired
    public AdminController(IOrderService orderService, ApplicationContext applicationContext) {
        this.orderService = orderService;
        this.applicationContext = applicationContext;
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
        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class, List.of(DataValidationField.USER));
        UserAuthorizationValidator userAuthorizationValidator = applicationContext.getBean(UserAuthorizationValidator.class);
        //Set validation chain
        dataValidator.setNext(userAuthorizationValidator);
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(adminId);
        request.setUserType(UserType.ADMIN);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
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
        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class,
                List.of(DataValidationField.USER, DataValidationField.ORDER));
        UserAuthorizationValidator userAuthorizationValidator = applicationContext.getBean(UserAuthorizationValidator.class);
        //Set validation chain
        dataValidator.setNext(userAuthorizationValidator);
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(adminId);
        request.setUserType(UserType.ADMIN);
        request.setOrderUUID(orderId);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
        }

        Order order = orderService.findById(orderId);
        return ResponseEntity.ok(order);
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
        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class,
                List.of(DataValidationField.USER, DataValidationField.ORDER));
        UserAuthorizationValidator userAuthorizationValidator = applicationContext.getBean(UserAuthorizationValidator.class);
        //Set validation chain
        dataValidator.setNext(userAuthorizationValidator);
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(adminId);
        request.setUserType(UserType.ADMIN);
        request.setOrderUUID(orderId);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
        }

        try {
            updatedOrder.setID(orderId);

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
        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class,
                List.of(DataValidationField.USER, DataValidationField.ORDER));
        UserAuthorizationValidator userAuthorizationValidator = applicationContext.getBean(UserAuthorizationValidator.class);
        //Set validation chain
        dataValidator.setNext(userAuthorizationValidator);
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(adminId);
        request.setUserType(UserType.ADMIN);
        request.setOrderUUID(orderId);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
        }

        try {
            orderService.delete(orderId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
