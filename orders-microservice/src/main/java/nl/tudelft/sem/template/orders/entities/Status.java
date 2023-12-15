package nl.tudelft.sem.template.orders.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets Status
 */

public enum Status {

    PENDING("pending"),

    ACCEPTED("accepted"),

    REJECTED("rejected"),

    PREPARING("preparing"),

    GIVENTOCOURIER("givenToCourier"),

    ONTRANSIT("onTransit"),

    DELIVERED("delivered");

    private String value;

    Status(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static Status fromValue(String value) {
        for (Status b : Status.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

