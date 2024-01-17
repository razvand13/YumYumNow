package nl.tudelft.sem.template.orders.controllers;


import nl.tudelft.sem.template.model.Vendor;
import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.Address;
import nl.tudelft.sem.template.model.UpdateDishQtyRequest;
import nl.tudelft.sem.template.model.CreateOrderRequest;
import nl.tudelft.sem.template.model.OrderedDish;
import nl.tudelft.sem.template.model.Status;
import nl.tudelft.sem.template.model.UpdateSpecialRequirementsRequest;
import nl.tudelft.sem.template.orders.domain.ICustomerService;
import nl.tudelft.sem.template.orders.domain.IDishService;
import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.domain.IVendorService;
import nl.tudelft.sem.template.orders.external.CustomerDTO;
import nl.tudelft.sem.template.orders.external.VendorDTO;
import nl.tudelft.sem.template.orders.mappers.VendorMapper;
import nl.tudelft.sem.template.orders.mappers.interfaces.IVendorMapper;
import nl.tudelft.sem.template.orders.integration.CustomerFacade;
import nl.tudelft.sem.template.orders.integration.VendorFacade;
import nl.tudelft.sem.template.orders.validator.DataValidator;
import nl.tudelft.sem.template.orders.validator.DataValidationField;
import nl.tudelft.sem.template.orders.validator.UserAuthorizationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.management.RuntimeMBeanException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.Collections;
import java.util.Optional;
import java.util.Arrays;
import java.util.ArrayList;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;


class CustomerControllerTest {

    @Mock
    private IVendorMapper IVendorMapper;
    @Mock
    private IVendorService vendorService;
    @Mock
    private IDishService dishService;
    @Mock
    private IOrderService orderService;
    @Mock
    private ICustomerService customerService;
    @Mock
    private CustomerFacade customerFacade;
    @Mock
    private VendorFacade vendorFacade;
    @Mock
    private DataValidator dataValidator;
    @Mock
    ApplicationContext applicationContext;
    @InjectMocks
    private CustomerController customerController;

    private CreateOrderRequest createOrderRequest;
    private UpdateSpecialRequirementsRequest updateSpecialRequirementsRequest;

    private UUID customerId;
    private UUID orderId;
    private UUID dishId;
    private Order order;

