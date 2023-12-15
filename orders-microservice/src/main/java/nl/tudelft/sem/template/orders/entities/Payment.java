package nl.tudelft.sem.template.orders.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets Payment
 */

public enum Payment {

    CREDITCARD("creditCard"),

    IDEAL("iDeal"),

    GOOGLEPAY("GooglePay"),

    APPLEPAY("ApplePay");

    private String value;

    Payment(String value) {
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
    public static Payment fromValue(String value) {
        for (Payment b : Payment.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

