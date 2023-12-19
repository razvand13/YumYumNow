package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.orders.domain.IVendorService;
import nl.tudelft.sem.template.orders.entities.Address;
import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import nl.tudelft.sem.template.orders.repositories.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VendorService implements IVendorService {
    private final transient DishRepository dishRepository;

    @Autowired
    public VendorService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    /**
     * Calculates if the vendor is within the fixed range of the customer.
     * It should be noted that although this method is using latitude and longitude,
     * the calculation is not geographically accurate.
     *
     * @param vendorLocation   the vendor's location
     * @param customerLocation the customer's location
     * @return true iff they are in range
     */
    public boolean isInRange(Address vendorLocation, Address customerLocation) {
        Double vendorLatitude = vendorLocation.getLatitude();
        Double vendorLongitude = vendorLocation.getLongitude();
        Double customerLatitude = customerLocation.getLatitude();
        Double customerLongitude = customerLocation.getLongitude();

        double distance = Math.hypot(vendorLatitude - customerLatitude, vendorLongitude - customerLongitude);

        return distance <= 5.0; // not geographically accurate
    }

    /**
     * Calculate the average price of all items from a vendor
     *
     * @param vendor vendor
     * @return the average price
     */
    public Double getAveragePrice(VendorDTO vendor) {
        List<DishEntity> dishes = dishRepository.getDishesByVendorId(vendor.getVendorId());

        if (dishes == null || dishes.isEmpty()) {
            return 0.0;
        }

        double sum = dishes.stream().mapToDouble(DishEntity::getPrice).sum();

        return sum / dishes.size();
    }

    /**
     * Filter vendors by name, average price and distance to delivery location
     *
     * @param vendors          vendors
     * @param name             name
     * @param minAvgPrice      min average price
     * @param maxAvgPrice      max average price
     * @param customerLocation customer location
     * @return the list of filtered vendors
     */
    public List<VendorDTO> filterVendors(List<VendorDTO> vendors, String name,
                                         Integer minAvgPrice, Integer maxAvgPrice, Address customerLocation) {
        List<VendorDTO> filteredVendors = new ArrayList<>();
        for (VendorDTO vendor : vendors) {
            Address vendorLocation = vendor.getLocation();
            Double avgPrice = getAveragePrice(vendor);

            // If these filters are not specified, max them out
            minAvgPrice = minAvgPrice != null ? minAvgPrice : Integer.MIN_VALUE;
            maxAvgPrice = maxAvgPrice != null ? maxAvgPrice : Integer.MAX_VALUE;

            if (isInRange(vendorLocation, customerLocation)
                    && avgPrice >= minAvgPrice && avgPrice <= maxAvgPrice) {
                if (name == null || vendor.getName().contains(name)) {
                    filteredVendors.add(vendor);
                }
            }
        }

        return filteredVendors;
    }

}
