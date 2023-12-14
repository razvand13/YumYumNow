package nl.tudelft.sem.template.orders.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Order
 */

@Entity
@Table(name = "CUSTOMER_ORDER")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID ID;

    @Valid
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    private List<@Valid Dish> dishes;

    @ManyToOne
    private Address location;

    private String specialRequirements;

    private Status status;

    private Double totalPrice;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime orderTime;

    private UUID vendorId;

    private UUID customerId;

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

    public Order addDishesItem(Dish dishesItem) {
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
    public List<@Valid Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<@Valid Dish> dishes) {
        this.dishes = dishes;
    }

    /**
     * Get location
     * @return location
     */
    @Valid
    @Schema(name = "location", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("location")
    public Address getLocation() {
        return location;
    }

    public void setLocation(Address location) {
        this.location = location;
    }

    /**
     * Get specialRequirements
     * @return specialRequirements
     */

    @Schema(name = "specialRequirements", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("specialRequirements")
    public String getSpecialRequirements() {
        return specialRequirements;
    }

    public void setSpecialRequirements(String specialRequirements) {
        this.specialRequirements = specialRequirements;
    }

    /**
     * Get status
     * @return status
     */
    @Valid
    @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("status")
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Get totalPrice
     * @return totalPrice
     */

    @Schema(name = "totalPrice", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("totalPrice")
    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * The timestamp when the order was created.
     * @return orderTime
     */
    @Valid
    @Schema(name = "orderTime", description = "The timestamp when the order was created.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("orderTime")
    public OffsetDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(OffsetDateTime orderTime) {
        this.orderTime = orderTime;
    }

    /**
     * Get vendorId
     * @return vendorId
     */
    @Valid
    @Schema(name = "vendorId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("vendorId")
    public UUID getVendorId() {
        return vendorId;
    }

    public void setVendorId(UUID vendorId) {
        this.vendorId = vendorId;
    }

    /**
     * Get customerId
     * @return customerId
     */
    @Valid
    @Schema(name = "customerId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("customerId")
    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Order order = (Order) o;
        return Objects.equals(this.ID, order.ID) &&
                Objects.equals(this.dishes, order.dishes) &&
                Objects.equals(this.location, order.location) &&
                Objects.equals(this.specialRequirements, order.specialRequirements) &&
                Objects.equals(this.status, order.status) &&
                Objects.equals(this.totalPrice, order.totalPrice) &&
                Objects.equals(this.orderTime, order.orderTime) &&
                Objects.equals(this.vendorId, order.vendorId) &&
                Objects.equals(this.customerId, order.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, dishes, location, specialRequirements, status, totalPrice, orderTime, vendorId, customerId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Order {\n");
        sb.append("    ID: ").append(toIndentedString(ID)).append("\n");
        sb.append("    dishes: ").append(toIndentedString(dishes)).append("\n");
        sb.append("    location: ").append(toIndentedString(location)).append("\n");
        sb.append("    specialRequirements: ").append(toIndentedString(specialRequirements)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    totalPrice: ").append(toIndentedString(totalPrice)).append("\n");
        sb.append("    orderTime: ").append(toIndentedString(orderTime)).append("\n");
        sb.append("    vendorId: ").append(toIndentedString(vendorId)).append("\n");
        sb.append("    customerId: ").append(toIndentedString(customerId)).append("\n");
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

