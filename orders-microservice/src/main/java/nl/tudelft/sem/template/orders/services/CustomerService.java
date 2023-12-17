package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.orders.domain.ICustomerService;
import nl.tudelft.sem.template.orders.entities.Address;
import nl.tudelft.sem.template.orders.external.CustomerDTO;
import org.springframework.stereotype.Service;

@Service
public class CustomerService implements ICustomerService {

    /**
     * Retrieves the delivery location of the customer.
     * If the current location is not specified, it defaults to their home address.
     * If there is no home address either, return null, which must be handled.
     *
     * @param customer customer
     * @return delivery address or null if there is no address
     */
    public Address getDeliveryLocation(CustomerDTO customer) {
        if (customer.getCurrentLocation() != null) {
            return customer.getCurrentLocation();
        }
        if (customer.getHomeAddress() != null) {
            return customer.getHomeAddress();
        }
        return null;
    }
}
