package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.model.Address;
import nl.tudelft.sem.template.orders.external.CustomerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerServiceTest {
    CustomerDTO customer;
    CustomerService customerService;

    @BeforeEach
    void setup() {
        this.customer = new CustomerDTO();

        Address currentLocation = createAddress(42, "1221AZ");
        this.customer.setCurrentLocation(currentLocation);

        Address homeLocation = createAddress(199, "3633BJ");
        this.customer.setHomeAddress(homeLocation);

        this.customerService = new CustomerService();
    }

    @Test
    void nullCustomerDTO() {
        assertThat(customerService.getDeliveryLocation(new CustomerDTO())).isNull();
    }

    @Test
    void currentLocationIsNull() {
        customer.setCurrentLocation(null);
        Address result = createAddress(199, "3633BJ");
        assertThat(customerService.getDeliveryLocation(customer)).isEqualTo(result);
    }

    @Test
    void getDeliveryLocationCurrent() {
        Address result = createAddress(42, "1221AZ");
        assertThat(customerService.getDeliveryLocation(customer)).isEqualTo(result);
    }

    private Address createAddress(int houseNumber, String zipcode) {
        Address address = new Address();
        address.setHouseNumber(houseNumber);
        address.setZipCode(zipcode);
        address.setLongitude(1.0);
        address.setLatitude(1.0);

        return address;
    }
}
