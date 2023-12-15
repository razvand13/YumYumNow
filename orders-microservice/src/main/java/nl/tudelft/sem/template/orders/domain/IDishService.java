package nl.tudelft.sem.template.orders.domain;

import java.util.UUID;
import nl.tudelft.sem.template.orders.entities.DishEntity;

public interface IDishService {
    DishEntity addDish(UUID vendorId, DishEntity dish);
}
