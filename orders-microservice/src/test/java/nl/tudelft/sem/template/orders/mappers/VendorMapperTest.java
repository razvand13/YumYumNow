package nl.tudelft.sem.template.orders.mappers;

import nl.tudelft.sem.template.orders.entities.Vendor;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class VendorMapperTest {

    private final VendorMapper vendorMapper = new VendorMapper();

    @Test
    public void testToEntityValidMapping() {
        VendorDTO vendorDTO = new VendorDTO(
                UUID.randomUUID(), "Vendor Name", false, "vendor@example.com", true, null);

        Vendor vendor = vendorMapper.toEntity(vendorDTO);

        assertThat(vendor).isNotNull();
        assertThat(vendor.getID()).isEqualTo(vendorDTO.getVendorId());
    }

    @Test
    public void testToDTOValidMapping() {
        Vendor vendor = new Vendor();
        vendor.setID(UUID.randomUUID());

        VendorDTO vendorDTO = vendorMapper.toDTO(vendor);

        assertThat(vendorDTO).isNotNull();
        assertThat(vendorDTO.getVendorId()).isEqualTo(vendor.getID());
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
