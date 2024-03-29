package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.model.Dish;
import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.model.OrderedDish;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByIdValid() {
        Order order = new Order();

        // Add some random attributes
        order.setVendorId(UUID.randomUUID());
        order.setOrderTime(OffsetDateTime.now());
        order.setTotalPrice(25.5);

        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order foundOrder = orderService.findById(orderId);

        assertThat(foundOrder).isEqualTo(order);
    }

    @Test
    void testFindByIdInvalid() {
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty()); // Unsuccessful query
        Order order = orderService.findById(orderId);

        assertThat(order).isNull();
    }

    @Test
    void testSave() {
        Order order = new Order();
        order.setID(UUID.randomUUID());
        order.setVendorId(UUID.randomUUID());
        order.setOrderTime(OffsetDateTime.now());
        order.setTotalPrice(25.5);
        order.setSpecialRequirements("Leave it at the door");

        when(orderRepository.save(order)).thenReturn(order);

        Order savedOrder = orderService.save(order);

        verify(orderRepository, Mockito.times(1)).save(order);

        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder).isEqualTo(order);
    }

    @Test
    void calculateOrderPriceWithEmptyList() {
        List<OrderedDish> orderedDishes = new ArrayList<>();

        double totalPrice = orderService.calculateOrderPrice(orderedDishes);

        assertThat(totalPrice).isEqualTo(0.0);
    }

    @Test
    void calculateOrderPriceWithNonEmptyList() {
        List<OrderedDish> orderedDishes = List.of(
                createOrderedDish(10.0, 2),
                createOrderedDish(5.0, 3)
        );

        double totalPrice = orderService.calculateOrderPrice(orderedDishes);

        assertThat(totalPrice).isEqualTo(35.0);
    }

    @Test
    void orderedDishInOrderFound() {
        UUID dishId = UUID.randomUUID();
        Order order = createOrderWithDish(dishId);

        Optional<OrderedDish> result = orderService.orderedDishInOrder(order, dishId);

        assertThat(result).isPresent();
    }

    @Test
    void orderedDishInOrderNotFound() {
        UUID dishId = UUID.randomUUID();
        Order order = new Order();
        order.setDishes(new ArrayList<>());

        Optional<OrderedDish> result = orderService.orderedDishInOrder(order, dishId);

        assertThat(result).isNotPresent();
    }


    @Test
    void removeDishOrderFound() {
        UUID dishId = UUID.randomUUID();
        List<OrderedDish> orderedDishes = new ArrayList<>();
        orderedDishes.add(createOrderedDish(10.0, 2, dishId));

        Optional<OrderedDish> result = orderService.removeDishOrder(orderedDishes, dishId);

        assertThat(result).isPresent();
    }

    @Test
    void removeDishOrderNotFound() {
        UUID dishId = UUID.randomUUID();
        List<OrderedDish> orderedDishes = new ArrayList<>();

        Optional<OrderedDish> result = orderService.removeDishOrder(orderedDishes, dishId);

        assertThat(result).isNotPresent();
    }

    @Test
    void findOrdersByCustomerIdTest() {
        UUID customerId = UUID.randomUUID();
        List<Order> mockOrders = Arrays.asList(new Order(), new Order());

        when(orderRepository.findByCustomerId(customerId)).thenReturn(mockOrders);

        List<Order> resultOrders = orderService.findOrdersByCustomerId(customerId);

        assertThat(resultOrders).isEqualTo(mockOrders);
        assertThat(resultOrders.size()).isEqualTo(2);
    }

    @Test
    void findAllReturnsEmptyList() {
        List<Order> orderList = new ArrayList<>();
        orderList.add(createOrderByPrice(12.0));
        orderList.add(createOrderByPrice(42.0));
        when(orderRepository.findAll()).thenReturn(orderList);

        List<Order> expectedList = new ArrayList<>();
        expectedList.add(createOrderByPrice(12.0));
        expectedList.add(createOrderByPrice(42.0));

        List<Order> result = orderService.findAll();
        assertThat(result.size()).isNotEqualTo(0);
        assertThat(result).isEqualTo(expectedList);
    }

    @Test
    void checkDeleteCallRepository() {
        UUID orderId = UUID.randomUUID();
        orderService.delete(orderId);
        verify(orderRepository).deleteById(orderId);
    }

    @Test
    void orderedDishInOrderFilterCheck() {
        UUID dishIdToFind = UUID.randomUUID();
        OrderedDish dish = createOrderedDishFromDish(createDish(UUID.randomUUID(), "Wrong Dish"));
        OrderedDish dish2 = createOrderedDishFromDish(createDish(dishIdToFind, "Right Dish"));

        List<OrderedDish> dishes = new ArrayList<>();
        dishes.add(dish);
        dishes.add(dish2);

        Order order = createOrderByPrice(0.0);
        order.setDishes(dishes);

        Optional<OrderedDish> result = orderService.orderedDishInOrder(order, dishIdToFind);
        assertThat(result.get().getDish().getName()).isEqualTo("Right Dish");
    }

    @Test
    void removeDishOrderFilterCheck() {
        UUID dishIdToFind = UUID.randomUUID();
        OrderedDish dish = createOrderedDishFromDish(createDish(UUID.randomUUID(), "Wrong Dish"));
        OrderedDish dish2 = createOrderedDishFromDish(createDish(dishIdToFind, "Found Dish"));

        List<OrderedDish> dishes = List.of(dish, dish2);

        Optional<OrderedDish> result = orderService.removeDishOrder(dishes, dishIdToFind);
        assertThat(result.get().getDish().getName()).isEqualTo("Found Dish");
    }

    // Utility methods for creating test objects

    private Order createOrderByPrice(double price) {
        Order order = new Order();
        order.setTotalPrice(price);
        return order;
    }

    private Dish createDish(UUID dishId, String name) {
        Dish dish = new Dish();
        dish.setID(dishId);
        dish.setName(name);
        return dish;
    }

    private Order createOrderWithDish(UUID dishId) {
        Order order = new Order();
        List<OrderedDish> orderedDishes = new ArrayList<>();
        orderedDishes.add(createOrderedDish(10.0, 2, dishId));
        order.setDishes(orderedDishes);
        return order;
    }

    private OrderedDish createOrderedDishFromDish(Dish dish) {
        OrderedDish orderedDish = new OrderedDish();
        orderedDish.setDish(dish);
        orderedDish.setId(dish.getID());
        return orderedDish;
    }

    private OrderedDish createOrderedDish(double price, int quantity, UUID dishId) {
        Dish dish = mock(Dish.class);
        when(dish.getPrice()).thenReturn(price);
        when(dish.getID()).thenReturn(dishId);
        OrderedDish orderedDish = new OrderedDish();
        orderedDish.setDish(dish);
        orderedDish.setQuantity(quantity);
        return orderedDish;
    }

    private OrderedDish createOrderedDish(double price, int quantity) {
        Dish dish = mock(Dish.class);
        when(dish.getPrice()).thenReturn(price);
        OrderedDish orderedDish = new OrderedDish();
        orderedDish.setDish(dish);
        orderedDish.setQuantity(quantity);
        return orderedDish;
    }

}
