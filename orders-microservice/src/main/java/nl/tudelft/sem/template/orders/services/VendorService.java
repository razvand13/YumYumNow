package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.orders.domain.IVendorService;
import nl.tudelft.sem.template.orders.entities.Address;
import nl.tudelft.sem.template.orders.entities.DishEntity;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import nl.tudelft.sem.template.orders.repositories.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VendorService implements IVendorService {
    private final DishRepository dishRepository;

    @Autowired
    public VendorService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    /**
     * Calculates if the vendor is within the fixed range of the customer.
     * It should be noted that although this method is using latitude and longitude,
     * the calculation is not geographically accurate.
     *
     * @param vendorLocation the vendor's location
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
        double sum = dishes.stream().mapToDouble(DishEntity::getPrice).sum();

        return sum / dishes.size();
    }

}
