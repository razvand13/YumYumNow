package nl.tudelft.sem.template.orders.external;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.tudelft.sem.template.orders.entities.Address;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CustomerDTO {

    private String customerId;
    private String name;
    private String email;
    private boolean isBlocked;
    private String payment;
    private Address homeAddress;
    private List<String> allergens;
    private Address currentLocation;

    public CustomerDTO() {
    }

    public CustomerDTO(String customerId, String name, String email, boolean isBlocked,
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
}
