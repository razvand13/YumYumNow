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
     * @return the dish
     */
    public Dish findByIdNotDeleted(UUID dishId) {
        return dishRepository.findById(dishId)
            .filter(dish -> !dish.getIsDeleted())
            .orElse(null);
    }

    /**
     * Find all dishes that belong to a specified vendor
     *
     * @param vendorId id of the vendor
     * @return the vendor's dishes
     */
    public List<Dish> findAllByVendorIdNotDeleted(UUID vendorId) {
        return dishRepository.getDishesByVendorId(vendorId)
            .stream()
            .filter(dish -> !dish.getIsDeleted())
            .collect(Collectors.toList());
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
     * Removes a dish from the menu of a vendor.
     *
     * @param vendorId the id of the vendor
     * @param dishId the id of the dish
     * @return true if the dish was removed, false otherwise
     */
    @Override
    public boolean removeDish(UUID vendorId, UUID dishId) {
        Dish dish = dishRepository.findById(dishId).orElse(null);

        if (dish != null && dish.getVendorId().equals(vendorId) && !dish.getIsDeleted()) {
            dish.setIsDeleted(true);
            dishRepository.save(dish);
            return true;
        }

        return false;
    }

    @Override
    public Dish updateDish(UUID dishId, Dish updatedDish) {
        Dish existingDish = dishRepository.findById(dishId)
            .filter(dish -> !dish.getIsDeleted())
            .orElse(null);

        if (existingDish == null) {
            return null;
        }

        existingDish.setName(updatedDish.getName());
        existingDish.setIngredients(updatedDish.getIngredients());
        existingDish.setDescription(updatedDish.getDescription());
        existingDish.setImageLink(updatedDish.getImageLink());
        existingDish.setPrice(updatedDish.getPrice());
        existingDish.setAllergens(updatedDish.getAllergens());

        return dishRepository.save(existingDish);
    }

    /**
     * Checks if a dish is in an order
     *
     * @param dishEntityList List of dishes to check
     * @param dishId Id of dish to look for
     * @return true iff dish with id dishId is in the list
     */
    public boolean isDishInOrder(List<Dish> dishEntityList, UUID dishId) {
        return dishEntityList.stream().anyMatch(dish -> dish.getID().equals(dishId));
    }


    /**
     * removes a dish from an order
     *
     * @param dishEntityList List of dishes to modify
     * @param dishId id of dish to remove
     * @return modified dish list
     */
    public List<Dish> removeDishOrder(List<Dish> dishEntityList, UUID dishId) {
        return dishEntityList.stream()
                .filter(dish -> !dish.getID().equals(dishId))
                .collect(Collectors.toList());
    }
}

