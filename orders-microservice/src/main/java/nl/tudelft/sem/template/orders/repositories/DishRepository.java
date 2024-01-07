package nl.tudelft.sem.template.orders.repositories;

import nl.tudelft.sem.template.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DishRepository extends JpaRepository<Dish, UUID> {
    @Query("SELECT d FROM Dish d WHERE d.vendorId = :vendorId")
    List<Dish> getDishesByVendorId(@Param("vendorId") UUID vendorId);

}

