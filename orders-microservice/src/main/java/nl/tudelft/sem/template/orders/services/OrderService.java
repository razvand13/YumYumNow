package nl.tudelft.sem.template.orders.services;

import nl.tudelft.sem.template.orders.domain.IOrderService;
import nl.tudelft.sem.template.orders.entities.Order;
import nl.tudelft.sem.template.orders.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService implements IOrderService {
    OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order findById(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(IllegalArgumentException::new);
    }
}
