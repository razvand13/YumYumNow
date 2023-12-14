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

@Embeddable
public class AnalyticsPopularItems {

  private UUID itemId;

  private String itemName;

  private Integer orderCount;

  private Analytics analyticsEntity;

  /**
   * Get itemId
   * @return itemId
  */

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
    AnalyticsPopularItems analyticsPopularItems = (AnalyticsPopularItems) o;
    return Objects.equals(this.itemId, analyticsPopularItems.itemId) &&
        Objects.equals(this.itemName, analyticsPopularItems.itemName) &&
        Objects.equals(this.orderCount, analyticsPopularItems.orderCount);
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

