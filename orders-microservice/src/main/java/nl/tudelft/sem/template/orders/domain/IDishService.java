package nl.tudelft.sem.template.orders.domain;

import nl.tudelft.sem.template.orders.entities.DishEntity;

import java.util.List;
import java.util.UUID;

public interface IDishService {
    DishEntity findById(UUID dishId);

    List<DishEntity> findAllByVendorId(UUID vendorId);
}
