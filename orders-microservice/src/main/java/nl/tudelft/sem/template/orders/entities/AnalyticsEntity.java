package nl.tudelft.sem.template.orders.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Analytics
 */

@Entity
public class AnalyticsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID itemId;

    private Integer orderVolumes;

    @Valid
    @OneToMany(mappedBy = "analyticsEntity", cascade = CascadeType.ALL)
    private List<@Valid AnalyticsPopularItemsInner> popularItems;

    @Valid
    @ElementCollection
    private List<OffsetDateTime> peakOrderingTimes;

    @Valid
    @OneToMany(mappedBy = "analyticsEntity", cascade = CascadeType.ALL)
    private List<@Valid AnalyticsCustomerPreferencesInner> customerPreferences;


    /**
     * Total number of orders for the vendor
     * @return orderVolumes
     */

    @Schema(name = "orderVolumes", description = "Total number of orders for the vendor", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("orderVolumes")
    public Integer getOrderVolumes() {
        return orderVolumes;
    }

    public void setOrderVolumes(Integer orderVolumes) {
        this.orderVolumes = orderVolumes;
    }

    public AnalyticsEntity addPopularItemsItem(AnalyticsPopularItemsInner popularItemsItem) {
        if (this.popularItems == null) {
            this.popularItems = new ArrayList<>();
        }
        this.popularItems.add(popularItemsItem);
        return this;
    }

    /**
     * Get popularItems
     * @return popularItems
     */
    @Valid
    @Schema(name = "popularItems", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("popularItems")
    public List<@Valid AnalyticsPopularItemsInner> getPopularItems() {
        return popularItems;
    }

    public void setPopularItems(List<@Valid AnalyticsPopularItemsInner> popularItems) {
        this.popularItems = popularItems;
    }

    public AnalyticsEntity addPeakOrderingTimesItem(OffsetDateTime peakOrderingTimesItem) {
        if (this.peakOrderingTimes == null) {
            this.peakOrderingTimes = new ArrayList<>();
        }
        this.peakOrderingTimes.add(peakOrderingTimesItem);
        return this;
    }

    /**
     * Times when the highest number of orders are placed
     * @return peakOrderingTimes
     */
    @Valid
    @Schema(name = "peakOrderingTimes", description = "Times when the highest number of orders are placed", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("peakOrderingTimes")
    public List<OffsetDateTime> getPeakOrderingTimes() {
        return peakOrderingTimes;
    }

    public void setPeakOrderingTimes(List<OffsetDateTime> peakOrderingTimes) {
        this.peakOrderingTimes = peakOrderingTimes;
    }

    public AnalyticsEntity addCustomerPreferencesItem(AnalyticsCustomerPreferencesInner customerPreferencesItem) {
        if (this.customerPreferences == null) {
            this.customerPreferences = new ArrayList<>();
        }
        this.customerPreferences.add(customerPreferencesItem);
        return this;
    }

    /**
     * Get customerPreferences
     * @return customerPreferences
     */
    @Valid
    @Schema(name = "customerPreferences", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("customerPreferences")
    public List<@Valid AnalyticsCustomerPreferencesInner> getCustomerPreferences() {
        return customerPreferences;
    }

    public void setCustomerPreferences(List<@Valid AnalyticsCustomerPreferencesInner> customerPreferences) {
        this.customerPreferences = customerPreferences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnalyticsEntity analytics = (AnalyticsEntity) o;
        return Objects.equals(this.orderVolumes, analytics.orderVolumes) &&
                Objects.equals(this.popularItems, analytics.popularItems) &&
                Objects.equals(this.peakOrderingTimes, analytics.peakOrderingTimes) &&
                Objects.equals(this.customerPreferences, analytics.customerPreferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderVolumes, popularItems, peakOrderingTimes, customerPreferences);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Analytics {\n");
        sb.append("    orderVolumes: ").append(toIndentedString(orderVolumes)).append("\n");
        sb.append("    popularItems: ").append(toIndentedString(popularItems)).append("\n");
        sb.append("    peakOrderingTimes: ").append(toIndentedString(peakOrderingTimes)).append("\n");
        sb.append("    customerPreferences: ").append(toIndentedString(customerPreferences)).append("\n");
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

