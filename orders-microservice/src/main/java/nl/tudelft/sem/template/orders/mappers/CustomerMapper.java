package nl.tudelft.sem.template.orders.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
        try {
            OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            var res = OBJECT_MAPPER.readValue(responseBody, new TypeReference<CustomerDTO>() {
            });
            if (res.equals(new CustomerDTO())) {
                throw new RuntimeException();
            }

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
