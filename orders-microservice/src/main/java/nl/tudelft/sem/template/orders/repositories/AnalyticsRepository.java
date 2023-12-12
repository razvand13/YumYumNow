package nl.tudelft.sem.template.orders.repositories;

import java.util.UUID;
import nl.tudelft.sem.template.model.Analytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsRepository extends JpaRepository<Analytics, UUID> {

}

