package nl.tudelft.sem.template.orders.entities.status;

import java.util.UUID;
import nl.tudelft.sem.template.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<Status, UUID> {

}
