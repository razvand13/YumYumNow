package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.model.Order;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
