package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.api.VendorApi;
import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.VendorNotFoundException;
import nl.tudelft.sem.template.orders.domain.IDishService;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.domain.IVendorService;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Creates an instance of the VendorController.
     *
     * @param vendorAdapter the vendor adapter
     * @param dishService   the dish service
     * @param vendorService the vendor service
     */
    @Autowired
    public VendorController(VendorAdapter vendorAdapter, IDishService dishService,
                            IOrderService orderService, IVendorService vendorService) {
        this.vendorAdapter = vendorAdapter;
        this.dishService = dishService;
        this.orderService = orderService;
        this.vendorService = vendorService;
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
        if (vendorId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (!vendorAdapter.checkRoleById(vendorId)) {
            // Unauthorized - ID of a customer/courier/admin
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!vendorAdapter.existsById(vendorId)) {
            // Vendor id not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
        if (vendorId == null || orderId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        //Verify user is not of wrong type
        if (!vendorAdapter.checkRoleById(vendorId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Order order = orderService.findById(orderId);

            //Verify order existence and user existence
            if (order == null || !vendorAdapter.existsById(vendorId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            //Verify order ownership
            if (!order.getVendorId().equals(vendorId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

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
        if (vendorId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        //Verify user type
        if (!vendorAdapter.checkRoleById(vendorId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //Verify existence of vendor
        if (!vendorAdapter.existsById(vendorId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            List<Order> orders = vendorService.getVendorOrders(vendorId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
