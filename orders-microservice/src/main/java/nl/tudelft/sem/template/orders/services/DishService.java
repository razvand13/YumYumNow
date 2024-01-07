package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.orders.domain.IDishService;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DishService implements IDishService {
    private final transient DishRepository dishRepository;

    @Autowired
    public DishService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    /**
     * Find dish by ID
     *
     * @param dishId id of the dish
     * @return dish
     * @throws IllegalArgumentException if there is no dish with the specified ID
     */
    public Dish findById(UUID dishId) {
        return dishRepository.findById(dishId).orElse(null);
    }

    /**
     * Find all dishes that belong to a specified vendor
     *
     * @param vendorId id of the vendor
     * @return the vendor's dishes
     */
    public List<Dish> findAllByVendorId(UUID vendorId) {
        return dishRepository.getDishesByVendorId(vendorId);
    }

    /**
     * Adds a dish to the menu of a vendor.
     *
     * @param vendorId the id of the vendor
     * @param dish the dish to be added
     * @return the added dish
     */
    public Dish addDish(UUID vendorId, Dish dish) throws IllegalArgumentException {
        dish.setVendorId(vendorId);
        return dishRepository.save(dish);
    }


    /**
     * Checks if a dish is in an order
     * @param dishEntityList
     * @param dishId
     * @return
     */
    public boolean isDishInOrder(List<Dish> dishEntityList, UUID dishId){
        return dishEntityList.stream().anyMatch(dish -> dish.getID().equals(dishId));
    }


    /**
     * removes a dish from an order
     * @param dishEntityList
     * @param dishId
     * @return
     */
    public List<Dish> removeDishOrder(List<Dish> dishEntityList, UUID dishId){
        return dishEntityList.stream()
                .filter(dish -> !dish.getID().equals(dishId))
                .collect(Collectors.toList());
    }
}

