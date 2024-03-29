package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.UUID;
import java.util.Optional;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
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

        Dish result = dishService.findByIdNotDeleted(dishId);

        assertThat(result).isNotNull();
        assertThat(result.getID()).isEqualTo(dishId);
    }

    @Test
    public void testFindByIdInvalid() {
        UUID nonExistentDishId = UUID.randomUUID();

        when(dishRepository.findById(nonExistentDishId)).thenReturn(Optional.empty()); // Unsuccessful query
        Dish dish = dishService.findByIdNotDeleted(nonExistentDishId);

        assertThat(dish).isNull();
    }

    @Test
    public void testFindAllByVendorIdValid() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();
        dish.setID(UUID.randomUUID());
        when(dishRepository.getDishesByVendorId(vendorId)).thenReturn(Collections.singletonList(dish));

        Iterable<Dish> result = dishService.findAllByVendorIdNotDeleted(vendorId);

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

        Iterable<Dish> result = dishService.findAllByVendorIdNotDeleted(nonExistentVendorId);

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
    void removeDishWhenDishNotFound() {
        UUID vendorId = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();

        when(dishRepository.findById(dishId)).thenReturn(Optional.empty());

        boolean result = dishService.removeDish(vendorId, dishId);

        assertThat(result).isFalse();
        verify(dishRepository).findById(dishId);
    }

    @Test
    void removeDishWhenDishBelongsToAnotherVendor() {

        UUID anotherVendorId = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();
        Dish dish = new Dish();
        dish.setID(dishId);
        dish.setVendorId(anotherVendorId);

        when(dishRepository.findById(dishId)).thenReturn(Optional.of(dish));

        UUID vendorId = UUID.randomUUID();
        boolean result = dishService.removeDish(vendorId, dishId);

        assertThat(result).isFalse();
        verify(dishRepository).findById(dishId);
        verify(dishRepository, never()).delete(any(Dish.class));
    }

    @Test
    void removeDishSuccessful() {
        UUID vendorId = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();
        Dish dish = new Dish();
        dish.setID(dishId);
        dish.setVendorId(vendorId);

        when(dishRepository.findById(dishId)).thenReturn(Optional.of(dish));

        boolean result = dishService.removeDish(vendorId, dishId);

        assertThat(result).isTrue();
        verify(dishRepository).findById(dishId);
        verify(dishRepository).save(dish);
        assertThat(dish.getIsDeleted()).isTrue();
    }

    @Test
    void updateDishNotFound() {
        UUID dishId = UUID.randomUUID();
        Dish updatedDish = new Dish();

        when(dishRepository.findById(dishId)).thenReturn(Optional.empty());

        Dish result = dishService.updateDish(dishId, updatedDish);

        assertThat(result).isNull();
        verify(dishRepository).findById(dishId);
        verify(dishRepository, never()).save(any(Dish.class));
    }

    @Test
    void updateDishAlreadyDeleted() {
        UUID dishId = UUID.randomUUID();
        Dish existingDish = new Dish();
        existingDish.setID(dishId);
        existingDish.setIsDeleted(true);

        Dish updatedDish = new Dish();

        when(dishRepository.findById(dishId)).thenReturn(Optional.of(existingDish));

        Dish result = dishService.updateDish(dishId, updatedDish);

        assertThat(result).isNull();
        verify(dishRepository).findById(dishId);
        verify(dishRepository, never()).save(any(Dish.class));
    }

    @Test
    void updateDishSuccessful() {
        UUID dishId = UUID.randomUUID();
        Dish existingDish = new Dish();
        existingDish.setID(dishId);
        existingDish.setIsDeleted(false);

        Dish updatedDish = new Dish();
        updatedDish.setName("Updated Name");

        when(dishRepository.findById(dishId)).thenReturn(Optional.of(existingDish));
        when(dishRepository.save(any(Dish.class))).thenReturn(updatedDish);

        Dish result = dishService.updateDish(dishId, updatedDish);

        assertThat(result).isEqualTo(updatedDish);
        verify(dishRepository).findById(dishId);
        verify(dishRepository).save(any(Dish.class));
    }
    
    @Test
    void isDishNotInOrder() {
        UUID dishId = UUID.randomUUID();
        List<Dish> dishes = Arrays.asList(createDish(UUID.randomUUID()), createDish(UUID.randomUUID()));

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

    @Test
    void addDishVendorIdCorrectlySet() {
        Dish dish = createDish(UUID.randomUUID());
        UUID vendorId = UUID.randomUUID();

        when(dishRepository.save(any(Dish.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        Dish result = dishService.addDish(vendorId, dish);
        assertThat(result.getVendorId()).isEqualTo(vendorId);
    }

    @Test
    void checkAttributesUpdateDish() {
        UUID dishId = UUID.randomUUID();
        Dish updatedDish = createDish(dishId);
        updatedDish.setName("Dish Name");
        updatedDish.setDescription("Dish Description");
        updatedDish.setPrice(12.0);
        updatedDish.setImageLink("Some Image Link");
        updatedDish.setAllergens(Collections.singletonList("Gluten"));
        updatedDish.setIngredients(Collections.singletonList("Milk"));

        when(dishRepository.findById(dishId)).thenReturn(Optional.of(createDish(dishId)));

        when(dishRepository.save(any(Dish.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        Dish result = dishService.updateDish(dishId, updatedDish);
        assertThat(result.getID()).isEqualTo(dishId);
        assertThat(result.getName()).isEqualTo("Dish Name");
        assertThat(result.getImageLink()).isEqualTo("Some Image Link");
        assertThat(result.getDescription()).isEqualTo("Dish Description");
        assertThat(result.getPrice()).isEqualTo(12.0);
        assertThat(result.getAllergens()).isEqualTo(Collections.singletonList("Gluten"));
        assertThat(result.getIngredients()).isEqualTo(Collections.singletonList("Milk"));
    }

    @Test
    void findByIdDeletedDish() {
        UUID dishId = UUID.randomUUID();
        Dish dish = createDish(dishId);
        dish.setIsDeleted(true);

        when(dishRepository.findById(dishId)).thenReturn(Optional.of(dish));

        Dish result = dishService.findByIdNotDeleted(dishId);
        assertThat(result).isNull();
    }

    @Test
    void findAllByVendorIdDeletedDish() {
        UUID dishId = UUID.randomUUID();
        Dish dish = createDish(dishId);
        dish.setIsDeleted(true);
        UUID anotherDishId = UUID.randomUUID();
        Dish anotherDish = createDish(anotherDishId);
        List<Dish> dishes = new ArrayList<>();
        dishes.add(dish);
        dishes.add(anotherDish);

        when(dishRepository.getDishesByVendorId(any(UUID.class))).thenReturn(dishes);

        List<Dish> result = dishService.findAllByVendorIdNotDeleted(UUID.randomUUID());
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(Collections.singletonList(createDish(anotherDishId)));
    }

    @Test
    void isDishInOrderReturnCheck() {
        UUID dishId = UUID.randomUUID();
        List<Dish> dishes = Arrays.asList(createDish(dishId), createDish(UUID.randomUUID()));

        boolean result = dishService.isDishInOrder(dishes, dishId);

        assertThat(result).isTrue();
    }

    private Dish createDish(UUID dishId) {
        Dish dish = new Dish();
        dish.setID(dishId);
        return dish;
    }
}
