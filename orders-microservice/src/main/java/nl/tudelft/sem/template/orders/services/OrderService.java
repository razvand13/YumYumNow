package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.OrderedDish;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.external.CustomerDTO;
import nl.tudelft.sem.template.orders.integration.CustomerFacade;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService implements IOrderService {
    private final transient OrderRepository orderRepository;

    private final transient DishService dishService;
    private final transient CustomerFacade customerFacade;

    /**
     * constructor for OrderService
     *
     * @param orderRepository order repository
     * @param dishService dish service
     * @param customerFacade customer facade
     */
    @Autowired
    public OrderService(OrderRepository orderRepository, DishService dishService, CustomerFacade customerFacade) {
        this.orderRepository = orderRepository;
        this.dishService = dishService;
        this.customerFacade = customerFacade;
    }

    public Order findById(UUID orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public void delete(UUID orderId) {
        orderRepository.deleteById(orderId);
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

    /**
     *
     * @param orderedDishes ordered dishes
     * @param dishId id of dish to remove
     * @return OrderedDish without the dish that was removed
     */
    public Optional<OrderedDish> removeDishOrder(List<OrderedDish> orderedDishes, UUID dishId) {
        return orderedDishes.stream()
                .filter(orderedDish -> orderedDish.getDish().getID().equals(dishId))
                .findFirst();
    }

    /**
     *
     * @param customerId id of customer
     * @return finds the orders of a customer by its id
     */
    public List<Order> findOrdersByCustomerId(UUID customerId) {
        // This method fetches orders from the repository using the customerId
        return orderRepository.findByCustomerId(customerId);
    }

    /**
     * Filters dishes for a customer based on allergens.
     *
     * @param vendorId id of the vendor
     * @param customerId id of the customer
     * @return List of dishes after filtering based on allergens
     */
    public List<Dish> getDishesForCustomer(UUID vendorId, UUID customerId) {



        List<Dish> vendorDishes = dishService.findAllByVendorId(vendorId);
        CustomerDTO customer = customerFacade.requestCustomer(customerId);

        List<String> customerAllergens = new ArrayList<>();
        customerAllergens.addAll(customer.getAllergens());

        ArrayList<Dish> dishesToRemove = new ArrayList<>();


        for (int i = 0; i < vendorDishes.size(); i++) {
            List<String> dishAllergens = vendorDishes.get(i).getAllergens();
            if (dishAllergens != null) {
                for (String allergen : dishAllergens) {
                    if (customerAllergens.contains(allergen)) {
                        dishesToRemove.add(vendorDishes.get(i));
                        break;
                    }
                }
            }
        }
        List<Dish> filteredDishes = new ArrayList<>();
        for (Dish dish : dishesToRemove) {
            if (Collections.disjoint(dish.getAllergens(), customerAllergens)) {
                filteredDishes.add(dish);
            }
        }

        return filteredDishes;
    }


}