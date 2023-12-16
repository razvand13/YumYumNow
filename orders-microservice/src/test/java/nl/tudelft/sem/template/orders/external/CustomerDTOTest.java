package nl.tudelft.sem.template.orders.external;

import nl.tudelft.sem.template.orders.entities.Address;
import nl.tudelft.sem.template.orders.external.CustomerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerDTOTest {

    private CustomerDTO customer;

    @BeforeEach
    public void setUp() {
        String customerId = "123";
        String name = "John Doe";
        String email = "john@example.com";
        boolean isBlocked = false;
        String payment = "Credit Card";
        List<String> allergens = Arrays.asList("Peanuts", "Shellfish");

        customer = new CustomerDTO(
                customerId, name, email, isBlocked, payment, new Address(), allergens, new Address());
    }

    @Test
    public void testEmptyConstructor() {
        assertThat(new CustomerDTO()).isNotNull();
    }

    @Test
    public void testNonEmptyConstructor() {
        assertThat(customer).isNotNull();
        assertThat(customer.getCustomerId()).isEqualTo("123");
        assertThat(customer.getName()).isEqualTo("John Doe");
        assertThat(customer.getEmail()).isEqualTo("john@example.com");
        assertThat(customer.isBlocked()).isFalse();
        assertThat(customer.getPayment()).isEqualTo("Credit Card");
        assertThat(customer.getHomeAddress()).isNotNull();
        assertThat(customer.getAllergens()).containsExactly("Peanuts", "Shellfish");
        assertThat(customer.getCurrentLocation()).isNotNull();
    }
}
