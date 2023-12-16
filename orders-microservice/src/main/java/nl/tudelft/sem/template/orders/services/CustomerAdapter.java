package nl.tudelft.sem.template.orders.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.orders.entities.Address;
import nl.tudelft.sem.template.orders.external.CustomerDTO;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import nl.tudelft.sem.template.orders.mappers.CustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

@Component
public class CustomerAdapter {
    private static final String USERS_URL = "https://localhost:8088";
    private static final String DELIVERY_URL = "https://localhost:8081";
    private final HttpClient client = HttpClient.newBuilder().build();
    private final CustomerMapper customerMapper;

    @Autowired
    public CustomerAdapter(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    /**
     * Requests customer from the Users microservice
     * @param customerId id of that customer
     * @return CustomerDTO object containing all relevant attributes
     */
    public CustomerDTO requestCustomer(UUID customerId) {
        String uri = USERS_URL + "/customers/" + customerId;
        final HttpRequest requestCustomer = createGetRequest(uri);

        CustomerDTO customer;
        try {
            HttpResponse<String> responseCustomer = client.send(requestCustomer, HttpResponse.BodyHandlers.ofString());
            customer = customerMapper.toDTO(responseCustomer.body());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return customer;
    }

    /**
     * Send a GET request to a specified uri
     * @param uri uri
     * @return the request that was created
     */
    private HttpRequest createGetRequest(String uri) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();
    }

}
