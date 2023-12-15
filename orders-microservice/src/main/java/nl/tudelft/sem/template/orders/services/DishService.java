package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.orders.domain.IDishService;
import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.entities.Vendor;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import nl.tudelft.sem.template.orders.repositories.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DishService implements IDishService {
    private final DishRepository dishRepository;
    private final VendorRepository vendorRepository;
    //TODO remove circular dependency from schemas, so that we have loose coupling here
    @Autowired
    public DishService(DishRepository dishRepository, VendorRepository vendorRepository) {
        this.dishRepository = dishRepository;
        this.vendorRepository = vendorRepository;
    }

    public DishEntity addDish(UUID vendorId, DishEntity dish) {
        Vendor vendor = vendorRepository.findById(vendorId).orElseThrow(IllegalArgumentException::new);
        dish.setVendor(vendor);
        vendorRepository.save(vendor); // <- should be removed
        return dishRepository.save(dish);
    }
}
