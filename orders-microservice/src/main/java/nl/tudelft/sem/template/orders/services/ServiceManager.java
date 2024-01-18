package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.orders.domain.ICustomerService;
import nl.tudelft.sem.template.orders.domain.IDishService;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.domain.IVendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceManager {
    private final transient IVendorService vendorService;
    private final transient IDishService dishService;
    private final transient IOrderService orderService;
    private final transient ICustomerService customerService;

    /**
     * Constructor for ServiceManager
     *
     * @param vendorService vendor service
     * @param dishService dish service
     * @param orderService order service
     * @param customerService customer service
     */
    @Autowired
    public ServiceManager(IVendorService vendorService, IDishService dishService,
                          IOrderService orderService, ICustomerService customerService) {
        this.vendorService = vendorService;
        this.dishService = dishService;
        this.orderService = orderService;
        this.customerService = customerService;
    }

    public IVendorService getVendorService() {
        return vendorService;
    }


    public IDishService getDishService() {
        return dishService;
    }


    public IOrderService getOrderService() {
        return orderService;
    }


    public ICustomerService getCustomerService() {
        return customerService;
    }

}
