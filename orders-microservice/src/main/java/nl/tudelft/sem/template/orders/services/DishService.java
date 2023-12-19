package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.orders.VendorNotFoundException;
import nl.tudelft.sem.template.orders.domain.IDishService;
import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.entities.Vendor;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import nl.tudelft.sem.template.orders.repositories.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DishService implements IDishService {
    private final transient DishRepository dishRepository;
    private final transient VendorRepository vendorRepository;

    //TODO remove circular dependency from schemas, so that we have loose coupling here

    @Autowired
    public DishService(DishRepository dishRepository, VendorRepository vendorRepository) {
        this.dishRepository = dishRepository;
        this.vendorRepository = vendorRepository;
    }

    /**
     * Find dish by ID
     *
     * @param dishId id of the dish
     * @return dish
     * @throws IllegalArgumentException if there is no dish with the specified ID
     */
    public DishEntity findById(UUID dishId) {
        return dishRepository.findById(dishId).orElse(null);
    }

    /**
     * Find all dishes that belong to a specified vendor
     *
     * @param vendorId id of the vendor
     * @return the vendor's dishes
     */
    public List<DishEntity> findAllByVendorId(UUID vendorId) {
        return dishRepository.getDishesByVendorId(vendorId);
    }

    /**
     * Adds a dish to the menu of a vendor.
     *
     * @param vendorId the id of the vendor
     * @param dish the dish to be added
     * @return the added dish
     */
    public DishEntity addDish(UUID vendorId, DishEntity dish) throws IllegalArgumentException {
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new VendorNotFoundException("Vendor with ID " + vendorId + " not found"));
        dish.setVendor(vendor);
        vendorRepository.save(vendor); // <- should be removed
        return dishRepository.save(dish);
    }


    /**
     * Checks if a dish is in an order
     * @param dishEntityList
     * @param dishId
     * @return
     */
    public boolean isDishInOrder(List<DishEntity> dishEntityList, UUID dishId){
        return dishEntityList.stream().anyMatch(dish -> dish.getID().equals(dishId));
    }


    /**
     * removes a dish from an order
     * @param dishEntityList
     * @param dishId
     * @return
     */
    public List<DishEntity> removeDishOrder(List<DishEntity> dishEntityList, UUID dishId){
        return dishEntityList.stream()
                .filter(dish -> !dish.getID().equals(dishId))
                .collect(Collectors.toList());
    }
}

