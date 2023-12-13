package nl.tudelft.sem.template.orders.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;
import java.util.UUID;

/**
 * Address.
 */

@Entity
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID Id;

    private Integer houseNumber;

    private String zip;

    private Double longitude;

    private Double latitude;

    /**
     * Get houseNumber
     * @return houseNumber
    */
    @Schema(name = "houseNumber", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("houseNumber")
    public Integer getHouseNumber() {
       return houseNumber;
    }

    public void setHouseNumber(Integer houseNumber) {
        this.houseNumber = houseNumber;
    }

    /**
     * Get zip
     * @return zip
    */

    @Schema(name = "zip", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("zip")
    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * Get longitude
     * @return longitude
    */

    @Schema(name = "longitude", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("longitude")
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
       this.longitude = longitude;
    }

    /**
     * Get latitude
     * @return latitude
    */
    @Schema(name = "latitude", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @JsonProperty("latitude")
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AddressEntity address = (AddressEntity) o;
        return Objects.equals(this.houseNumber, address.houseNumber)
                && Objects.equals(this.zip, address.zip)
                && Objects.equals(this.longitude, address.longitude)
                && Objects.equals(this.latitude, address.latitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(houseNumber, zip, longitude, latitude);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Address {\n");
        sb.append("    houseNumber: ").append(toIndentedString(houseNumber)).append("\n");
        sb.append("    zip: ").append(toIndentedString(zip)).append("\n");
        sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
        sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
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

