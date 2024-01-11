package nl.tudelft.sem.template.orders.external;

import nl.tudelft.sem.template.model.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class VendorDTOTest {

    private VendorDTO vendor;

    /**
     * Sets up some commonly used parameters
     */
    @BeforeEach
    public void setUp() {
        UUID vendorId = UUID.randomUUID();
        String name = "Vendor Name";
        boolean isBlocked = false;
        String email = "vendor@example.com";
        boolean isApproved = true;
        Address location = new Address();

        vendor = new VendorDTO(vendorId, name, isBlocked, email, isApproved, location);
    }

    @Test
    public void testEmptyConstructor() {
        assertThat(new VendorDTO()).isNotNull();
    }

    @Test
    public void testNonEmptyConstructor() {
        assertThat(vendor).isNotNull();
        assertThat(vendor.getVendorId()).isNotNull();
        assertThat(vendor.getName()).isEqualTo("Vendor Name");
        assertThat(vendor.isBlocked()).isFalse();
        assertThat(vendor.getEmail()).isEqualTo("vendor@example.com");
        assertThat(vendor.isApproved()).isTrue();
        assertThat(vendor.getLocation()).isNotNull();
    }
}
