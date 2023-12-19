package nl.tudelft.sem.template.orders.controllers;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.orders.VendorNotFoundException;
import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.mappers.DishMapper;
import nl.tudelft.sem.template.orders.services.DishService;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class VendorControllerTest {

    private VendorAdapter vendorAdapter;
    private DishService dishService;
    private DishMapper dishMapper;
    private VendorController vendorController;

    @BeforeEach
    void setUp() {
        vendorAdapter = mock(VendorAdapter.class);
        dishService = mock(DishService.class);
        dishMapper = mock(DishMapper.class);
        vendorController = new VendorController(vendorAdapter, dishService, dishMapper);
    }

    @Test
    void addDishToMenuWhenVendorIdIsNull() {
        Dish dish = new Dish();

        ResponseEntity<Dish> response = vendorController.addDishToMenu(null, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        verifyNoInteractions(vendorAdapter);
        verifyNoInteractions(dishService);
        verifyNoInteractions(dishMapper);
    }

    @Test
    void addDishToMenuWhenVendorRoleIsInvalid() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(false);

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(vendorAdapter).checkRoleById(vendorId);
        verifyNoInteractions(dishService);
        verifyNoInteractions(dishMapper);
    }

    @Test
    void addDishToMenuWhenVendorNotFound() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(false);

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(vendorAdapter).checkRoleById(vendorId);
        verify(vendorAdapter).existsById(vendorId);
        verifyNoInteractions(dishService);
        verifyNoInteractions(dishMapper);
    }

    @Test
    void addDishToMenuSuccessful() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();
        DishEntity dishEntity = new DishEntity();
        DishEntity addedDishEntity = new DishEntity();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(dishMapper.toEntity(dish)).thenReturn(dishEntity);
        when(dishService.addDish(vendorId, dishEntity)).thenReturn(addedDishEntity);
        when(dishMapper.toDTO(addedDishEntity)).thenReturn(dish);

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dish);
        verify(vendorAdapter).checkRoleById(vendorId);
        verify(vendorAdapter).existsById(vendorId);
        verify(dishMapper).toEntity(dish);
        verify(dishService).addDish(vendorId, dishEntity);
        verify(dishMapper).toDTO(addedDishEntity);
    }

    @Test
    void addDishToMenuVendorNotFound() {
        UUID nonExistentVendorId = UUID.randomUUID();
        Dish dish = new Dish();
        DishEntity dishEntity = new DishEntity();

        when(vendorAdapter.checkRoleById(any(UUID.class))).thenReturn(true);
        when(vendorAdapter.existsById(any(UUID.class))).thenReturn(true);
        when(dishMapper.toEntity(any(Dish.class))).thenReturn(dishEntity);
        when(dishService.addDish(any(UUID.class), any(DishEntity.class)))
            .thenThrow(new VendorNotFoundException());

        ResponseEntity<Dish> response = vendorController.addDishToMenu(nonExistentVendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Verify interactions
        verify(vendorAdapter).checkRoleById(nonExistentVendorId);
        verify(vendorAdapter).existsById(nonExistentVendorId);
        verify(dishMapper).toEntity(dish);
        verify(dishService).addDish(nonExistentVendorId, dishEntity);
    }

    @Test
    void addDishToMenuBadRequest() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();
        DishEntity dishEntity = new DishEntity();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(dishMapper.toEntity(dish)).thenReturn(dishEntity);
        when(dishService.addDish(vendorId, dishEntity)).thenThrow(new IllegalArgumentException());

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(dishService).addDish(vendorId, dishEntity);
    }

    @Test
    void addDishToMenuInternalServerError() {
        UUID vendorId = UUID.randomUUID();
        Dish dish = new Dish();

        when(vendorAdapter.checkRoleById(vendorId)).thenReturn(true);
        when(vendorAdapter.existsById(vendorId)).thenReturn(true);
        when(dishMapper.toEntity(dish)).thenThrow(new RuntimeException());

        ResponseEntity<Dish> response = vendorController.addDishToMenu(vendorId, dish);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(dishMapper).toEntity(dish);
    }
}
