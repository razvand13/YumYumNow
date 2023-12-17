package nl.tudelft.sem.template.orders.mappers;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.entities.Vendor;
import nl.tudelft.sem.template.orders.repositories.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DishMapperTest {

    private VendorRepository vendorRepository;
    private DishMapper dishMapper;

    @BeforeEach
    void setUp() {
        vendorRepository = mock(VendorRepository.class);
        dishMapper = new DishMapper(vendorRepository);
    }

    @Test
    void toEntityValidConversion() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();
        dish.setID(UUID.randomUUID());
        dish.setName("Pizza");
        dish.setImageLink("Link to yummy pizza");
        dish.setPrice(1.99);
        dish.setAllergens(List.of("Pizza", "Cheese"));
        dish.setIngredients(List.of("Flour", "Tomato", "Mozzarella"));
        dish.setDescription("Super simple pizza");
        dish.setVendorId(vendorId);

        Vendor vendor = new Vendor();
        vendor.setID(vendorId);

        when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));

        DishEntity dishEntity = dishMapper.toEntity(dish);

        assertThat(dishEntity.getID()).isEqualTo(dish.getID());
        assertThat(dishEntity.getName()).isEqualTo(dish.getName());
        assertThat(dishEntity.getImageLink()).isEqualTo(dish.getImageLink());
        assertThat(dishEntity.getPrice()).isEqualTo(dish.getPrice());
        assertThat(dishEntity.getAllergens()).isEqualTo(dish.getAllergens());
        assertThat(dishEntity.getIngredients()).isEqualTo(dish.getIngredients());
        assertThat(dishEntity.getDescription()).isEqualTo(dish.getDescription());
        assertThat(dishEntity.getVendor()).isEqualTo(vendor);
    }

    @Test
    void toDTOValidConversion() {
        DishEntity dishEntity = new DishEntity();
        dishEntity.setID(UUID.randomUUID());
        dishEntity.setName("Salad");
        dishEntity.setImageLink("Photo that makes customer feel healthy");
        dishEntity.setPrice(99.99);
        dishEntity.setAllergens(new ArrayList<>(List.of("Being Healthy")));
        dishEntity.setIngredients(new ArrayList<>(List.of("Lettuce", "Tomato", "Cucumber")));
        dishEntity.setDescription("Healthy salad");

        UUID vendorId = UUID.randomUUID();
        Vendor vendor = new Vendor();
        vendor.setID(vendorId);
        dishEntity.setVendor(vendor);

        Dish dish = dishMapper.toDTO(dishEntity);

        assertThat(dish.getID()).isEqualTo(dishEntity.getID());
        assertThat(dish.getName()).isEqualTo(dishEntity.getName());
        assertThat(dish.getImageLink()).isEqualTo(dishEntity.getImageLink());
        assertThat(dish.getPrice()).isEqualTo(dishEntity.getPrice());
        assertThat(dish.getAllergens()).isEqualTo(dishEntity.getAllergens());
        assertThat(dish.getIngredients()).isEqualTo(dishEntity.getIngredients());
        assertThat(dish.getDescription()).isEqualTo(dishEntity.getDescription());
        assertThat(dish.getVendorId()).isEqualTo(vendorId);
    }

    @Test
    void toEntityInvalidVendorId() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();
        dish.setVendorId(vendorId);

        when(vendorRepository.findById(vendorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dishMapper.toEntity(dish))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid vendor ID");

        verify(vendorRepository).findById(vendorId);
    }

    @Test
    void toDTOWithNullVendor() {
        DishEntity dishEntity = new DishEntity();
        dishEntity.setID(UUID.randomUUID());
        dishEntity.setName("Salad");
        dishEntity.setImageLink("Photo that makes customer feel healthy");
        dishEntity.setPrice(99.99);
        dishEntity.setAllergens(new ArrayList<>(List.of("Being Healthy")));
        dishEntity.setIngredients(new ArrayList<>(List.of("Lettuce", "Tomato", "Cucumber")));
        dishEntity.setDescription("Healthy salad");
        dishEntity.setVendor(null);

        Dish dish = dishMapper.toDTO(dishEntity);

        assertThat(dish.getID()).isEqualTo(dishEntity.getID());
        assertThat(dish.getName()).isEqualTo(dishEntity.getName());
        assertThat(dish.getImageLink()).isEqualTo(dishEntity.getImageLink());
        assertThat(dish.getPrice()).isEqualTo(dishEntity.getPrice());
        assertThat(dish.getAllergens()).isEqualTo(dishEntity.getAllergens());
        assertThat(dish.getIngredients()).isEqualTo(dishEntity.getIngredients());
        assertThat(dish.getDescription()).isEqualTo(dishEntity.getDescription());
        assertThat(dish.getVendorId()).isNull();
    }

}

