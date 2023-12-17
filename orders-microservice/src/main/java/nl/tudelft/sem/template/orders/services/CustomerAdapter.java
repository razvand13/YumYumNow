package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.orders.external.CustomerDTO;
import nl.tudelft.sem.template.orders.mappers.CustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Component
public class CustomerAdapter {
    private static final String USERS_URL = "https://localhost:8088";
    private static final String DELIVERY_URL = "https://localhost:8081";
    private static final HttpClient CLIENT = HttpClient.newBuilder().build();
    private final transient CustomerMapper customerMapper;

    @Autowired
    public CustomerAdapter(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    /**
     * Requests customer from the Users microservice
     *
     * @param customerId id of that customer
     * @return CustomerDTO object containing all relevant attributes
     */
    public CustomerDTO requestCustomer(UUID customerId) {
        String uri = USERS_URL + "/customers/" + customerId;
        final HttpRequest requestCustomer = createGetRequest(uri);

        try {
            HttpResponse<String> responseCustomer = CLIENT.send(requestCustomer, HttpResponse.BodyHandlers.ofString());
            return customerMapper.toDTO(responseCustomer.body());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Send a GET request to a specified uri
     *
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
