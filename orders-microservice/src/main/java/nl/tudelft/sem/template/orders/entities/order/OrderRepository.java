package nl.tudelft.sem.template.orders.entities.order;

import java.util.UUID;
import nl.tudelft.sem.template.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

}
