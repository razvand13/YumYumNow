package nl.tudelft.sem.template.orders.repositories;

import java.util.UUID;
import nl.tudelft.sem.template.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

}
