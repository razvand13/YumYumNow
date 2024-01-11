package nl.tudelft.sem.template.orders.validator;

import nl.tudelft.sem.template.orders.domain.IDishService;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class DataValidator extends BaseValidator {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private IDishService dishService;
    private final List<DataValidationField> fields;

    /**
     * Construct a new DataValidator. Pass in the field types that you want the validator to check.
     */
    public DataValidator(List<DataValidationField> fields) {
        this.fields = fields;
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

        return true;
    }
}
