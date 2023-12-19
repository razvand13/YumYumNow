package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.model.Address;
import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.domain.IVendorService;
import nl.tudelft.sem.template.orders.external.PaymentMock;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import nl.tudelft.sem.template.orders.repositories.DishRepository;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VendorService implements IVendorService {
    private final transient DishRepository dishRepository;
    private final transient OrderRepository orderRepository;
    private final transient PaymentMock paymentMock;

    /**
     * Constructor for Vendor Service
     *
     * @param dishRepository the dish repo
     * @param orderRepository the order repo
     * @param paymentMock the payment mock
     */
    @Autowired
    public VendorService(DishRepository dishRepository, OrderRepository orderRepository, PaymentMock paymentMock) {
        this.dishRepository = dishRepository;
        this.orderRepository = orderRepository;
        this.paymentMock = paymentMock;
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
        List<Dish> dishes = dishRepository.getDishesByVendorId(vendor.getVendorId());

        if (dishes == null || dishes.isEmpty()) {
            return 0.0;
        }

        double sum = dishes.stream().mapToDouble(Dish::getPrice).sum();

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

    /**
     * Collects and returns orders belonging to vendor with ID vendorID
     * Pre-condition: vendorID should be a valid id of a vendor
     *
     * @param vendorId ID of vendor
     * @return List of orders. Only orders that belong to the vendor and are paid are returned;
     */
    @Override
    public List<Order> getVendorOrders(UUID vendorId) {
        List<Order> orders = orderRepository.findByVendorId(vendorId);
        return orders.stream().filter((o) -> paymentMock.isPaid(o.getID())).collect(Collectors.toList());
    }
}
