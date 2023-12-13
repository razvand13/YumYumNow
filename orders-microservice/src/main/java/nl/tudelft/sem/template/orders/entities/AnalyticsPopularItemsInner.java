package nl.tudelft.sem.template.orders.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

/**
 * List of popular items based on order frequency
 */

@Schema(name = "Analytics_popularItems_inner", description = "List of popular items based on order frequency")
@JsonTypeName("Analytics_popularItems_inner")
@Entity
public class AnalyticsPopularItemsInner {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private UUID itemId;

  private String itemName;

  private Integer orderCount;

  @ManyToOne
  @JoinColumn(name = "analytics_entity_id")
  private AnalyticsEntity analyticsEntity;

  /**
   * Get itemId
   * @return itemId
  */

  @Schema(name = "itemId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("itemId")
  public UUID getItemId() {
    return itemId;
  }

  public void setItemId(UUID itemId) {
    this.itemId = itemId;
  }

  /**
   * Get itemName
   * @return itemName
  */

  @Schema(name = "itemName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("itemName")
  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  /**
   * Get orderCount
   * @return orderCount
  */

  @Schema(name = "orderCount", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("orderCount")
  public Integer getOrderCount() {
    return orderCount;
  }

  public void setOrderCount(Integer orderCount) {
    this.orderCount = orderCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AnalyticsPopularItemsInner analyticsPopularItemsInner = (AnalyticsPopularItemsInner) o;
    return Objects.equals(this.itemId, analyticsPopularItemsInner.itemId) &&
        Objects.equals(this.itemName, analyticsPopularItemsInner.itemName) &&
        Objects.equals(this.orderCount, analyticsPopularItemsInner.orderCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemId, itemName, orderCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AnalyticsPopularItemsInner {\n");
    sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
    sb.append("    itemName: ").append(toIndentedString(itemName)).append("\n");
    sb.append("    orderCount: ").append(toIndentedString(orderCount)).append("\n");
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

