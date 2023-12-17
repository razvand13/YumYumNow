package nl.tudelft.sem.template.orders.domain;

import nl.tudelft.sem.template.orders.entities.Address;
import nl.tudelft.sem.template.orders.external.VendorDTO;

public interface IVendorService {
    boolean isInRange(Address vendorLocation, Address customerLocation);

    Double getAveragePrice(VendorDTO vendor);
}
