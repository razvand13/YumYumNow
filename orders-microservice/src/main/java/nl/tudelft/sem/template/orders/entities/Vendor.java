package nl.tudelft.sem.template.orders.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Vendor
 */

@Entity
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID ID;

    @Valid
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL)
    private List<@Valid DishEntity> dishes;

    /**
     * Get ID
     * @return ID
     */
    @Valid
    @Schema(name = "ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("ID")
    public UUID getID() {
        return ID;
    }

    public void setID(UUID ID) {
        this.ID = ID;
    }

    public Vendor addDishesItem(DishEntity dishesItem) {
        if (this.dishes == null) {
            this.dishes = new ArrayList<>();
        }
        this.dishes.add(dishesItem);
        return this;
    }

    /**
     * Get dishes
     * @return dishes
     */
    @Valid
    @Schema(name = "dishes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("dishes")
    public List<@Valid DishEntity> getDishes() {
        return dishes;
    }

    public void setDishes(List<@Valid DishEntity> dishes) {
        this.dishes = dishes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vendor vendor = (Vendor) o;
        return Objects.equals(this.ID, vendor.ID) &&
                Objects.equals(this.dishes, vendor.dishes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, dishes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Vendor {\n");
        sb.append("    ID: ").append(toIndentedString(ID)).append("\n");
        sb.append("    dishes: ").append(toIndentedString(dishes)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

