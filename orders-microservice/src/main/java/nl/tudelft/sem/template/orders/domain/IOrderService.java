package nl.tudelft.sem.template.orders.domain;

import nl.tudelft.sem.template.orders.entities.Order;

import java.util.UUID;

public interface IOrderService {
    Order findById(UUID orderId);

    Order save(Order order);
}