    @BeforeEach
    void setup() {
        IVendorMapper = mock(VendorMapper.class);
        vendorService = mock(IVendorService.class);
        dishService = mock(IDishService.class);
        orderService = mock(IOrderService.class);
        customerService = mock(ICustomerService.class);
        customerFacade = mock(CustomerFacade.class);
        vendorFacade = mock(VendorFacade.class);
        applicationContext = mock(ApplicationContext.class);
        dataValidator = mock(DataValidator.class);

        when(applicationContext.getBean(eq(DataValidator.class), anyList()))
                .thenReturn(dataValidator);

        // getVendors
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER))))
                .thenReturn(new DataValidator(List.of(DataValidationField.USER),
                        orderService, dishService, vendorFacade));

        // createOrder
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER, DataValidationField.CREATEORDERREQUEST))))
                .thenReturn(new DataValidator(List.of(DataValidationField.USER, DataValidationField.CREATEORDERREQUEST),
                        orderService, dishService, vendorFacade));

        // getVendorDishes & getOrder
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER, DataValidationField.ORDER))))
                .thenReturn(new DataValidator(List.of(DataValidationField.USER, DataValidationField.ORDER),
                        orderService, dishService, vendorFacade));

        // addDishToOrder
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER, DataValidationField.DISH,
                        DataValidationField.ORDER, DataValidationField.UPDATEDISHQTYREQUEST))))
                .thenReturn(new DataValidator(
                        List.of(DataValidationField.USER, DataValidationField.DISH,
                                DataValidationField.ORDER, DataValidationField.UPDATEDISHQTYREQUEST),
                        orderService, dishService, vendorFacade));


        // removeDishFromOrder
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER, DataValidationField.ORDER, DataValidationField.DISH))))
                .thenReturn(new DataValidator(
                        List.of(DataValidationField.USER, DataValidationField.ORDER, DataValidationField.DISH),
                        orderService, dishService, vendorFacade));


        // updateDishQty
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER, DataValidationField.ORDER,
                        DataValidationField.DISH, DataValidationField.UPDATEDISHQTYREQUEST))))
                .thenReturn(new DataValidator(
                        List.of(DataValidationField.USER, DataValidationField.ORDER,
                                DataValidationField.DISH, DataValidationField.UPDATEDISHQTYREQUEST),
                        orderService, dishService, vendorFacade));


        // getDishFromOrder
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER, DataValidationField.ORDER, DataValidationField.DISH))))
                .thenReturn(new DataValidator(
                        List.of(DataValidationField.USER, DataValidationField.ORDER, DataValidationField.DISH),
                        orderService, dishService, vendorFacade));

        // updateSpecialRequirements
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER, DataValidationField.ORDER,
                        DataValidationField.UPDATESPECIALREQUIREMENTSREQUEST))))
                .thenReturn(new DataValidator(
                        List.of(DataValidationField.USER, DataValidationField.ORDER,
                                DataValidationField.UPDATESPECIALREQUIREMENTSREQUEST),
                        orderService, dishService, vendorFacade));

        when(applicationContext.getBean(UserAuthorizationValidator.class))
                .thenReturn(new UserAuthorizationValidator(customerFacade, vendorFacade, orderService, dishService));

        customerController = new CustomerController(IVendorMapper, vendorService, dishService, orderService,
                customerService, customerFacade, vendorFacade, applicationContext);

        customerId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        dishId = UUID.randomUUID();
        order = new Order();
        order.setID(orderId);
        order.setCustomerId(customerId);
        order.setVendorId(UUID.randomUUID());

        updateSpecialRequirementsRequest = new UpdateSpecialRequirementsRequest();
        createOrderRequest = new CreateOrderRequest();

        when(orderService.findById(orderId)).thenReturn(order);
        when(dishService.findById(dishId)).thenReturn(new Dish());
    }

    @Test
    void getOrderSuccess() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);

        ResponseEntity<Order> responseEntity = customerController.getOrder(customerId, orderId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(order);
    }

    @Test
    void getOrderBadRequest() {
        ResponseEntity<Order> responseEntity = customerController.getOrder(null, null);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getOrderUnauthorized() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<Order> responseEntity = customerController.getOrder(customerId, orderId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getOrderNotFoundCustomer() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(false);

        ResponseEntity<Order> responseEntity = customerController.getOrder(customerId, orderId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getOrderNotFoundOrder() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> responseEntity = customerController.getOrder(customerId, orderId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getOrderUnauthorizedCustomerMismatch() {
        UUID anotherCustomerId = UUID.randomUUID();
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);

        ResponseEntity<Order> responseEntity = customerController.getOrder(anotherCustomerId, orderId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getDishFromOrderSuccess() {
        Dish dish = new Dish();
        dish.setID(dishId);
        OrderedDish orderedDish = new OrderedDish();
        orderedDish.setDish(dish);
        order.setDishes(Collections.singletonList(orderedDish));

        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(dishService.findById(dishId)).thenReturn(dish);

        ResponseEntity<OrderedDish> responseEntity = customerController.getDishFromOrder(customerId, orderId, dishId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(orderedDish);
    }

    @Test
    void getDishFromOrderNotFound() {
        ResponseEntity<OrderedDish> responseEntity =
                customerController.getDishFromOrder(customerId, UUID.randomUUID(), null);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getDishFromOrderUnauthorized() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<OrderedDish> responseEntity = customerController.getDishFromOrder(customerId, orderId, dishId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getDishFromOrderNotFoundDish() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(dishService.findById(dishId)).thenReturn(null);

        ResponseEntity<OrderedDish> responseEntity = customerController.getDishFromOrder(customerId, orderId, dishId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getDishFromOrderNotFoundOrder() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<OrderedDish> responseEntity = customerController.getDishFromOrder(customerId, orderId, dishId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getDishFromOrderDishDoesNotBelongToOrder() {
        Dish dish = new Dish();
        dish.setID(dishId);
        dish.setName("Chicken Tikka Masala");
        OrderedDish orderedDish = new OrderedDish();
        orderedDish.setDish(dish);
        order.setDishes(Collections.singletonList(orderedDish));
        Dish anotherDish = new Dish();
        UUID anotherDishId = UUID.randomUUID();
        dish.setID(anotherDishId);

        when(dishService.findById(anotherDishId)).thenReturn(anotherDish);
        when(dishService.findById(dishId)).thenReturn(dish);
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);

        ResponseEntity<OrderedDish> responseEntity =
                customerController.getDishFromOrder(customerId, orderId, anotherDishId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getDishesFromOrderEmptyDishList() {
        Dish dish = new Dish();
        dish.setID(dishId);
        dish.setName("Chicken Tikka Masala");
        order.setDishes(Collections.emptyList());

        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(dishService.findById(dishId)).thenReturn(dish);

        ResponseEntity<OrderedDish> responseEntity =
                customerController.getDishFromOrder(customerId, orderId, dishId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    @Test
    void getVendorsSuccess() {
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);

        CustomerDTO customer = new CustomerDTO();
        customer.setCurrentLocation(new Address());
        when(customerService.getDeliveryLocation(customer)).thenReturn(new Address());
        when(customerFacade.requestCustomer(customerId)).thenReturn(customer);

        List<VendorDTO> vendors = Collections.singletonList(new VendorDTO());
        when(vendorFacade.requestVendors()).thenReturn(vendors);
        when(vendorService.filterVendors(vendors, null, null, null, customer.getCurrentLocation()))
                .thenReturn(vendors);

        when(IVendorMapper.toEntity(new VendorDTO())).thenReturn(new Vendor());

        ResponseEntity<List<Vendor>> response = customerController.getVendors(customerId, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getVendorsBadRequest() {
        ResponseEntity<List<Vendor>> response = customerController.getVendors(null, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getVendorsNotFound() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(false);

        ResponseEntity<List<Vendor>> response = customerController.getVendors(customerId, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getVendorsUnauthorized() {
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(customerFacade.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<List<Vendor>> response = customerController.getVendors(customerId, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getVendorDishesBadRequestNullParams() {
        ResponseEntity<List<Dish>> response = customerController.getVendorDishes(null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getVendorDishesUnauthorizedCustomer() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<List<Dish>> response = customerController.getVendorDishes(customerId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getVendorDishesCustomerNotFound() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(false);

        ResponseEntity<List<Dish>> response = customerController.getVendorDishes(customerId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getVendorDishesOrderNotFound() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<List<Dish>> response = customerController.getVendorDishes(customerId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getVendorDishesSuccess() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);

        Order order = new Order();
        order.setCustomerId(customerId);
        UUID vendorId = UUID.randomUUID();
        order.setVendorId(vendorId);
        when(orderService.findById(orderId)).thenReturn(order);

        List<Dish> dishes = Collections.singletonList(new Dish());
        when(dishService.findAllByVendorId(vendorId)).thenReturn(dishes);

        CustomerDTO customer = setupCustomer();
        when(customerFacade.requestCustomer(customerId)).thenReturn(customer);

        ResponseEntity<List<Dish>> response = customerController.getVendorDishes(customerId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dishes);
    }

    @Test
    void getVendorsNullCustomerLocation() {
        when(applicationContext.getBean(eq(DataValidator.class), eq(List.of(DataValidationField.USER))))
                .thenReturn(dataValidator);

        CustomerDTO customerDTO = new CustomerDTO();
        when(customerFacade.requestCustomer(customerId)).thenReturn(customerDTO);

        when(customerService.getDeliveryLocation(customerDTO)).thenReturn(null);

        ResponseEntity<List<Vendor>> response = customerController.getVendors(customerId, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    void addDishToOrderOrderNotFound() {
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> response = customerController
                .addDishToOrder(customerId, orderId, dishId, new UpdateDishQtyRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addDishToOrderUnauthorized() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(false);

        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(1);
        ResponseEntity<Order> responseEntity = customerController
                .addDishToOrder(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void addDishToOrderDishExists() {
        DataValidator mockDataValidator = mock(DataValidator.class);
        UserAuthorizationValidator mockUserAuthorizationValidator = mock(UserAuthorizationValidator.class);
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER, DataValidationField.DISH,
                        DataValidationField.ORDER, DataValidationField.UPDATEDISHQTYREQUEST))))
                .thenReturn(mockDataValidator);
        when(applicationContext.getBean(UserAuthorizationValidator.class)).thenReturn(mockUserAuthorizationValidator);
        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(2);
        OrderedDish existingOrderedDish = new OrderedDish();
        existingOrderedDish.setQuantity(1);

        Order mockOrder = new Order();
        when(orderService.findById(orderId)).thenReturn(mockOrder);
        when(dishService.findById(dishId)).thenReturn(new Dish());
        when(orderService.orderedDishInOrder(mockOrder, dishId)).thenReturn(Optional.of(existingOrderedDish));

        ResponseEntity<Order> response = customerController
                .addDishToOrder(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(existingOrderedDish.getQuantity()).isEqualTo(3);
        verify(orderService).save(any(Order.class));
    }

    @Test
    void addDishToOrderDishNotExists() {
        DataValidator mockDataValidator = mock(DataValidator.class);
        UserAuthorizationValidator mockUserAuthorizationValidator = mock(UserAuthorizationValidator.class);
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER, DataValidationField.DISH,
                        DataValidationField.ORDER, DataValidationField.UPDATEDISHQTYREQUEST))))
                .thenReturn(mockDataValidator);
        when(applicationContext.getBean(UserAuthorizationValidator.class)).thenReturn(mockUserAuthorizationValidator);
        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(2);

        Order mockOrder = new Order();
        Dish newDish = new Dish();
        when(orderService.findById(orderId)).thenReturn(mockOrder);
        when(dishService.findById(dishId)).thenReturn(newDish);
        when(orderService.orderedDishInOrder(mockOrder, dishId)).thenReturn(Optional.empty());

        ResponseEntity<Order> response = customerController
                .addDishToOrder(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderService).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getDishes()).hasSize(1);
        OrderedDish addedDish = savedOrder.getDishes().get(0);
        assertThat(addedDish.getDish()).isEqualTo(newDish);
        assertThat(addedDish.getQuantity()).isEqualTo(2);
    }

    @Test
    void removeDishFromOrderOrderNotFound() {
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> response = customerController.removeDishFromOrder(customerId, orderId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void removeDishFromOrderUnauthorized() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<Order> responseEntity = customerController.removeDishFromOrder(customerId, orderId, dishId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void removeDishFromOrderSuccess() {
        DataValidator mockDataValidator = mock(DataValidator.class);
        UserAuthorizationValidator mockUserAuthorizationValidator = mock(UserAuthorizationValidator.class);
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER, DataValidationField.ORDER, DataValidationField.DISH))))
                .thenReturn(mockDataValidator);
        when(applicationContext.getBean(UserAuthorizationValidator.class)).thenReturn(mockUserAuthorizationValidator);

        OrderedDish existingOrderedDish = new OrderedDish();
        Dish dish = new Dish();
        dish.setID(dishId);
        existingOrderedDish.setDish(dish);

        UUID remainingDishId = UUID.randomUUID();
        OrderedDish remainingOrderedDish = new OrderedDish();
        Dish remainingDish = new Dish();
        remainingDish.setID(remainingDishId);
        remainingOrderedDish.setDish(remainingDish);

        Order mockOrder = new Order();
        mockOrder.setDishes(new ArrayList<>(List.of(remainingOrderedDish, existingOrderedDish)));
        when(orderService.findById(orderId)).thenReturn(mockOrder);
        when(orderService.calculateOrderPrice(anyList())).thenReturn(100.0);

        when(orderService.save(any(Order.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        ResponseEntity<Order> response = customerController.removeDishFromOrder(customerId, orderId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDishes().get(0).getDish().getID()).isEqualTo(remainingDishId);
        verify(orderService).save(any(Order.class));
    }


    @Test
    void removeDishFromOrderDishNotFound() {
        DataValidator mockDataValidator = mock(DataValidator.class);
        UserAuthorizationValidator mockUserAuthorizationValidator = mock(UserAuthorizationValidator.class);
        when(applicationContext.getBean(eq(DataValidator.class),
                eq(List.of(DataValidationField.USER, DataValidationField.ORDER, DataValidationField.DISH))))
                .thenReturn(mockDataValidator);
        when(applicationContext.getBean(UserAuthorizationValidator.class)).thenReturn(mockUserAuthorizationValidator);

        Order mockOrder = new Order();
        mockOrder.setDishes(new ArrayList<>());
        when(orderService.findById(orderId)).thenReturn(mockOrder);

        ResponseEntity<Order> response = customerController.removeDishFromOrder(customerId, orderId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateDishQtyNegativeQuantity() {
        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(-1);

        ResponseEntity<Order> response = customerController
                .updateDishQty(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateDishQtyOrderNotFound() {
        when(orderService.findById(orderId)).thenReturn(null);

        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(1); // Set a valid quantity

        ResponseEntity<Order> response = customerController
                .updateDishQty(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateDishQtyUnauthorized() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(false);

        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(1);
        ResponseEntity<Order> responseEntity = customerController
                .updateDishQty(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void updateDishQtyDishExists() {
        DataValidator mockDataValidator = mock(DataValidator.class);
        UserAuthorizationValidator mockUserAuthorizationValidator = mock(UserAuthorizationValidator.class);
        when(applicationContext.getBean(eq(DataValidator.class), anyList())).thenReturn(mockDataValidator);
        when(applicationContext.getBean(UserAuthorizationValidator.class)).thenReturn(mockUserAuthorizationValidator);

        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(3);

        OrderedDish existingOrderedDish = new OrderedDish();
        Dish dish = new Dish();
        dish.setID(dishId);
        existingOrderedDish.setDish(dish);
        existingOrderedDish.setQuantity(2);

        Order mockOrder = new Order();
        mockOrder.setDishes(new ArrayList<>(List.of(existingOrderedDish)));

        when(orderService.findById(orderId)).thenReturn(mockOrder);
        when(orderService.calculateOrderPrice(anyList())).thenReturn(100.0);

        ResponseEntity<Order> response = customerController
                .updateDishQty(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(existingOrderedDish.getQuantity()).isEqualTo(3);
        verify(orderService).save(any(Order.class));
    }

    @Test
    void updateDishQtyDishNotExists() {
        DataValidator mockDataValidator = mock(DataValidator.class);
        UserAuthorizationValidator mockUserAuthorizationValidator = mock(UserAuthorizationValidator.class);
        when(applicationContext.getBean(eq(DataValidator.class), anyList())).thenReturn(mockDataValidator);
        when(applicationContext.getBean(UserAuthorizationValidator.class)).thenReturn(mockUserAuthorizationValidator);

        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(3);

        Order mockOrder = new Order();
        mockOrder.setDishes(new ArrayList<>());

        when(orderService.findById(orderId)).thenReturn(mockOrder);
        when(orderService.calculateOrderPrice(anyList())).thenReturn(100.0);

        ResponseEntity<Order> response = customerController
                .updateDishQty(customerId, orderId, UUID.randomUUID(), updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mockOrder.getDishes()).isEmpty();
        verify(orderService).save(any(Order.class));
    }

    @Test
    void getPersonalOrderHistoryWithNullCustomerId() {
        ResponseEntity<List<Order>> response = customerController.getPersonalOrderHistory(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getPersonalOrderHistoryWithUnauthorizedCustomer() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<List<Order>> response = customerController.getPersonalOrderHistory(customerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getPersonalOrderHistoryWithNonExistingCustomer() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(false);

        ResponseEntity<List<Order>> response = customerController.getPersonalOrderHistory(customerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPersonalOrderHistoryWithNoOrders() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(orderService.findOrdersByCustomerId(customerId)).thenReturn(new ArrayList<>());

        ResponseEntity<List<Order>> response = customerController.getPersonalOrderHistory(customerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPersonalOrderHistoryWithOrders() {
        List<Order> orders = List.of(new Order());
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(orderService.findOrdersByCustomerId(customerId)).thenReturn(orders);

        ResponseEntity<List<Order>> response = customerController.getPersonalOrderHistory(customerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(orders);
    }

    @Test
    void reorderWithNullInputs() {
        ResponseEntity<Order> response = customerController.reorder(null, null, new Address());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void reorderWithUnauthorizedCustomer() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<Order> response = customerController.reorder(customerId, orderId, new Address());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void reorderWithNonExistingCustomer() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(false);

        ResponseEntity<Order> response = customerController.reorder(customerId, orderId, new Address());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void reorderWithNonExistingPreviousOrder() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> response = customerController.reorder(customerId, orderId, new Address());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void reorderWithOrderNotBelongingToCustomer() {
        Order previousOrder = createOrder();
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(previousOrder);
        previousOrder.setCustomerId(UUID.randomUUID());

        ResponseEntity<Order> response = customerController.reorder(customerId, orderId, new Address());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void reorderSuccess() {
        Order previousOrder = createOrder();
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(previousOrder);
        when(orderService.save(any(Order.class))).thenReturn(new Order());

        ResponseEntity<Order> response = customerController.reorder(customerId, orderId, new Address());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateSpecialRequirementsWhenOrderIsNull() {
        UpdateSpecialRequirementsRequest updateSpecialRequirementsRequest = new UpdateSpecialRequirementsRequest();
        ResponseEntity<Order> response = customerController
                .updateSpecialRequirements(customerId, null, updateSpecialRequirementsRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(customerFacade);
        verifyNoInteractions(orderService);
    }

    @Test
    void updateSpecialRequirementsWhenCustomerIsNull() {
        ResponseEntity<Order> response = customerController
                .updateSpecialRequirements(null, orderId, updateSpecialRequirementsRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(customerFacade);
        verifyNoInteractions(orderService);
    }

    @Test
    void updateSpecialRequirementsCustomerNotFound() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(false);

        ResponseEntity<Order> responseEntity = customerController
                .updateSpecialRequirements(customerId, orderId, updateSpecialRequirementsRequest);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateSpecialRequirementsCustomerInvalid() {
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(customerFacade.checkRoleById(customerId)).thenReturn(false);

        ResponseEntity<Order> response = customerController
                .updateSpecialRequirements(customerId, orderId, updateSpecialRequirementsRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void updateSpecialRequirementsRequestOrderIsNull() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(null);

        ResponseEntity<Order> response = customerController
                .updateSpecialRequirements(customerId, orderId, updateSpecialRequirementsRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateSpecialRequirementsCustomerNotEqual() {
        Order order = createOrder();
        order.setCustomerId(UUID.randomUUID());

        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);

        ResponseEntity<Order> response = customerController
                .updateSpecialRequirements(customerId, orderId, updateSpecialRequirementsRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void updateSpecialRequirementsSuccess() {
        Order order = createOrder();

        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(order);
        when(orderService.save(order)).thenReturn(order);

        updateSpecialRequirementsRequest.setSpecialRequirements("New Special Requirements");
        Order result = createOrder();
        result.setSpecialRequirements("New Special Requirements");
        result.setVendorId(order.getVendorId());
        result.setOrderTime(order.getOrderTime());

        ResponseEntity<Order> response = customerController
                .updateSpecialRequirements(customerId, orderId, updateSpecialRequirementsRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(result);
        verify(customerFacade).existsById(customerId);
        verify(customerFacade).checkRoleById(customerId);
        verify(orderService, times(4)).findById(orderId);
    }

    @Test
    void getDishesWithClashingAllergens() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);

        Order order = createOrder();

        when(orderService.findById(orderId)).thenReturn(order);

        CustomerDTO customer = setupCustomer("Gluten");
        when(customerFacade.requestCustomer(customerId)).thenReturn(customer);

        List<Dish> vendorDishes = setupVendorDishes(
                new String[]{"Nuts, Lactose"},
                new String[]{"Nuts", "Lactose", "Chocolate", "Gluten"});

        when(dishService.findAllByVendorId(order.getVendorId())).thenReturn(vendorDishes);

        ResponseEntity<List<Dish>> response = customerController.getVendorDishes(customerId, orderId);

        List<Dish> result = setupVendorDishes(new String[]{"Nuts, Lactose"});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(result);

        verify(customerFacade).checkRoleById(customerId);
        verify(customerFacade).existsById(customerId);
        verify(dishService).findAllByVendorId(order.getVendorId());
    }

    @Test
    void getDishesWithNoClashingAllergens() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);

        Order order = createOrder();

        when(orderService.findById(orderId)).thenReturn(order);

        CustomerDTO customer = setupCustomer("Gluten");
        when(customerFacade.requestCustomer(customerId)).thenReturn(customer);

        List<Dish> vendorDishes = setupVendorDishes(new String[]{}, new String[]{"Nuts", "Lactose", "Chocolate"});

        when(dishService.findAllByVendorId(order.getVendorId())).thenReturn(vendorDishes);

        ResponseEntity<List<Dish>> response = customerController.getVendorDishes(customerId, orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(vendorDishes);

        verify(customerFacade).checkRoleById(customerId);
        verify(customerFacade).existsById(customerId);
        verify(dishService).findAllByVendorId(order.getVendorId());
    }

    @Test
    void testReorderAttributes() {
        Order previousOrder = createOrder();
        previousOrder.setLocation(createTestingAddress(42));
        previousOrder.setTotalPrice(12.5);
        previousOrder.setSpecialRequirements("Leave at the door");
        previousOrder.setStatus(Status.ACCEPTED);

        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);
        when(orderService.findById(orderId)).thenReturn(previousOrder);

        // Mock the behavior of orderService.save
        when(orderService.save(any(Order.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        ResponseEntity<Order> response = customerController.reorder(customerId, orderId, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Order order = response.getBody();

        assertThat(order.getCustomerId()).isEqualTo(previousOrder.getCustomerId());
        assertThat(order.getVendorId()).isEqualTo(previousOrder.getVendorId());
        assertThat(order.getDishes()).isEqualTo(previousOrder.getDishes());
        assertThat(order.getOrderTime()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(Status.PENDING);
        assertThat(order.getLocation()).isEqualTo(previousOrder.getLocation());
        assertThat(order.getTotalPrice()).isEqualTo(previousOrder.getTotalPrice());
    }

    @Test
    void testCorrectNewPriceDishQty() {
        DataValidator mockDataValidator = mock(DataValidator.class);
        UserAuthorizationValidator mockUserAuthorizationValidator = mock(UserAuthorizationValidator.class);
        when(applicationContext.getBean(eq(DataValidator.class), anyList())).thenReturn(mockDataValidator);
        when(applicationContext.getBean(UserAuthorizationValidator.class)).thenReturn(mockUserAuthorizationValidator);

        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(5);

        OrderedDish orderedDish = new OrderedDish();
        Dish dish = new Dish();
        dish.setID(dishId);
        dish.setPrice(15.0);
        orderedDish.setDish(dish);
        orderedDish.setQuantity(2);

        Order order = new Order();
        order.setDishes(new ArrayList<>(List.of(orderedDish)));

        when(orderService.findById(orderId)).thenReturn(order);
        when(orderService.calculateOrderPrice(anyList())).thenAnswer(invocation -> {
            return calculateOrderPrice(invocation.getArgument(0));
        });
        // Mock the behavior of orderService.save
        when(orderService.save(any(Order.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        ResponseEntity<Order> response = customerController
                .updateDishQty(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(orderedDish.getQuantity()).isEqualTo(5);
        assertThat(response.getBody().getTotalPrice()).isEqualTo(75.0);
        verify(orderService).save(any(Order.class));
    }

    @Test
    void testCorrectNewPriceAddingDish() {
        DataValidator mockDataValidator = mock(DataValidator.class);
        UserAuthorizationValidator mockUserAuthorizationValidator = mock(UserAuthorizationValidator.class);
        when(applicationContext.getBean(eq(DataValidator.class), anyList())).thenReturn(mockDataValidator);
        when(applicationContext.getBean(UserAuthorizationValidator.class)).thenReturn(mockUserAuthorizationValidator);

        UpdateDishQtyRequest updateDishQtyRequest = new UpdateDishQtyRequest();
        updateDishQtyRequest.setQuantity(2);

        Dish dish = new Dish();
        dish.setName("New Dish");
        dish.setID(dishId);
        dish.setPrice(15.0);

        Order order = createOrder();

        when(orderService.findById(orderId)).thenReturn(order);
        when(dishService.findById(dishId)).thenReturn(dish);
        when(orderService.orderedDishInOrder(order, dishId)).thenReturn(Optional.empty());

        when(orderService.calculateOrderPrice(anyList())).thenAnswer(invocation -> {
            return calculateOrderPrice(invocation.getArgument(0));
        });
        // Mock the behavior of orderService.save
        when(orderService.save(any(Order.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        ResponseEntity<Order> response = customerController
                .addDishToOrder(customerId, orderId, dishId, updateDishQtyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalPrice()).isEqualTo(30.0);
        verify(orderService).save(any(Order.class));
    }

    @Test
    void testCorrectPriceRemovingDish() {
        when(customerFacade.checkRoleById(customerId)).thenReturn(true);
        when(customerFacade.existsById(customerId)).thenReturn(true);

        Order order = createOrder();
        UUID vendorId = order.getVendorId();

        Dish dish = new Dish();
        dish.setName("New Dish");
        dish.setVendorId(vendorId);
        dish.setID(dishId);
        dish.setPrice(12.0);
        Dish newDish = new Dish();
        newDish.setID(UUID.randomUUID());
        newDish.setPrice(14.0);

        OrderedDish newOrderedDish = new OrderedDish();
        newOrderedDish.setDish(newDish);
        order.addDishesItem(newOrderedDish);
        OrderedDish orderedDish = new OrderedDish();
        orderedDish.setDish(dish);
        orderedDish.setId(dishId);
        order.addDishesItem(orderedDish);

        List<Dish> dishes = new ArrayList<>();
        dishes.add(dish);

        when(orderService.findById(orderId)).thenReturn(order);
        when(dishService.findAllByVendorId(vendorId)).thenReturn(dishes);

        when(orderService.calculateOrderPrice(anyList())).thenAnswer(invocation -> {
            return calculateOrderPrice(invocation.getArgument(0));
        });
        // Mock the behavior of orderService.save
        when(orderService.save(any(Order.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        ResponseEntity<Order> response = customerController.removeDishFromOrder(customerId, orderId, dishId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalPrice()).isEqualTo(0.0);
        verify(orderService).save(any(Order.class));
    }

    private double calculateOrderPrice(List<OrderedDish> dishes) {
        return dishes.stream()
                .mapToDouble(orderedDish -> orderedDish.getDish().getPrice() * orderedDish.getQuantity())
                .sum();
    }

    private CustomerDTO setupCustomer(String... customerAllergens) {
        CustomerDTO customerDTO = new CustomerDTO();
        if (customerAllergens != null) {
            List<String> allergens = new ArrayList<>(Arrays.asList(customerAllergens));
            customerDTO.setAllergens(allergens);
        }
        return customerDTO;
    }

    private Address createTestingAddress(Integer housenumber) {
        Address address = new Address();
        address.setLatitude(1.0);
        address.setLongitude(1.0);
        address.setHouseNumber(housenumber);
        address.setZipCode("1344AH");

        return address;
    }

    private List<Dish> setupVendorDishes(String[]... dishAllergens) {
        List<Dish> vendorDishes = new ArrayList<>();
        for (String[] allergens : dishAllergens) {
            Dish dish = new Dish();
            dish.setAllergens(Arrays.asList(allergens));
            vendorDishes.add(dish);
        }
        return vendorDishes;
    }

    private Order createOrder() {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setVendorId(UUID.randomUUID());
        order.setDishes(new ArrayList<>());
        order.setOrderTime(OffsetDateTime.now());
        order.setStatus(Status.PENDING);
        return order;
    }
}
