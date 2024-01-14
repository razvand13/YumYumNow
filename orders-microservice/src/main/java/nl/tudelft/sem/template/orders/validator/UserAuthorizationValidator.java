package nl.tudelft.sem.template.orders.validator;

import nl.tudelft.sem.template.orders.domain.IDishService;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.services.AdminAdapter;
import nl.tudelft.sem.template.orders.services.CustomerAdapter;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Preforms authorization checks on specified request.
 *
 * <p>Specifically:</p>
 *
 * <p>If UserType=CUSTOMER:<ul>
 *     <li>Check that userUUID is a customer</li>
 *     <li>If orderUUID specified, check order ownership</li>
 * </ul></p>
 *
 * <p>If UserType=VENDOR:<ul>
 *     <li>Check that userUUID is a vendor</li>
 *     <li>If orderUUID specified, check that order belongs to vendor</li>
 *     <li>If dishUUID specified, check that dish belongs to vendor</li>
 * </ul></p>
 *
 * <p>If UserType=ADMIN<ul>
 *     <li>Check that userUUID is an admin</li>
 * </ul></p>
 */
@Component
public class UserAuthorizationValidator extends BaseValidator {
    @Autowired
    private transient CustomerAdapter customerAdapter;
    @Autowired
    private transient VendorAdapter vendorAdapter;
    @Autowired
    private transient AdminAdapter adminAdapter;
    @Autowired
    private transient IOrderService orderService;
    @Autowired
    private transient IDishService dishService;

    public UserAuthorizationValidator() {
    }

    /**
     * Constructor to manually set fields for testing
     */
    public UserAuthorizationValidator(CustomerAdapter customerAdapter, VendorAdapter vendorAdapter,
                                      IOrderService orderService, IDishService dishService) {
        this.customerAdapter = customerAdapter;
        this.vendorAdapter = vendorAdapter;
        this.orderService = orderService;
        this.dishService = dishService;
    }

    /**
     * Constructor to manually set fields for testing
     */
    public UserAuthorizationValidator(CustomerAdapter customerAdapter, VendorAdapter vendorAdapter,
                                      IOrderService orderService, IDishService dishService, AdminAdapter adminAdapter) {
        this.customerAdapter = customerAdapter;
        this.vendorAdapter = vendorAdapter;
        this.orderService = orderService;
        this.dishService = dishService;
        this.adminAdapter = adminAdapter;
    }

    @Override
    public boolean handle(ValidatorRequest request) throws ValidationFailureException {
        if (request.getUserType() == null) {
            throw new IllegalArgumentException();
        }

        switch (request.getUserType()) {
            case CUSTOMER:
                //Check type first, fails on not being of type customer
                if (!customerAdapter.checkRoleById(request.getUserUUID())) {
                    throw new ValidationFailureException(HttpStatus.UNAUTHORIZED);
                }
                //Check existence of customer
                if (!customerAdapter.existsById(request.getUserUUID())) {
                    throw new ValidationFailureException(HttpStatus.NOT_FOUND);
                }
                //If orderUUID specified, check that order belongs to customer
                if (request.getOrderUUID() != null && !orderService.findById(request.getOrderUUID())
                        .getCustomerId().equals(request.getUserUUID())) {
                    throw new ValidationFailureException(HttpStatus.UNAUTHORIZED);
                }
                break;
            case VENDOR:
                //Check user type -- verifies user not of another type
                if (!vendorAdapter.checkRoleById(request.getUserUUID())) {
                    throw new ValidationFailureException(HttpStatus.UNAUTHORIZED);
                }
                //Check existence of vendor
                if (!vendorAdapter.existsById(request.getUserUUID())) {
                    throw new ValidationFailureException(HttpStatus.NOT_FOUND);
                }
                //If orderUUID specified, check that order belongs to vendor
                if (request.getOrderUUID() != null && !orderService.findById(request.getOrderUUID())
                        .getVendorId().equals(request.getUserUUID())) {
                    throw new ValidationFailureException(HttpStatus.UNAUTHORIZED);
                }
                //If dishUUID specified, check that dish belongs to vendor
                if (request.getDishUUID() != null && !dishService.findById(request.getDishUUID())
                        .getVendorId().equals(request.getUserUUID())) {
                    throw new ValidationFailureException(HttpStatus.UNAUTHORIZED);
                }
                break;
            case ADMIN:
                if (!adminAdapter.checkRoleById(request.getUserUUID())) {
                    throw new ValidationFailureException(HttpStatus.UNAUTHORIZED);
                }

                if (!adminAdapter.existsById(request.getUserUUID())) {
                    throw new ValidationFailureException(HttpStatus.NOT_FOUND);
                }
                break;
            default:
                throw new ValidationFailureException();
        }

        return super.checkNext(request);
    }
}
