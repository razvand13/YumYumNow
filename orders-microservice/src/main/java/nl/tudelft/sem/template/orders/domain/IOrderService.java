package nl.tudelft.sem.template.orders.domain;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.OrderedDish;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IOrderService {
    Order findById(UUID orderId);

    List<Order> findAll();

    Order save(Order order);

    void delete(UUID orderId);
    
    public double calculateOrderPrice(List<OrderedDish> orderedDishes);

    Optional<OrderedDish> orderedDishInOrder(Order order, UUID dishId);

    List<Order> findOrdersByCustomerId(UUID customerId);

    List<Dish> getDishesForCustomer(UUID vendorId, UUID customerId);
}
