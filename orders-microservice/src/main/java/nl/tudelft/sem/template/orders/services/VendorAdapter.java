package nl.tudelft.sem.template.orders.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Component
public class VendorAdapter {
    private final String USERS_URL = "https://localhost:8088";
    private final String DELIVERY_URL = "https://localhost:8081";
    private final HttpClient client = HttpClient.newBuilder().build();

    public boolean existsById(UUID vendorId) {
        return sendGetRequest(USERS_URL + "/vendor/" + vendorId);
    }

    public boolean checkRoleById(UUID userId) {
        if (isRole(userId, "/customers/")) return false;
        if (isRole(userId, "/couriers/")) return false;
        if (isRole(userId, "/admins/")) return false;
        return true;
    }

    private boolean isRole(UUID userId, String path) {
        return sendGetRequest(USERS_URL + path + userId);
    }

    private boolean sendGetRequest(String url) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == HttpStatus.OK.value();
        } catch (Exception e) {
            return false;
        }
    }

}
