package nl.tudelft.sem.template.orders.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Customer-specific preferences based on most frequently ordered dishes
 */

@Schema(name = "Analytics_customerPreferences_inner", description = "Customer-specific preferences based on most frequently ordered dishes")
@JsonTypeName("Analytics_customerPreferences_inner")
@Entity
public class AnalyticsCustomerPreferencesInner {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private UUID customerId;

  @Valid
  @OneToMany(cascade = CascadeType.ALL)
  private List<@Valid DishEntity> mostOrderedDishes;

  @ManyToOne
  @JoinColumn(name = "analytics_entity_id")
  private AnalyticsEntity analyticsEntity;

  /**
   * The unique identifier of the customer
   * @return customerId
  */
  @Valid 
  @Schema(name = "customerId", description = "The unique identifier of the customer", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("customerId")
  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }

  public AnalyticsCustomerPreferencesInner addMostOrderedDishesItem(DishEntity mostOrderedDishesItem) {
    if (this.mostOrderedDishes == null) {
      this.mostOrderedDishes = new ArrayList<>();
    }
    this.mostOrderedDishes.add(mostOrderedDishesItem);
    return this;
  }

  /**
   * Most frequently ordered dishes by the specific customer
   * @return mostOrderedDishes
  */
  @Valid 
  @Schema(name = "mostOrderedDishes", description = "Most frequently ordered dishes by the specific customer", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("mostOrderedDishes")
  public List<@Valid DishEntity> getMostOrderedDishes() {
    return mostOrderedDishes;
  }

  public void setMostOrderedDishes(List<@Valid DishEntity> mostOrderedDishes) {
    this.mostOrderedDishes = mostOrderedDishes;
  }

  public AnalyticsEntity getAnalyticsEntity() {
    return analyticsEntity;
  }

  public void setAnalyticsEntity(AnalyticsEntity analyticsEntity) {
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
    AnalyticsCustomerPreferencesInner analyticsCustomerPreferencesInner = (AnalyticsCustomerPreferencesInner) o;
    return Objects.equals(this.customerId, analyticsCustomerPreferencesInner.customerId) &&
        Objects.equals(this.mostOrderedDishes, analyticsCustomerPreferencesInner.mostOrderedDishes);
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

