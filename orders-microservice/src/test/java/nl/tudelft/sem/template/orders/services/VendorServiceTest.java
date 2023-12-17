package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.orders.entities.Address;
import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class VendorServiceTest {

    @Mock
    private DishRepository dishRepository;

    @InjectMocks
    private VendorService vendorService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsInRangeWithinRangeCenter() {
        Address vendorLocation = new Address();
        vendorLocation.setLatitude(40.0);
        vendorLocation.setLongitude(45.0);
        Address customerLocation = new Address();
        customerLocation.setLatitude(40.0);
        customerLocation.setLongitude(45.0);

        boolean inRange = vendorService.isInRange(vendorLocation, customerLocation);

        assertThat(inRange).isTrue();
    }

    @Test
    void testIsInRangeWithinRangeInside() {
        Address vendorLocation = new Address();
        vendorLocation.setLatitude(40.0);
        vendorLocation.setLongitude(40.0);
        Address customerLocation = new Address();
        customerLocation.setLatitude(43.5);
        customerLocation.setLongitude(42.5);

        boolean inRange = vendorService.isInRange(vendorLocation, customerLocation);

        assertThat(inRange).isTrue();
    }

    @Test
    void testIsInRangeWithinRangeEdge() {
        Address vendorLocation = new Address();
        vendorLocation.setLatitude(40.0);
        vendorLocation.setLongitude(40.0);
        Address customerLocation = new Address();
        customerLocation.setLatitude(44.0);
        customerLocation.setLongitude(43.0);

        boolean inRange = vendorService.isInRange(vendorLocation, customerLocation);

        assertThat(inRange).isTrue();
    }

    @Test
    void testIsInRangeOutOfRange() {
        Address vendorLocation = new Address();
        vendorLocation.setLatitude(40.0);
        vendorLocation.setLongitude(40.0);
        Address customerLocation = new Address();
        customerLocation.setLatitude(45.0);
        customerLocation.setLongitude(44.0);

        boolean inRange = vendorService.isInRange(vendorLocation, customerLocation);

        assertThat(inRange).isFalse();
    }

    @Test
    void testGetAveragePrice() {
        VendorDTO vendor = new VendorDTO();
        vendor.setVendorId(UUID.randomUUID());

        DishEntity dish1 = new DishEntity();
        dish1.setPrice(10.0);
        DishEntity dish2 = new DishEntity();
        dish2.setPrice(20.0);
        DishEntity dish3 = new DishEntity();
        dish3.setPrice(30.0);

        List<DishEntity> dishes = Arrays.asList(dish1, dish2, dish3);

        when(dishRepository.getDishesByVendorId(vendor.getVendorId())).thenReturn(dishes);

        Double averagePrice = vendorService.getAveragePrice(vendor);

        assertThat(averagePrice).isEqualTo(20.0);
    }
}