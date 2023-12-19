package nl.tudelft.sem.template.orders.external;

import java.util.UUID;

/**
 * This class is added to aid in mocking of payment for testing.
 *
 * <p>By default, this class returns true for any passed UUID. However, by mocking this class and passing it into
 * PaymentMock, you can decide if a payment request should succeed. This is useful for example in testing
 * our API call to pay for an order, as mocking this allows for testing of logic for a failed payment</p>
 */
public class PaymentSuccessDecider {
    public boolean doesPaymentSucceed(UUID orderID) {
        return true;
    }
}
