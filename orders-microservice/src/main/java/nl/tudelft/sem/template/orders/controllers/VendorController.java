package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.api.VendorApi;
import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.VendorNotFoundException;
import nl.tudelft.sem.template.orders.domain.IDishService;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.domain.IVendorService;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
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

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class VendorController implements VendorApi {

    private final transient VendorAdapter vendorAdapter;
    private final transient IDishService dishService;
    private final transient IOrderService orderService;
    private final transient IVendorService vendorService;
    private final transient ApplicationContext applicationContext;

    /**
     * Creates an instance of the VendorController.
     *
     * @param vendorAdapter the vendor adapter
     * @param dishService   the dish service
     * @param vendorService the vendor service
     */
    @Autowired
    public VendorController(VendorAdapter vendorAdapter, IDishService dishService,
                            IOrderService orderService, IVendorService vendorService,
                            ApplicationContext applicationContext) {
        this.vendorAdapter = vendorAdapter;
        this.dishService = dishService;
        this.orderService = orderService;
        this.vendorService = vendorService;
        this.applicationContext = applicationContext;
    }

    /**
     * Adds a dish to the menu of a vendor.
     *
     * @param vendorId the id of the vendor
     * @param dish the dish to be added
     * @return the added dish
     */
    @Override
    public ResponseEntity<Dish> addDishToMenu(UUID vendorId, Dish dish) {
        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class, List.of(DataValidationField.USER));
        UserAuthorizationValidator userAuthorizationValidator = applicationContext.getBean(UserAuthorizationValidator.class);
        //Set validation chain
        dataValidator.setNext(userAuthorizationValidator);
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(vendorId);
        request.setUserType(UserType.VENDOR);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
        }

        try {

            Dish addedDish = dishService.addDish(vendorId, dish);
            return ResponseEntity.ok(addedDish);
        } catch (VendorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            // Bad request from service
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /vendor/{vendorId}/orders/{orderId} : Get details of a specific order
     * (including how much money is earned and special requirements)
     *
     * @param vendorId (required)
     * @param orderId  (required)
     * @return Details of the specified order (status code 200)
     *     or Bad Request - Invalid request parameters. (status code 400)
     *     or Unauthorized - User is not a vendor/order does not belong to vendor. (status code 401)
     *     or Order or vendor not found. (status code 404)
     *     or Internal Server Error - An unexpected error occurred. (status code 500)
     */
    @Override
    public ResponseEntity<Order> getOrderDetails(UUID vendorId, UUID orderId) {
        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class,
                List.of(DataValidationField.USER, DataValidationField.ORDER));
        UserAuthorizationValidator userAuthorizationValidator = applicationContext.getBean(UserAuthorizationValidator.class);
        //Set validation chain
        dataValidator.setNext(userAuthorizationValidator);
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(vendorId);
        request.setUserType(UserType.VENDOR);
        request.setOrderUUID(orderId);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
        }

        try {
            Order order = orderService.findById(orderId);

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /vendor/{vendorId}/orders : Get orders for a vendor
     * A vendor can ask for all of their orders.
     * They only receive orders that have been paid for, so the ones with status \&quot;accepted\&quot;.
     *
     * @param vendorId (required)
     * @return List of orders for the vendor (status code 200)
     *     or Bad Request - Invalid request parameters. (status code 400)
     *     or Unauthorized - User is not a vendor (status code 401)
     *     or Vendor not found (status code 404)
     *     or Internal Server Error - An unexpected error occurred on the server. (status code 500)
     */
    @Override
    public ResponseEntity<List<Order>> getVendorOrders(UUID vendorId) {
        //Chain of responsibility validation
        //Get Validators
        DataValidator dataValidator = applicationContext.getBean(DataValidator.class,
                List.of(DataValidationField.USER));
        UserAuthorizationValidator userAuthorizationValidator = applicationContext.getBean(UserAuthorizationValidator.class);
        //Set validation chain
        dataValidator.setNext(userAuthorizationValidator);
        //Create and fill validation request
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(vendorId);
        request.setUserType(UserType.VENDOR);
        try {
            dataValidator.handle(request);
        } catch (ValidationFailureException e) {
            return ResponseEntity.status(e.getFailureStatus()).build();
        }

        try {
            List<Order> orders = vendorService.getVendorOrders(vendorId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
