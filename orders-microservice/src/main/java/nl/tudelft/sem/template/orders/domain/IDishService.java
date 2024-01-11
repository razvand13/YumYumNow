package nl.tudelft.sem.template.orders.domain;

import nl.tudelft.sem.template.model.Dish;

import java.util.List;
import java.util.UUID;

public interface IDishService {
    Dish findByIdNotDeleted(UUID dishId);

    List<Dish> findAllByVendorIdNotDeleted(UUID vendorId);

    Dish addDish(UUID vendorId, Dish dish);

    boolean removeDish(UUID vendorId, UUID dishId);

    boolean isDishInOrder(List<Dish> dishEntityList, UUID dishId);

    List<Dish> removeDishOrder(List<Dish> dishEntityList, UUID dishId);

    Dish updateDish(UUID dishId, Dish dish);
}
