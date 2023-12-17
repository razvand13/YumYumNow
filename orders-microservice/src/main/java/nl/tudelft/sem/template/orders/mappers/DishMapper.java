package nl.tudelft.sem.template.orders.mappers;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.entities.Vendor;
import nl.tudelft.sem.template.orders.repositories.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class DishMapper {

    private final transient VendorRepository vendorRepository;

    @Autowired
    public DishMapper(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    /**
     * Maps a dish DTO to a dish entity.
     *
     * @param dishDTO the dish DTO
     * @return the dish entity
     */
    public DishEntity toEntity(Dish dishDTO) {
        DishEntity dish = new DishEntity();
        dish.setID(dishDTO.getID());
        dish.setName(dishDTO.getName());
        dish.setImageLink(dishDTO.getImageLink());
        dish.setPrice(dishDTO.getPrice());
        dish.setAllergens(dishDTO.getAllergens());
        dish.setIngredients(dishDTO.getIngredients());
        dish.setDescription(dishDTO.getDescription());

        Vendor vendor = vendorRepository.findById(dishDTO.getVendorId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid vendor ID"));
        dish.setVendor(vendor);

        return dish;
    }

    /**
     * Maps a dish entity to a dish DTO.
     *
     * @param dish the dish entity
     * @return the dish DTO
     */
    public Dish toDTO(DishEntity dish) {
        Dish dishDTO = new Dish();
        dishDTO.setID(dish.getID());
        dishDTO.setName(dish.getName());
        dishDTO.setImageLink(dish.getImageLink());
        dishDTO.setPrice(dish.getPrice());
        dishDTO.setAllergens(new ArrayList<>(dish.getAllergens()));
        dishDTO.setIngredients(new ArrayList<>(dish.getIngredients()));
        dishDTO.setDescription(dish.getDescription());

        if (dish.getVendor() != null) {
            dishDTO.setVendorId(dish.getVendor().getID());
        }

        return dishDTO;
    }
}