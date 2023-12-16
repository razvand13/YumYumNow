package nl.tudelft.sem.template.orders.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.orders.entities.Vendor;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VendorMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Vendor toEntity(VendorDTO vendorDTO) {
        Vendor vendor = new Vendor();
        vendor.setID(vendorDTO.getVendorId());

        return vendor;
    }

    public VendorDTO toDTO(Vendor vendor) {
        VendorDTO vendorDTO = new VendorDTO();
        vendorDTO.setVendorId(vendor.getID());

        return vendorDTO;
    }

    public List<VendorDTO> toDTO(String responseBody) {
        List<VendorDTO> vendors;
        try {
            vendors = OBJECT_MAPPER.readValue(responseBody, new TypeReference<List<VendorDTO>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return vendors;
    }
}