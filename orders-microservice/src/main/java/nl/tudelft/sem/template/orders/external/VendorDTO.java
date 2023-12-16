package nl.tudelft.sem.template.orders.external;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.tudelft.sem.template.orders.entities.Address;
import java.util.UUID;

@Getter
@Setter
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
}