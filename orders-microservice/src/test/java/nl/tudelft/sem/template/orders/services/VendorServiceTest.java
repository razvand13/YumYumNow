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

import java.util.ArrayList;
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

    private Address vendorLocation;
    private Address customerLocation;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        vendorLocation = new Address();
        customerLocation = new Address();
    }

    @Test
    void testIsInRangeWithinRangeCenter() {
        vendorLocation.setLatitude(40.0);
        vendorLocation.setLongitude(45.0);

        customerLocation.setLatitude(40.0);
        customerLocation.setLongitude(45.0);

        boolean inRange = vendorService.isInRange(vendorLocation, customerLocation);

        assertThat(inRange).isTrue();
    }

    @Test
    void testIsInRangeWithinRangeInside() {
        vendorLocation.setLatitude(40.0);
        vendorLocation.setLongitude(40.0);

        customerLocation.setLatitude(43.5);
        customerLocation.setLongitude(42.5);

        boolean inRange = vendorService.isInRange(vendorLocation, customerLocation);

        assertThat(inRange).isTrue();
    }

    @Test
    void testIsInRangeWithinRangeEdge() {
        vendorLocation.setLatitude(40.0);
        vendorLocation.setLongitude(40.0);

        customerLocation.setLatitude(44.0);
        customerLocation.setLongitude(43.0);

        boolean inRange = vendorService.isInRange(vendorLocation, customerLocation);

        assertThat(inRange).isTrue();
    }

    @Test
    void testIsInRangeOutOfRange() {
        vendorLocation.setLatitude(40.0);
        vendorLocation.setLongitude(40.0);

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

    @Test
    void testGetAveragePriceNoDishes() {
        VendorDTO vendor = new VendorDTO();
        vendor.setVendorId(UUID.randomUUID());

        Double averagePrice = vendorService.getAveragePrice(vendor);

        assertThat(averagePrice).isEqualTo(0.0);
    }

    @Test
    void testFilterVendors() {
        customerLocation.setLongitude(52.0);
        customerLocation.setLatitude(4.0);

        Address vendorLocation1 = new Address();
        vendorLocation1.setLongitude(53.0);
        vendorLocation1.setLatitude(5.0);
        Address vendorLocation2 = new Address();
        vendorLocation2.setLongitude(52.0);
        vendorLocation2.setLatitude(4.0);

        VendorDTO vendor1 = new VendorDTO(UUID.randomUUID(), "Vendor 1", false, "vendor1@example.com", true, vendorLocation1);
        VendorDTO vendor2 = new VendorDTO(UUID.randomUUID(), "Rodnev 2", false, "vendor2@example.com", true, vendorLocation2);

        List<VendorDTO> vendors = List.of(vendor1, vendor2);

        List<VendorDTO> filteredVendors = vendorService.filterVendors(vendors, "Ven", 0, 100, customerLocation);

        assertThat(filteredVendors).containsExactlyInAnyOrder(vendor1);
    }

    @Test
    void testFilterVendorsByAveragePrice() {
        vendorLocation.setLongitude(52.0);
        vendorLocation.setLatitude(4.0);

        VendorDTO vendor = new VendorDTO(UUID.randomUUID(), "Vendor", false, "vendor@example.com", true, vendorLocation);


        DishEntity dish1 = new DishEntity();
        dish1.setPrice(10.0);
        DishEntity dish2 = new DishEntity();
        dish2.setPrice(20.0);
        DishEntity dish3 = new DishEntity();
        dish3.setPrice(30.0);

        List<DishEntity> dishes = Arrays.asList(dish1, dish2, dish3);

        when(dishRepository.getDishesByVendorId(vendor.getVendorId())).thenReturn(dishes);

        List<VendorDTO> vendors = List.of(vendor);

        List<VendorDTO> filteredVendors = vendorService.filterVendors(vendors, "Ven", 20, 20, vendorLocation);

        assertThat(filteredVendors).containsExactlyInAnyOrder(vendor);
    }
}
