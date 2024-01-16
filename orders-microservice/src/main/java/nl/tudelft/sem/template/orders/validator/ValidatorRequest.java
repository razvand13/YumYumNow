package nl.tudelft.sem.template.orders.validator;

import nl.tudelft.sem.template.model.CreateOrderRequest;
import nl.tudelft.sem.template.model.UpdateDishQtyRequest;
import nl.tudelft.sem.template.model.UpdateOrderStatusRequest;
import nl.tudelft.sem.template.model.UpdateSpecialRequirementsRequest;

import java.util.UUID;

public class ValidatorRequest {
    private UUID userUUID;
    private UserType userType;
    private UUID orderUUID;
    private UUID dishUUID;
    private UpdateDishQtyRequest updateDishQtyRequest;
    private CreateOrderRequest createOrderRequest;
    private UpdateOrderStatusRequest updateOrderStatusRequest;
    private UpdateSpecialRequirementsRequest updateSpecialRequirementsRequest;

    public ValidatorRequest() {

    }

    /**
     * Create a new validator request. Leave unused fields as null
     *
     * @param userUUID UUID of the user making the request
     * @param userType Type of the user making the request
     * @param orderUUID UUID of the order being operated on, if present
     * @param dishUUID UUID of the dish being operated on, if present
     * @param updateDishQtyRequest UpdateDishQtyRequest of the request, if present
     * @param createOrderRequest CreateOrderRequest of the request, if present
     */
    public ValidatorRequest(UUID userUUID, UserType userType, UUID orderUUID, UUID dishUUID,
                            UpdateDishQtyRequest updateDishQtyRequest, CreateOrderRequest createOrderRequest,
                            UpdateOrderStatusRequest updateOrderStatusRequest,
                            UpdateSpecialRequirementsRequest updateSpecialRequirementsRequest) {
        this.userUUID = userUUID;
        this.userType = userType;
        this.orderUUID = orderUUID;
        this.dishUUID = dishUUID;
        this.updateDishQtyRequest = updateDishQtyRequest;
        this.createOrderRequest = createOrderRequest;
        this.updateOrderStatusRequest = updateOrderStatusRequest;
        this.updateSpecialRequirementsRequest = updateSpecialRequirementsRequest;
    }

    public UUID getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(UUID userUUID) {
        this.userUUID = userUUID;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public UUID getOrderUUID() {
        return orderUUID;
    }

    public void setOrderUUID(UUID orderUUID) {
        this.orderUUID = orderUUID;
    }

    public UUID getDishUUID() {
        return dishUUID;
    }

    public void setDishUUID(UUID dishUUID) {
        this.dishUUID = dishUUID;
    }

    public UpdateDishQtyRequest getUpdateDishQtyRequest() {
        return updateDishQtyRequest;
    }

    public UpdateOrderStatusRequest getUpdateOrderStatusRequest() {
        return updateOrderStatusRequest;
    }

    public void setUpdateOrderStatusRequest(UpdateOrderStatusRequest updateOrderStatusRequest) {
        this.updateOrderStatusRequest = updateOrderStatusRequest;
    }

    public void setUpdateDishQtyRequest(UpdateDishQtyRequest updateDishQtyRequest) {
        this.updateDishQtyRequest = updateDishQtyRequest;
    }

    public CreateOrderRequest getCreateOrderRequest() {
        return createOrderRequest;
    }

    public void setCreateOrderRequest(CreateOrderRequest createOrderRequest) {
        this.createOrderRequest = createOrderRequest;
    }

    public UpdateSpecialRequirementsRequest getUpdateSpecialRequirementsRequest() {
        return updateSpecialRequirementsRequest;
    }

    public void setUpdateSpecialRequirementsRequest(UpdateSpecialRequirementsRequest updateSpecialRequirementsRequest) {
        this.updateSpecialRequirementsRequest = updateSpecialRequirementsRequest;
    }
}
