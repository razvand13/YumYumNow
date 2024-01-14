package nl.tudelft.sem.template.orders.validator;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.services.AdminAdapter;
import nl.tudelft.sem.template.orders.services.CustomerAdapter;
import nl.tudelft.sem.template.orders.services.DishService;
import nl.tudelft.sem.template.orders.services.OrderService;
import nl.tudelft.sem.template.orders.services.VendorAdapter;
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
    CustomerAdapter customerAdapter;
    @Mock
    VendorAdapter vendorAdapter;
    @Mock
    AdminAdapter adminAdapter;
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
        when(customerAdapter.checkRoleById(any(UUID.class))).thenReturn(true);
        when(customerAdapter.checkRoleById(vendorUUID)).thenReturn(false);
        when(customerAdapter.checkRoleById(adminUUID)).thenReturn(false);
        when(customerAdapter.existsById(any(UUID.class))).thenReturn(false);
        when(customerAdapter.existsById(customerUUID)).thenReturn(true);

        when(vendorAdapter.checkRoleById(any(UUID.class))).thenReturn(true);
        when(vendorAdapter.checkRoleById(customerUUID)).thenReturn(false);
        when(vendorAdapter.checkRoleById(adminUUID)).thenReturn(false);
        when(vendorAdapter.existsById(any(UUID.class))).thenReturn(false);
        when(vendorAdapter.existsById(vendorUUID)).thenReturn(true);

        when(adminAdapter.checkRoleById(any(UUID.class))).thenReturn(true);
        when(adminAdapter.checkRoleById(customerUUID)).thenReturn(false);
        when(adminAdapter.checkRoleById(vendorUUID)).thenReturn(false);
        when(adminAdapter.existsById(any(UUID.class))).thenReturn(false);
        when(adminAdapter.existsById(adminUUID)).thenReturn(true);

        sut = new UserAuthorizationValidator(customerAdapter, vendorAdapter, orderService, dishService, adminAdapter);
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
        request.setOrderUUID(order.getID());
        order.setVendorId(UUID.randomUUID());
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