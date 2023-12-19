package nl.tudelft.sem.template.orders.domain;

import nl.tudelft.sem.template.orders.entities.Address;
import nl.tudelft.sem.template.orders.entities.Order;
import nl.tudelft.sem.template.orders.external.VendorDTO;

import java.util.List;
import java.util.UUID;

public interface IVendorService {
    boolean isInRange(Address vendorLocation, Address customerLocation);

    Double getAveragePrice(VendorDTO vendor);

    List<VendorDTO> filterVendors(List<VendorDTO> vendors, String name,
                                  Integer minAvgPrice, Integer maxAvgPrice, Address customerLocation);

    List<Order> getVendorOrders(UUID vendorID);
}
