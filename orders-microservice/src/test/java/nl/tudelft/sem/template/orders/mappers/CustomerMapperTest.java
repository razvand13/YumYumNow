package nl.tudelft.sem.template.orders.mappers;

import nl.tudelft.sem.template.orders.external.CustomerDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CustomerMapperTest {

    private final CustomerMapper customerMapper = new CustomerMapper();

    @Test
    public void testToDTOValidParsing() {
        // Hard-coded JSON
        String responseBody = """
                {
                "customerId":"123",
                "name":"John Doe",
                "email":"john@example.com",
                "blocked":false,
                "payment":"Credit Card",
                "homeAddress":{},
                "allergens":[],
                "currentLocation":{}
                }""";

        CustomerDTO customerDTO = customerMapper.toDTO(responseBody);

        assertThat(customerDTO).isNotNull();
        assertThat(customerDTO.getCustomerId()).isEqualTo("123");
        assertThat(customerDTO.getName()).isEqualTo("John Doe");
        assertThat(customerDTO.getEmail()).isEqualTo("john@example.com");
        assertThat(customerDTO.isBlocked()).isFalse();
        assertThat(customerDTO.getPayment()).isEqualTo("Credit Card");
        assertThat(customerDTO.getHomeAddress()).isNotNull();
        assertThat(customerDTO.getAllergens()).isEmpty();
        assertThat(customerDTO.getCurrentLocation()).isNotNull();
    }

    @Test
    public void testToDTOInvalidParsing() {
        String responseBody = """
                {
                "Invalid":"Data"
                }""";

        assertThatThrownBy(() -> customerMapper.toDTO(responseBody)).isInstanceOf(RuntimeException.class);
    }
}
