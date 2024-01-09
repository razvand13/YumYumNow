package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.OrderedDish;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService implements IOrderService {
    private final transient OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order findById(UUID orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public double calculateOrderPrice(List<OrderedDish> orderedDishes) {
        double totalPrice = 0.0;

        for (OrderedDish orderedDish : orderedDishes) {
            Dish dish = orderedDish.getDish();
            int quantity = orderedDish.getQuantity();
            double price = dish.getPrice();
            totalPrice += price * quantity;
        }

        return totalPrice;
    }

    public Optional<OrderedDish> orderedDishInOrder(Order order, UUID dishId){
        return order.getDishes().stream()
                .filter(orderedDish -> orderedDish.getDish().getID().equals(dishId))
                .findFirst();
    }
    public Optional<OrderedDish> removeDishOrder(List<OrderedDish> orderedDishes, UUID dishId) {
        return orderedDishes.stream()
                .filter(orderedDish -> orderedDish.getDish().getID().equals(dishId))
                .findFirst();
    }

    public List<Order> findOrdersByCustomerId(UUID customerId) {
        // This method fetches orders from the repository using the customerId
        return orderRepository.findByCustomerId(customerId);
    }
}