package nl.tudelft.sem.template.orders.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import nl.tudelft.sem.template.orders.mappers.VendorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
public class VendorAdapter {

    private static final String USERS_URL = "https://localhost:8088";
    private static final String DELIVERY_URL = "https://localhost:8081";
    private final HttpClient client = HttpClient.newBuilder().build();
    private final VendorMapper vendorMapper;

    @Autowired
    public VendorAdapter(VendorMapper vendorMapper) {
        this.vendorMapper = vendorMapper;
    }

    /**
     * Request all vendors from the Users microservice
     *
     * @return a list of VendorDTO
     */
    public List<VendorDTO> requestVendors() {
        String uri = USERS_URL + "/vendors";
        final HttpRequest requestVendors = createGetRequest(uri);

        List<VendorDTO> vendors;
        try {
            HttpResponse<String> responseVendors = client.send(requestVendors, HttpResponse.BodyHandlers.ofString());
            vendors = vendorMapper.toDTO(responseVendors.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return vendors;
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