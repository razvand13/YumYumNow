package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.entities.Vendor;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import nl.tudelft.sem.template.orders.repositories.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class DishServiceTest {

    private DishRepository dishRepository;
    private VendorRepository vendorRepository;
    private DishService dishService;

    @BeforeEach
    void setUp() {
        dishRepository = mock(DishRepository.class);
        vendorRepository = mock(VendorRepository.class);
        dishService = new DishService(dishRepository, vendorRepository);
    }

    @Test
    void addDishWhenVendorExists() {
        UUID vendorId = UUID.randomUUID();
        DishEntity dish = new DishEntity();
        Vendor vendor = new Vendor();
        vendor.setID(vendorId);

        when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));
        when(dishRepository.save(any(DishEntity.class))).thenReturn(dish);

        DishEntity savedDish = dishService.addDish(vendorId, dish);

        assertThat(savedDish).isEqualTo(dish);
        verify(vendorRepository).findById(vendorId);
        verify(dishRepository).save(dish); // <- should be removed later
    }

    @Test
    void addDishWhenVendorDoesNotExist() {
        UUID vendorId = UUID.randomUUID();
        DishEntity dish = new DishEntity();

        when(vendorRepository.findById(vendorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dishService.addDish(vendorId, dish))
            .isInstanceOf(IllegalArgumentException.class);

        verify(vendorRepository).findById(vendorId);
        verifyNoInteractions(dishRepository);
    }
}
