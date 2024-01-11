package nl.tudelft.sem.template.orders.validator;

import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.domain.IDishService;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Validator to preform data validation before checking authorization.
 * Which things to check are defined by the list of DataValidationField objects passed during object creation
 * <ul>
 *     <li>DataValidationField.USER: Checks for userUUID being null</li>
 *     <li>DataValidationField.ORDER: Checks for orderUUID being null and whether order exists and contains IDs</li>
 *     <li>DataValidationField.DISH: Checks for dishUUID being null and whether dish exists</li>
 *     <li>DataValidationField.UPDATEDISHQTYREQUEST: Checks for null and valid quantity</li>
 * </ul>
 */
@Component
public class DataValidator extends BaseValidator {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private IDishService dishService;
    @Autowired
    private VendorAdapter vendorAdapter;
    private final List<DataValidationField> fields;

    /**
     * Construct a new DataValidator. Pass in the field types that you want the validator to check.
     */
    public DataValidator(List<DataValidationField> fields) {
        this.fields = fields;
    }

    /**
     * Constructor to manually set order service and dish service for testing
     */
    public DataValidator(List<DataValidationField> fields, IOrderService orderService, IDishService dishService,
                         VendorAdapter vendorAdapter) {
        this.fields = fields;
        this.orderService = orderService;
        this.dishService = dishService;
        this.vendorAdapter = vendorAdapter;
    }

    /**
     * Perform data validation based on which fields were requested to be validated by the constructor
     * Verifies that requested fields are not null, and that Orders and Dishes exist in the database
     *
     * @param request Parameters to validate
     * @return true if success, else throws exception
     */
    public boolean handle(ValidatorRequest request) throws ValidationFailureException {
        if (fields.contains(DataValidationField.USER) && request.getUserUUID() == null) {
            throw new ValidationFailureException(HttpStatus.BAD_REQUEST);
        }

        if (fields.contains(DataValidationField.ORDER)) {
            if (request.getOrderUUID() == null) {
                throw new ValidationFailureException(HttpStatus.BAD_REQUEST);
            }

            if (orderService.findById(request.getOrderUUID()) == null) {
                throw new ValidationFailureException(HttpStatus.NOT_FOUND);
            }

            //Check that order contains customerId and vendorId
            Order order = orderService.findById(request.getOrderUUID());
            if (order.getVendorId() == null || order.getCustomerId() == null) {
                throw new ValidationFailureException(HttpStatus.BAD_REQUEST);
            }
        }

        if (fields.contains(DataValidationField.DISH)) {
            if (request.getDishUUID() == null) {
                throw new ValidationFailureException(HttpStatus.BAD_REQUEST);
            }

            if (dishService.findById(request.getDishUUID()) == null) {
                throw new ValidationFailureException(HttpStatus.NOT_FOUND);
            }
        }

        if (fields.contains(DataValidationField.UPDATEDISHQTYREQUEST)
                && (request.getUpdateDishQtyRequest() == null || request.getUpdateDishQtyRequest().getQuantity() < 0)) {
            throw new ValidationFailureException(HttpStatus.BAD_REQUEST);
        }

        if (fields.contains(DataValidationField.CREATEORDERREQUEST)) {
            if (request.getCreateOrderRequest() == null) {
                throw new ValidationFailureException(HttpStatus.BAD_REQUEST);
            }

            //Check for address being present
            if (request.getCreateOrderRequest().getAddress() == null) {
                throw new ValidationFailureException(HttpStatus.BAD_REQUEST);
            }

            //Check for provided vendor being valid
            if (request.getCreateOrderRequest().getVendorId() == null) {
                throw new ValidationFailureException(HttpStatus.BAD_REQUEST);
            }
            if (!vendorAdapter.existsById(request.getCreateOrderRequest().getVendorId())) {
                throw new ValidationFailureException(HttpStatus.NOT_FOUND);
            }
        }

        return super.checkNext(request);
    }
}
