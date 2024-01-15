package nl.tudelft.sem.template.orders.mappers;

import nl.tudelft.sem.template.model.Address;
import nl.tudelft.sem.template.model.Vendor;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import nl.tudelft.sem.template.orders.mappers.interfaces.IVendorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class VendorMapperTest {

    private final IVendorMapper vendorMapper = new VendorMapper();
    private Address address;

    @BeforeEach
    void setup() {
        address = new Address();
        address.setHouseNumber(1);
        address.setLatitude(50.0);
        address.setLongitude(40.0);
        address.setZip("1234AB");
    }

    @Test
    public void testToEntityValidMapping() {
        VendorDTO vendorDTO = new VendorDTO(
                UUID.randomUUID(), "Vendor Name", false, "vendor@example.com", true, address);

        Vendor vendor = vendorMapper.toEntity(vendorDTO);

        assertThat(vendor).isNotNull();
        assertThat(vendor.getID()).isEqualTo(vendorDTO.getVendorId());
        assertThat(vendor.getName()).isEqualTo(vendorDTO.getName());
        assertThat(vendor.getLocation()).isEqualTo(vendorDTO.getLocation());
    }

    @Test
    public void testToDTOValidMapping() {
        Vendor vendor = new Vendor();
        vendor.setID(UUID.randomUUID());
        vendor.setName("Vendor Name");
        vendor.setLocation(address);

        VendorDTO vendorDTO = vendorMapper.toDTO(vendor);

        assertThat(vendorDTO).isNotNull();
        assertThat(vendorDTO.getVendorId()).isEqualTo(vendor.getID());
        assertThat(vendorDTO.getName()).isEqualTo(vendor.getName());
        assertThat(vendorDTO.getLocation()).isEqualTo(vendor.getLocation());
    }

    @Test
    public void testToDTOListValidParsing() {
        // Hard-coded JSON
        String responseBody = """
                [
                    {
                        "vendorId": "550e8400-e29b-41d4-a716-446655440000",
                        "name": "Vendor 1",
                        "blocked": false,
                        "email": "vendor1@example.com",
                        "approved": true,
                        "location": {}
                    },
                    {
                        "vendorId": "550e8400-e29b-41d4-a716-446655440001",
                        "name": "Vendor 2",
                        "blocked": true,
                        "email": "vendor2@example.com",
                        "approved": false,
                        "location": {}
                    }
                ]""";

        List<VendorDTO> vendors = vendorMapper.toDTO(responseBody);

        assertThat(vendors).hasSize(2);
        assertThat(vendors.get(0).getVendorId()).isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        assertThat(vendors.get(1).getVendorId()).isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
    }

    @Test
    public void testToDTOListInvalidParsing() {
        // Hard-coded JSON
        String responseBody = """
                [
                    {
                        "vendorId": "550e8400-e29b-41d4-a716-446655440000",
                        "name": "Vendor 1",
                        "blocked": false,
                        "email": "vendor1@example.com",
                        "approved": true,
                        "location": {}
                    },
                    {
                        "Invalid": "Data"
                    }
                ]""";

        assertThatThrownBy(() -> vendorMapper.toDTO(responseBody)).isInstanceOf(RuntimeException.class);
    }
}
