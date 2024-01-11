package nl.tudelft.sem.template.orders.domain;

import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.entities.Order;

import java.util.List;
import java.util.UUID;

public interface IOrderService {
    Order findById(UUID orderId);

    List<Order> findAll();

    Order save(Order order);

    void delete(UUID orderId);

    double calculateOrderPrice(List<DishEntity> dishEntityList);
}
