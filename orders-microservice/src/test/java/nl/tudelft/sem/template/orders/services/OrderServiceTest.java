package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.orders.entities.Order;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import nl.tudelft.sem.template.orders.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        UUID orderId = UUID.randomUUID();
        Order order = new Order();

        // Add some random attributes
        order.setVendorId(UUID.randomUUID());
        order.setOrderTime(OffsetDateTime.now());
        order.setTotalPrice(25.5);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order foundOrder = orderService.findById(orderId);

        assertThat(foundOrder).isEqualTo(order);
    }

    @Test
    void testFindByIdInvalid() {
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty()); // Unsuccessful query

        assertThatThrownBy(() -> orderService.findById(orderId)).isInstanceOf(IllegalArgumentException.class);
    }
}
