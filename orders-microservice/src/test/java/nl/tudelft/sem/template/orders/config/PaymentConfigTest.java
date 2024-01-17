package nl.tudelft.sem.template.orders.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PaymentConfigTest {
    private PaymentConfig paymentConfig;

    @Test
    void singletonPayment() {
        this.paymentConfig = new PaymentConfig();
        assertNotNull(paymentConfig.singletonPayment());
    }
}
