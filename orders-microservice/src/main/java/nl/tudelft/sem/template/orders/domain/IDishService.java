package nl.tudelft.sem.template.orders.domain;

import nl.tudelft.sem.template.orders.entities.DishEntity;

import java.util.List;
import java.util.UUID;

public interface IDishService {
    DishEntity findById(UUID dishId);

    List<DishEntity> findAllByVendorId(UUID vendorId);

    DishEntity addDish(UUID vendorId, DishEntity dish);

    boolean isDishInOrder(List<DishEntity> dishEntityList, UUID dishId);

    List<DishEntity> removeDishOrder(List<DishEntity> dishEntityList, UUID dishId);
}
