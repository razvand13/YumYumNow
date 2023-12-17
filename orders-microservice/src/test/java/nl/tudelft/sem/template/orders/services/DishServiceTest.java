package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import nl.tudelft.sem.template.orders.repositories.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

public class DishServiceTest {

    @Mock
    private DishRepository dishRepository;
    @Mock
    private VendorRepository vendorRepository;
    @InjectMocks
    private DishService dishService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindByIdValid() {
        UUID dishId = UUID.randomUUID();
        DishEntity dish = new DishEntity();
        dish.setID(dishId);
        when(dishRepository.findById(dishId)).thenReturn(Optional.of(dish));

        DishEntity result = dishService.findById(dishId);

        assertThat(result).isNotNull();
        assertThat(result.getID()).isEqualTo(dishId);
    }

    @Test
    public void testFindByIdInvalid() {
        UUID nonExistentDishId = UUID.randomUUID();

        when(dishRepository.findById(nonExistentDishId)).thenReturn(Optional.empty()); // Unsuccessful query
        DishEntity dish = dishService.findById(nonExistentDishId);

        assertThat(dish).isNull();
    }

    @Test
    public void testFindAllByVendorIdValid() {
        UUID vendorId = UUID.randomUUID();
        DishEntity dish = new DishEntity();
        dish.setID(UUID.randomUUID());
        when(dishRepository.getDishesByVendorId(vendorId)).thenReturn(Collections.singletonList(dish));

        Iterable<DishEntity> result = dishService.findAllByVendorId(vendorId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getID()).isEqualTo(dish.getID());
    }

    @Test
    public void testFindAllByVendorIdInvalid() {
        UUID nonExistentVendorId = UUID.randomUUID();
        UUID existentVendorId = UUID.randomUUID();
        DishEntity dish = new DishEntity();
        when(dishRepository.getDishesByVendorId(existentVendorId)).thenReturn(Collections.singletonList(dish));

        Iterable<DishEntity> result = dishService.findAllByVendorId(nonExistentVendorId);

        assertThat(result).isEmpty();
    }
}
