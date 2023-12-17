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
     * Checks if a customer with a specified ID exists
     *
     * @param customerId id
     * @return true iff the customer exists in the database
     */
    public boolean existsById(UUID customerId) {
        return sendGetRequest(USERS_URL + "/customers/" + customerId).statusCode() == 200;
    }

    /**
     * Checks if the user with the given UUID is a vendor, courier or admin.
     * If so, the user is not authorized.
     *
     * @param userId the id of the user
     * @return true iff the user is a customer
     */
    public boolean checkRoleById(UUID userId) {
        if (isRole(userId, "/vendors/")) {
            return false;
        }
        if (isRole(userId, "/couriers/")) {
            return false;
        }
        if (isRole(userId, "/admins/")) {
            return false;
        }
        return true;
    }

    /**
     * Helper method. Queries the Users microservice for the specified ID, using a certain path.
     *
     * @param userId user id
     * @param path path
     * @return true iff the status code is 200
     */
    private boolean isRole(UUID userId, String path) {
        return sendGetRequest(USERS_URL + path + userId).statusCode() == 200;
    }


    /**
     * Requests customer from the Users microservice
     *
     * @param customerId id of that customer
     * @return CustomerDTO object containing all relevant attributes
     */
    public CustomerDTO requestCustomer(UUID customerId) {
        String uri = USERS_URL + "/customers/" + customerId;
        return customerMapper.toDTO(sendGetRequest(uri).body());
    }

    /**
     * Send a GET request to a specified uri
     *
     * @param uri uri
     * @return the request that was created
     */
    private HttpResponse<String> sendGetRequest(String uri) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();
        try {
            return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
