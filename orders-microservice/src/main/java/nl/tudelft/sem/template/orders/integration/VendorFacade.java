package nl.tudelft.sem.template.orders.integration;

import nl.tudelft.sem.template.orders.external.VendorDTO;
import nl.tudelft.sem.template.orders.mappers.interfaces.IVendorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

/**
 * Client class that interacts with the Adapter (VendorMapper) through the Interface (IVendorMapper).
 * Serves for communication with users microservice and uses the Adapter to convert responses
 * to a suitable format for our usages
 * Includes methods for business logic that involve external service calls (used for authorization).
 */
@Component
public class VendorFacade {

    private static final String USERS_URL = "https://gyyl7.wiremockapi.cloud";
    private static final String DELIVERY_URL = "https://localhost:8081";
    private static final HttpClient CLIENT = HttpClient.newBuilder().build();
    private final transient IVendorMapper IVendorMapper;

    @SuppressWarnings("checkstyle:ParameterName")
    @Autowired
    public VendorFacade(IVendorMapper vendorMapper) {
        this.IVendorMapper = vendorMapper;
    }

    /**
     * Checks if a vendor with a specified ID exists
     *
     * @param vendorId id
     * @return true iff the vendor exists in the database
     */
    public boolean existsById(UUID vendorId) {
        return sendGetRequest(USERS_URL + "/vendors/" + vendorId, vendorId).statusCode() == 200;
    }

    /**
     * Checks if the user with the given UUID is a customer, courier or admin.
     * If so, the user is not authorized.
     *
     * @param userId the id of the user
     * @return true iff the user is a vendor
     */
    public boolean checkRoleById(UUID userId) {
        if (isRole(userId, "/customers/")) {
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
        return sendGetRequest(USERS_URL + path + userId, userId).statusCode() == 200;
    }

    /**
     * Request all vendors from the Users microservice
     *
     * @return a list of VendorDTO
     */
    public List<VendorDTO> requestVendors(UUID userId) {
        String uri = USERS_URL + "/vendors";
        return IVendorMapper.toDTO(sendGetRequest(uri, userId).body());
    }

    /**
     * Send a GET request to a specified uri
     *
     * @param uri uri
     * @return the request that was created
     */
    private HttpResponse<String> sendGetRequest(String uri, UUID userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("X-User-Id", userId.toString())
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
