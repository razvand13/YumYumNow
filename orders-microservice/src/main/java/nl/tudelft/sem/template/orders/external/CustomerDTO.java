package nl.tudelft.sem.template.orders.external;

import java.util.List;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.tudelft.sem.template.model.Address;

@Getter
@EqualsAndHashCode
@ToString
public class CustomerDTO {

    private UUID customerId;
    private String name;
    private String email;
    private boolean isBlocked;
    private String payment;
    private Address homeAddress;
    private List<String> allergens;
    private Address currentLocation;

    public CustomerDTO() {
    }

    /**
     * Constructor for CustomerDTO.
     * CustomerDTO is what is expected as a response from querying an external microservice.
     *
     * @param customerId      customer id
     * @param name            name
     * @param email           email
     * @param isBlocked       blocked
     * @param payment         payment
     * @param homeAddress     home address
     * @param allergens       allergens
     * @param currentLocation current location
     */
    public CustomerDTO(UUID customerId, String name, String email, boolean isBlocked,
                       String payment, Address homeAddress, List<String> allergens,
                       Address currentLocation) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.isBlocked = isBlocked;
        this.payment = payment;
        this.homeAddress = homeAddress;
        this.allergens = allergens;
        this.currentLocation = currentLocation;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * THIS SETTER IS NEEDED
     * The generated setter is generated as 'setBlocked' which interferes with the ObjectMapper,
     * as the attribute inside the JSON file is called 'isBlocked'
     *
     * @param isBlocked blocked
     */
    public void setIsBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }

    public void setCurrentLocation(Address currentLocation) {
        this.currentLocation = currentLocation;
    }
}