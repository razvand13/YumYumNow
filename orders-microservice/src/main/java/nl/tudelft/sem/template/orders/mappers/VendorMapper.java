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

    /**
     * This method maps a VendorDTO to a Vendor
     *
     * @param vendorDTO VendorDTO
     * @return Vendor
     */
    public Vendor toEntity(VendorDTO vendorDTO) {
        Vendor vendor = new Vendor();
        vendor.setID(vendorDTO.getVendorId());

        return vendor;
    }

    /**
     * This method maps a Vendor to a VendorDTO
     *
     * @param vendor Vendor
     * @return VendorDTO
     */
    public VendorDTO toDTO(Vendor vendor) {
        VendorDTO vendorDTO = new VendorDTO();
        vendorDTO.setVendorId(vendor.getID());

        return vendorDTO;
    }

    /**
     * This method maps a JSON response object to a List of VendorDTOs
     *
     * @param responseBody JSON
     * @return List of VendorDTO
     */
    public List<VendorDTO> toDTO(String responseBody) {
        try {
            return OBJECT_MAPPER.readValue(responseBody, new TypeReference<List<VendorDTO>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}