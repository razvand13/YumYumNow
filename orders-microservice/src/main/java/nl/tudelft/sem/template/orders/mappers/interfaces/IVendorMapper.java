package nl.tudelft.sem.template.orders.mappers.interfaces;

import nl.tudelft.sem.template.model.Vendor;
import nl.tudelft.sem.template.orders.external.VendorDTO;

import java.util.List;

/**
 * Client interface in the Adapter design pattern. Declares methods for mapping
 * data to and from DTOs.
 */
public interface IVendorMapper {

    List<VendorDTO> toDTO(String responseBody);

    VendorDTO toDTO(Vendor vendor);

    Vendor toEntity(VendorDTO vendorDTO);
}
