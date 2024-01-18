package nl.tudelft.sem.template.orders.integration;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Component
public class AdminFacade {

    private static final String USERS_URL = "http://localhost:8088";
    private static final HttpClient CLIENT = HttpClient.newBuilder().build();

    /**
     * Checks if an admin with a specific ID exists
     *
     * @param adminId id to check
     * @return true iff the admin exists in the database
     */
    public boolean existsById(UUID adminId) {
        return sendGetRequest(USERS_URL + "/admins/" + adminId, adminId).statusCode() == 200;
    }

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


    /**
     * Verifies that the user with the given UUID is an admin.
     *
     * @param userId the id of the user
     * @return true if the user is an admin, false otherwise
     */
    public boolean checkRoleById(UUID userId) {
        if (isRole(userId, "/customers/")) {
            return false;
        }
        if (isRole(userId, "/couriers/")) {
            return false;
        }
        if (isRole(userId, "/vendors/")) {
            return false;
        }
        return true;
    }

    /**
     * Helper method. Queries the Users microservice for the specified ID, using a certain path.
     *
     * @param userId user id
     * @param path   path
     * @return true if the status code is 200
     */
    private boolean isRole(UUID userId, String path) {
        return sendGetRequest(USERS_URL + path + userId, userId).statusCode() == 200;
    }
}
