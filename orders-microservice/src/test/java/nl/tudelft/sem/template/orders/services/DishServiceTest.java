package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class DishServiceTest {

    @Mock
    private DishRepository dishRepository;

    @InjectMocks
    private DishService dishService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        dishService = new DishService(dishRepository);
    }

    @Test
    public void testFindByIdValid() {
        UUID dishId = UUID.randomUUID();
        Dish dish = new Dish();
        dish.setID(dishId);
        when(dishRepository.findById(dishId)).thenReturn(Optional.of(dish));

        Dish result = dishService.findById(dishId);

        assertThat(result).isNotNull();
        assertThat(result.getID()).isEqualTo(dishId);
    }

    @Test
    public void testFindByIdInvalid() {
        UUID nonExistentDishId = UUID.randomUUID();

        when(dishRepository.findById(nonExistentDishId)).thenReturn(Optional.empty()); // Unsuccessful query
        Dish dish = dishService.findById(nonExistentDishId);

        assertThat(dish).isNull();
    }

    @Test
    public void testFindAllByVendorIdValid() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();
        dish.setID(UUID.randomUUID());
        when(dishRepository.getDishesByVendorId(vendorId)).thenReturn(Collections.singletonList(dish));

        Iterable<Dish> result = dishService.findAllByVendorId(vendorId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getID()).isEqualTo(dish.getID());
    }

    @Test
    public void testFindAllByVendorIdInvalid() {
        UUID nonExistentVendorId = UUID.randomUUID();
        UUID existentVendorId = UUID.randomUUID();
        Dish dish = new Dish();
        when(dishRepository.getDishesByVendorId(existentVendorId)).thenReturn(Collections.singletonList(dish));

        Iterable<Dish> result = dishService.findAllByVendorId(nonExistentVendorId);

        assertThat(result).isEmpty();
    }

    @Test
    void addDish() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();

        when(dishRepository.save(any(Dish.class))).thenReturn(dish);

        Dish savedDish = dishService.addDish(vendorId, dish);

        assertThat(savedDish).isEqualTo(dish);
        verify(dishRepository).save(dish);
    }

    @Test
    void isDishNotInOrder() {
        UUID dishId = UUID.randomUUID();
        List<Dish> dishes = Arrays.asList(createDishWithDifferentId(), createDishWithDifferentId());

        boolean result = dishService.isDishInOrder(dishes, dishId);

        assertThat(result).isFalse();
    }

    @Test
    void removeDishFromOrder() {
        UUID dishIdToRemove = UUID.randomUUID();
        Dish dishToRemove = createDish(dishIdToRemove);
        Dish otherDish = createDish(UUID.randomUUID());

        List<Dish> dishes = Arrays.asList(dishToRemove, otherDish);

        List<Dish> result = dishService.removeDishOrder(dishes, dishIdToRemove);

        assertThat(result).doesNotContain(dishToRemove);
        assertThat(result).hasSize(1);
        assertThat(result).contains(otherDish);
    }

    @Test
    void removeDishNotInOrder() {
        UUID dishIdToRemove = UUID.randomUUID();
        Dish dish1 = createDish(UUID.randomUUID());
        Dish dish2 = createDish(UUID.randomUUID());

        List<Dish> dishes = Arrays.asList(dish1, dish2);

        List<Dish> result = dishService.removeDishOrder(dishes, dishIdToRemove);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(dish1, dish2);
    }

    private Dish createDish(UUID dishId) {
        Dish dish = new Dish();
        dish.setID(dishId);
        return dish;
    }

    private Dish createDishWithDifferentId() {
        Dish dish = new Dish();
        dish.setID(UUID.randomUUID());
        return dish;
    }

}
