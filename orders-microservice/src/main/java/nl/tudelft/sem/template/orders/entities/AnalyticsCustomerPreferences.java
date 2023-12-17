package nl.tudelft.sem.template.orders.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Embeddable;

/**
 * Customer-specific preferences based on most frequently ordered dishes
 */

@Embeddable
public class AnalyticsCustomerPreferences {

    private UUID customerId;

    private List<DishEntity> mostOrderedDishes;

    private Analytics analyticsEntity;

    /**
    * The unique identifier of the customer.
    * @return customerId
    */
    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public AnalyticsCustomerPreferences addMostOrderedDishesItem(DishEntity mostOrderedDishesItem) {
        if (this.mostOrderedDishes == null) {
            this.mostOrderedDishes = new ArrayList<>();
        }
        this.mostOrderedDishes.add(mostOrderedDishesItem);
        return this;
    }

    /**
    * Most frequently ordered dishes by the specific customer.
    * @return mostOrderedDishes
    */
    public List<DishEntity> getMostOrderedDishes() {
        return mostOrderedDishes;
    }

    public void setMostOrderedDishes(List<DishEntity> mostOrderedDishes) {
        this.mostOrderedDishes = mostOrderedDishes;
    }

    public Analytics getAnalyticsEntity() {
        return analyticsEntity;
    }

    public void setAnalyticsEntity(Analytics analyticsEntity) {
        this.analyticsEntity = analyticsEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnalyticsCustomerPreferences analyticsCustomerPreferences = (AnalyticsCustomerPreferences) o;
        return Objects.equals(this.customerId, analyticsCustomerPreferences.customerId) &&
              Objects.equals(this.mostOrderedDishes, analyticsCustomerPreferences.mostOrderedDishes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, mostOrderedDishes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AnalyticsCustomerPreferencesInner {\n");
        sb.append("    customerId: ").append(toIndentedString(customerId)).append("\n");
        sb.append("    mostOrderedDishes: ").append(toIndentedString(mostOrderedDishes)).append("\n");
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

