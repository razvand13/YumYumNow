package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.api.VendorApi;
import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.mappers.DishMapper;
import nl.tudelft.sem.template.orders.services.DishService;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.UUID;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class VendorController implements VendorApi {

    private final transient VendorAdapter vendorAdapter;
    private final transient DishService dishService;
    private final transient DishMapper dishMapper;

    /**
     * Creates an instance of the VendorController.
     *
     * @param vendorAdapter the vendor adapter
     * @param dishService the dish service
     * @param dishMapper the dish mapper
     */
    @Autowired
    public VendorController(VendorAdapter vendorAdapter, DishService dishService, DishMapper dishMapper) {
        this.vendorAdapter = vendorAdapter;
        this.dishService = dishService;
        this.dishMapper = dishMapper;
    }

    @Override
    public ResponseEntity<Dish> addDishToMenu(UUID vendorId, Dish dish) {
        if (!vendorAdapter.checkRoleById(vendorId)) {
            // Unauthorized - ID of a customer/courier/admin
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!vendorAdapter.existsById(vendorId)) {
            // Vendor id not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            DishEntity dishEntity = dishMapper.toEntity(dish);
            DishEntity addedDish = dishService.addDish(vendorId, dishEntity);
            Dish addedDishDTO = dishMapper.toDTO(addedDish);
            return ResponseEntity.ok(addedDishDTO);
        } catch (IllegalArgumentException e) {
            // Bad request from service
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
