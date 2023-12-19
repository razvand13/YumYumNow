package nl.tudelft.sem.template.orders.repositories;

import nl.tudelft.sem.template.orders.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByVendorId(UUID vendorId);
}
