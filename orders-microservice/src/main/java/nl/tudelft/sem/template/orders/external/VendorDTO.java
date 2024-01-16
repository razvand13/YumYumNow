package nl.tudelft.sem.template.orders.external;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.tudelft.sem.template.model.Address;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@ToString
public class VendorDTO {

    private UUID vendorId;
    private String name;
    private boolean isBlocked;
    private String email;
    private boolean isApproved;
    private Address location;

    public VendorDTO() {
    }

    /**
     * Constructor for VendorDTO.
     * VendorDTO is what is expected as a response from querying an external microservice.
     *
     * @param vendorId vendor id
     * @param name name
     * @param isBlocked blocked
     * @param email email
     * @param isApproved approved
     * @param location location
     */
    public VendorDTO(UUID vendorId, String name, boolean isBlocked, String email,
                     boolean isApproved, Address location) {
        this.vendorId = vendorId;
        this.name = name;
        this.isBlocked = isBlocked;
        this.email = email;
        this.isApproved = isApproved;
        this.location = location;
    }

    public void setVendorId(UUID vendorId) {
        this.vendorId = vendorId;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setEmail(String email) {
        this.email = email;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public void setLocation(Address location) {
        this.location = location;
    }
}