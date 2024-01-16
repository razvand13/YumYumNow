package nl.tudelft.sem.template.orders.validator;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.integration.AdminFacade;
import nl.tudelft.sem.template.orders.integration.CustomerFacade;
import nl.tudelft.sem.template.orders.integration.VendorFacade;
import nl.tudelft.sem.template.orders.services.DishService;
import nl.tudelft.sem.template.orders.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserAuthorizationValidatorTest {
    @Mock
    CustomerFacade customerFacade;
    @Mock
    VendorFacade vendorFacade;
    @Mock
    AdminFacade adminFacade;
    @Mock
    OrderService orderService;
    @Mock
    DishService dishService;

    UUID customerUUID = UUID.randomUUID();
    UUID vendorUUID = UUID.randomUUID();
    UUID adminUUID = UUID.randomUUID();
    Order order;
    Dish dish;

    UserAuthorizationValidator sut;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        order = new Order();
        order.setID(UUID.randomUUID());

        when(orderService.findById(order.getID())).thenReturn(order);

        dish = new Dish();
        dish.setID(UUID.randomUUID());

        when(dishService.findById(dish.getID())).thenReturn(dish);

        //Define adapter behavior
        when(customerFacade.checkRoleById(any(UUID.class))).thenReturn(true);
        when(customerFacade.checkRoleById(vendorUUID)).thenReturn(false);
        when(customerFacade.checkRoleById(adminUUID)).thenReturn(false);
        when(customerFacade.existsById(any(UUID.class))).thenReturn(false);
        when(customerFacade.existsById(customerUUID)).thenReturn(true);

        when(vendorFacade.checkRoleById(any(UUID.class))).thenReturn(true);
        when(vendorFacade.checkRoleById(customerUUID)).thenReturn(false);
        when(vendorFacade.checkRoleById(adminUUID)).thenReturn(false);
        when(vendorFacade.existsById(any(UUID.class))).thenReturn(false);
        when(vendorFacade.existsById(vendorUUID)).thenReturn(true);

        when(adminFacade.checkRoleById(any(UUID.class))).thenReturn(true);
        when(adminFacade.checkRoleById(customerUUID)).thenReturn(false);
        when(adminFacade.checkRoleById(vendorUUID)).thenReturn(false);
        when(adminFacade.existsById(any(UUID.class))).thenReturn(false);
        when(adminFacade.existsById(adminUUID)).thenReturn(true);

        sut = new UserAuthorizationValidator(customerFacade, vendorFacade, orderService, dishService, adminFacade);
    }

    @Test
    public void testCustomerSuccessNoOrder() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(customerUUID);
        request.setUserType(UserType.CUSTOMER);

        assertThat(sut.handle(request)).isTrue();
    }

    @Test
    public void testCustomerSuccessOrder() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(customerUUID);
        request.setUserType(UserType.CUSTOMER);
        request.setOrderUUID(order.getID());
        order.setCustomerId(customerUUID);

        assertThat(sut.handle(request)).isTrue();
    }

    @Test
    public void testCustomerWrongType() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(vendorUUID);
        request.setUserType(UserType.CUSTOMER);

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testCustomerNotExist() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(UUID.randomUUID());
        request.setUserType(UserType.CUSTOMER);

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.NOT_FOUND));
    }

    @Test
    public void testCustomerOrderNotBelongToCustomer() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(customerUUID);
        request.setUserType(UserType.CUSTOMER);
        request.setOrderUUID(order.getID());
        order.setCustomerId(UUID.randomUUID());

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testVendorSuccessNoOrderNoDish() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(vendorUUID);
        request.setUserType(UserType.VENDOR);

        assertThat(sut.handle(request)).isTrue();
    }

    @Test
    public void testVendorSuccessOrder() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(vendorUUID);
        request.setUserType(UserType.VENDOR);
        request.setOrderUUID(order.getID());
        order.setVendorId(vendorUUID);

        assertThat(sut.handle(request)).isTrue();
    }

    @Test
    public void testVendorSuccessDish() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(vendorUUID);
        request.setUserType(UserType.VENDOR);
        request.setDishUUID(dish.getID());
        dish.setVendorId(vendorUUID);

        assertThat(sut.handle(request)).isTrue();
    }

    @Test
    public void testVendorSuccessBoth() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(vendorUUID);
        request.setUserType(UserType.VENDOR);
        request.setOrderUUID(order.getID());
        order.setVendorId(vendorUUID);
        request.setDishUUID(dish.getID());
        dish.setVendorId(vendorUUID);

        assertThat(sut.handle(request)).isTrue();
    }

    @Test
    public void testVendorOrderNotBelong() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(vendorUUID);
        request.setUserType(UserType.VENDOR);
        request.setOrderUUID(order.getID());
        order.setVendorId(UUID.randomUUID());

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testVendorDishNotBelong() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(vendorUUID);
        request.setUserType(UserType.VENDOR);
        request.setDishUUID(dish.getID());
        dish.setVendorId(UUID.randomUUID());
        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testVendorWrongType() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(customerUUID);
        request.setUserType(UserType.VENDOR);

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testVendorNotExist() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(UUID.randomUUID());
        request.setUserType(UserType.VENDOR);

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.NOT_FOUND));
    }

    @Test
    public void testAdminSuccess() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(adminUUID);
        request.setUserType(UserType.ADMIN);

        assertThat(sut.handle(request)).isTrue();
    }

    @Test
    public void testAdminWrongType() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(customerUUID);
        request.setUserType(UserType.ADMIN);

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testAdminNotExist() {
        ValidatorRequest request = new ValidatorRequest();
        request.setUserUUID(UUID.randomUUID());
        request.setUserType(UserType.ADMIN);

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).matches((e) -> ((ValidationFailureException) e).getFailureStatus().equals(HttpStatus.NOT_FOUND));
    }

    @Test
    public void testNoUserType() {
        ValidatorRequest request = new ValidatorRequest();

        assertThatThrownBy(() -> {
            sut.handle(request);
        }).isInstanceOf(IllegalArgumentException.class);
    }
}