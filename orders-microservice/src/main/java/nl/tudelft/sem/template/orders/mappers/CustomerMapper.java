package nl.tudelft.sem.template.orders.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.orders.external.CustomerDTO;
import nl.tudelft.sem.template.orders.mappers.interfaces.ICustomerMapper;
import org.springframework.stereotype.Component;

/**
 * Adapter class in the Adapter design pattern. Implements ICustomerMapper interface.
 * Used for converting JSON response data into CustomerDTO objects.
 * This class acts as a converter between the users microservice's data format and our internal data format.
 */

@Component
public class CustomerMapper implements ICustomerMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * This method maps a JSON response object to a CustomerDTO
     *
     * @param responseBody JSON
     * @return CustomerDTO after parsing
     */
    public CustomerDTO toDTO(String responseBody) {
        try {
            return OBJECT_MAPPER.readValue(responseBody, new TypeReference<CustomerDTO>() {
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
