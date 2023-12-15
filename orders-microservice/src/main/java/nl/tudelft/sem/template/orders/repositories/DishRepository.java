package nl.tudelft.sem.template.orders.repositories;

import nl.tudelft.sem.template.orders.entities.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DishRepository extends JpaRepository<Dish, UUID> {

}
