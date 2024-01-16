package nl.tudelft.sem.template.orders.validator;

import nl.tudelft.sem.template.model.Address;
import nl.tudelft.sem.template.model.CreateOrderRequest;
import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Status;
import nl.tudelft.sem.template.model.UpdateDishQtyRequest;
import nl.tudelft.sem.template.model.UpdateOrderStatusRequest;
import nl.tudelft.sem.template.orders.integration.VendorFacade;
import nl.tudelft.sem.template.orders.services.DishService;
import nl.tudelft.sem.template.orders.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DataValidatorTest {
    @Mock
    OrderService orderService;
    @Mock
    DishService dishService;
    @Mock
    VendorFacade vendorFacade;

    Order order;
    UUID customerUUID = UUID.randomUUID();
    UUID vendorUUID = UUID.randomUUID();
    Dish dish;

    DataValidator sut;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        order = new Order();
        order.setID(UUID.randomUUID());

        dish = new Dish();
        dish.setID(UUID.randomUUID());

        when(orderService.findById(order.getID())).thenReturn(order);
        when(dishService.findById(any(UUID.class))).thenReturn(null);
        when(dishService.findById(dish.getID())).thenReturn(dish);
        when(vendorFacade.existsById(any(UUID.class))).thenReturn(false);
        when(vendorFacade.existsById(vendorUUID)).thenReturn(true);
    }

    @Test
    public void testUserSuccess() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.USER));

        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(customerUUID);

        assertThat(sut.handle(request)).isTrue();
    }

    @Test
    public void testUserFailure() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.USER));

        ValidatorRequest request = new ValidatorRequest();

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testOrderSuccess() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.ORDER));

        ValidatorRequest request = new ValidatorRequest();
        request.setOrderUUID(order.getID());
        order.setCustomerId(customerUUID);
        order.setVendorId(vendorUUID);

        assertThat(sut.handle(request)).isTrue();
    }

    @Test
    public void testOrderNullID() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.ORDER));

        ValidatorRequest request = new ValidatorRequest();

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testOrderNotFound() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.ORDER));

        ValidatorRequest request = new ValidatorRequest();
        request.setOrderUUID(UUID.randomUUID());

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.NOT_FOUND));
    }

    @Test
    public void testOrderCustomerVendorIDNoMatch() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.ORDER));

        ValidatorRequest request = new ValidatorRequest();
        request.setOrderUUID(order.getID());
        order.setCustomerId(customerUUID);

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.BAD_REQUEST));

        order.setCustomerId(null);
        order.setVendorId(vendorUUID);

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testDishSuccess() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.DISH));

        ValidatorRequest request = new ValidatorRequest();
        request.setDishUUID(dish.getID());

        assertThat(sut.handle(request)).isTrue();
    }

    @Test
    public void testDishNullID() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.DISH));

        ValidatorRequest request = new ValidatorRequest();

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testDishNotFound() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.DISH));

        ValidatorRequest request = new ValidatorRequest();
        request.setDishUUID(UUID.randomUUID());

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.NOT_FOUND));
    }

    @Test
    public void testUpdateDishSuccess() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.UPDATEDISHQTYREQUEST));

        ValidatorRequest request = new ValidatorRequest();
        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(0);
        request.setUpdateDishQtyRequest(updateDishQtyRequest);

        assertThat(sut.handle(request)).isTrue();
    }

    @Test
    public void testUpdateDishNull() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.UPDATEDISHQTYREQUEST));

        ValidatorRequest request = new ValidatorRequest();

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testUpdateDishInvalidQuantity() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.UPDATEDISHQTYREQUEST));

        ValidatorRequest request = new ValidatorRequest();
        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(-1);
        request.setUpdateDishQtyRequest(updateDishQtyRequest);

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testCreateOrderSuccess() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.CREATEORDERREQUEST));

        ValidatorRequest request = new ValidatorRequest();
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setAddress(new Address());
        createOrderRequest.setVendorId(vendorUUID);
        request.setCreateOrderRequest(createOrderRequest);

        assertThat(sut.handle(request)).isTrue();
    }

    @Test
    public void testCreateOrderNull() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.CREATEORDERREQUEST));

        ValidatorRequest request = new ValidatorRequest();

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testCreateOrderNullAddress() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.CREATEORDERREQUEST));

        ValidatorRequest request = new ValidatorRequest();
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setVendorId(vendorUUID);
        request.setCreateOrderRequest(createOrderRequest);

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testCreateOrderNullVendor() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.CREATEORDERREQUEST));

        ValidatorRequest request = new ValidatorRequest();
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setAddress(new Address());
        request.setCreateOrderRequest(createOrderRequest);

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testCreateOrderVendorNotExist() {
        sut = new DataValidator(orderService, dishService, vendorFacade, List.of(DataValidationField.CREATEORDERREQUEST));

        ValidatorRequest request = new ValidatorRequest();
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setAddress(new Address());
        createOrderRequest.setVendorId(UUID.randomUUID());
        request.setCreateOrderRequest(createOrderRequest);

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.NOT_FOUND));
    }

    @Test
    public void testUpdateStatusSuccess() {
        sut = new DataValidator(orderService, dishService, vendorFacade,
                List.of(DataValidationField.UPDATEORDERSTATUSREQUEST));

        UpdateOrderStatusRequest updateOrderStatusRequest = new UpdateOrderStatusRequest();
        updateOrderStatusRequest.setStatus(Status.ACCEPTED);
        ValidatorRequest request = new ValidatorRequest();
        request.setUpdateOrderStatusRequest(updateOrderStatusRequest);

        assertThat(sut.handle(request)).isTrue();
    }

    @Test
    public void testUpdateStatusNull() {
        sut = new DataValidator(orderService, dishService, vendorFacade,
                List.of(DataValidationField.UPDATEORDERSTATUSREQUEST));

        ValidatorRequest request = new ValidatorRequest();

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testUpdateStatusStatusNull() {
        sut = new DataValidator(orderService, dishService, vendorFacade,
                List.of(DataValidationField.UPDATEORDERSTATUSREQUEST));

        UpdateOrderStatusRequest updateOrderStatusRequest = new UpdateOrderStatusRequest();
        ValidatorRequest request = new ValidatorRequest();
        request.setUpdateOrderStatusRequest(updateOrderStatusRequest);

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testEmpty() {
        sut = new DataValidator(orderService, dishService, vendorFacade,
                new ArrayList<>());

        ValidatorRequest request = new ValidatorRequest();

        assertThat(sut.handle(request)).isTrue();
    }
}