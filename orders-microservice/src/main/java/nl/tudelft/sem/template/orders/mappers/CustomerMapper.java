package nl.tudelft.sem.template.orders.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.orders.external.CustomerDTO;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * This method maps a JSON response object to a CustomerDTO
     *
     * @param responseBody JSON
     * @return CustomerDTO after parsing
     */
    public CustomerDTO toDTO(String responseBody) {
        CustomerDTO customer;
        try {
            customer = OBJECT_MAPPER.readValue(responseBody, new TypeReference<CustomerDTO>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return customer;
    }
}
