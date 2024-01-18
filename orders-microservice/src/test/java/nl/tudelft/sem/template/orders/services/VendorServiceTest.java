package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.model.Address;
import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Vendor;
import nl.tudelft.sem.template.orders.external.PaymentMock;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import nl.tudelft.sem.template.orders.integration.VendorFacade;
import nl.tudelft.sem.template.orders.mappers.VendorMapper;
import nl.tudelft.sem.template.orders.mappers.interfaces.IVendorMapper;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VendorServiceTest {

    private DishRepository dishRepository;

    private OrderRepository orderRepository;

    private PaymentMock paymentMock;

    private VendorService vendorService;

    private Address vendorLocation;
    private Address customerLocation;
    private VendorFacade vendorFacade;
    private VendorMapper vendorMapper;

    @BeforeEach
    void setup() {
        dishRepository = mock(DishRepository.class);
        orderRepository = mock(OrderRepository.class);
        vendorFacade = mock(VendorFacade.class);
        vendorMapper = mock(VendorMapper.class);
        paymentMock = new PaymentMock();
        vendorService = new VendorService(dishRepository, orderRepository, paymentMock, vendorFacade, vendorMapper);
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

        Dish dish1 = new Dish();
        dish1.setPrice(10.0);
        Dish dish2 = new Dish();
        dish2.setPrice(20.0);
        Dish dish3 = new Dish();
        dish3.setPrice(30.0);

        List<Dish> dishes = Arrays.asList(dish1, dish2, dish3);

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

        VendorDTO vendor1 = new VendorDTO(UUID.randomUUID(), "Vendor 1", false, "vendor1@example.com", true,
                vendorLocation1);
        VendorDTO vendor2 = new VendorDTO(UUID.randomUUID(), "Rodnev 2", false, "vendor2@example.com", true,
                vendorLocation2);

        List<VendorDTO> vendors = List.of(vendor1, vendor2);

        List<VendorDTO> filteredVendors = vendorService.filterVendors(vendors, "Ven", 0, 100, customerLocation);

        assertThat(filteredVendors).containsExactlyInAnyOrder(vendor1);
    }

    @Test
    void testFilterVendorsByAveragePrice() {
        vendorLocation.setLongitude(52.0);
        vendorLocation.setLatitude(4.0);


        Dish dish1 = new Dish();
        dish1.setPrice(10.0);
        Dish dish2 = new Dish();
        dish2.setPrice(20.0);
        Dish dish3 = new Dish();
        dish3.setPrice(30.0);

        List<Dish> dishes = Arrays.asList(dish1, dish2, dish3);

        VendorDTO vendor = new VendorDTO(UUID.randomUUID(), "Vendor", false, "vendor@example.com", true, vendorLocation);

        when(dishRepository.getDishesByVendorId(vendor.getVendorId())).thenReturn(dishes);

        List<VendorDTO> vendors = List.of(vendor);

        List<VendorDTO> filteredVendors = vendorService.filterVendors(vendors, "Ven", 20, 20, vendorLocation);

        assertThat(filteredVendors).containsExactlyInAnyOrder(vendor);
    }

    @Test
    void testGetVendorOrders() {
        Order order1 = new Order();
        order1.setID(UUID.randomUUID());
        Order order2 = new Order();
        order2.setID(UUID.randomUUID());
        Order order3 = new Order();
        order3.setID(UUID.randomUUID());

        List<Order> orders = List.of(order1, order2, order3);

        paymentMock.pay(order1.getID(), null);
        paymentMock.pay(order3.getID(), null);

        UUID vendorId = UUID.randomUUID();

        when(orderRepository.findByVendorId(vendorId)).thenReturn(orders);

        List<Order> result = vendorService.getVendorOrders(vendorId);

        assertThat(result).containsExactlyInAnyOrder(order1, order3);
    }

    @Test
    void testGetFilteredVendorEntitiesWithAllFilters() {
        Address customerLocation = new Address();
        customerLocation.setLatitude(5.0);
        customerLocation.setLongitude(5.0);

        List<VendorDTO> mockVendors = Arrays.asList(
                new VendorDTO(UUID.randomUUID(), "Vendor1", false, "vendor1@example.com", true, customerLocation),
                new VendorDTO(UUID.randomUUID(), "Vendor2", false, "vendor2@example.com", true, customerLocation)
        );
        when(vendorFacade.requestVendors()).thenReturn(mockVendors);

        // Declare the variables as final
        final String nameFilter = "Vendor1";
        final Integer minAvgPrice = null;
        final Integer maxAvgPrice = null;

        List<Vendor> filteredVendors = vendorService.getFilteredVendorEntities(nameFilter,
                minAvgPrice, maxAvgPrice, customerLocation);

        assertThat(filteredVendors).hasSize(1);
        assertThat(filteredVendors.get(0).getName()).isEqualTo(nameFilter);
    }





}
