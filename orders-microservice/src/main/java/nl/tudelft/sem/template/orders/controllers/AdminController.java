package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.api.AdminApi;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.entities.Order;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class AdminController implements AdminApi {


    private final transient IOrderService orderService;
    private final transient VendorAdapter vendorAdapter;

    @Autowired
    public AdminController(IOrderService orderService, VendorAdapter vendorAdapter) {
        this.orderService = orderService;
        this.vendorAdapter = vendorAdapter;
    }
    @Override
    public ResponseEntity<List<Order>> adminGetAllOrders(UUID adminId) {
        if (adminId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        //verify that user is admin
        if (!vendorAdapter.checkRoleById(adminId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // verify that admin exists
        if (!vendorAdapter.existsById(adminId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            List<nl.tudelft.sem.template.orders.entities.Order> orders = orderService.findAll();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
