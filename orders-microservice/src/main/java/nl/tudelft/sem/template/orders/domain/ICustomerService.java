package nl.tudelft.sem.template.orders.domain;

import nl.tudelft.sem.template.model.Address;
import nl.tudelft.sem.template.orders.external.CustomerDTO;

public interface ICustomerService {
    Address getDeliveryLocation(CustomerDTO customer);
}
