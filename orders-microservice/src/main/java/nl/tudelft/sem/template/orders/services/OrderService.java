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

    /**
     * Calculates total price of an order
     *
     * @param orderedDishes list of dishes to use to calculate price
     * @return calculated price
     */
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

    /**
     * Returns OrderedDish corresponding to provided dish ID
     *
     * @param order Order to check for OrderedDish
     * @param dishId id of Dish
     * @return OrderedDish with dish having provided id, or empty
     */
    public Optional<OrderedDish> orderedDishInOrder(Order order, UUID dishId) {
        return order.getDishes().stream()
                .filter(orderedDish -> orderedDish.getDish().getID().equals(dishId))
                .findFirst();
    }
}