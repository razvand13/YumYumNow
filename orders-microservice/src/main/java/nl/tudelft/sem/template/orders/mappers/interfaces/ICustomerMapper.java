package nl.tudelft.sem.template.orders.mappers.interfaces;

import nl.tudelft.sem.template.orders.external.CustomerDTO;

/**
 * Client interface in the Adapter design pattern. Declares methods for mapping
 * data to DTOs.
 */
public interface ICustomerMapper {

    CustomerDTO toDTO(String responseBody);

}
