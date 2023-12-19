package nl.tudelft.sem.template.orders.external;

import nl.tudelft.sem.template.orders.entities.PayOrderRequest;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This Class mocks a theoretical payment microservice so that we can check the payment status of an order.
 *
 * <p>Using this class:
 * This class is configured as a Singleton Bean, so it may be used via dependency injection.
 * Declare usages as PaymentConfig.PaymentMock (variable name here).
 * Spring will ensure a single instance is kept across controllers.
 * This is necessary due to PaymentMock keeping a list of paid orders, so multiple instances would have their own lists.
 * IMPORTANT: DO NOT CREATE AN INSTANCE OF THIS CLASS YOURSELF (except in tests) USE DEPENDENCY INJECTION</p>
 */
@Component
public class PaymentMock {
    private final transient Set<UUID> paidOrders;
    private transient PaymentSuccessDecider paymentSuccessDecider;

    public PaymentMock() {
        this.paidOrders = new HashSet<UUID>();
        this.paymentSuccessDecider = new PaymentSuccessDecider();
    }

    /**
     * Request to mark an order as paid
     *
     * @param orderId UUID of order
     * @param payOrderRequest Object containing data about payment
     * @return true iff payment succeeds
     */
    public boolean pay(UUID orderId, PayOrderRequest payOrderRequest) {
        if (paymentSuccessDecider.doesPaymentSucceed(orderId)) {
            paidOrders.add(orderId);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if an order has been paid
     *
     * @param orderId UUID of order
     * @return true iff order has been marked as paid
     */
    public boolean isPaid(UUID orderId) {
        return paidOrders.contains(orderId);
    }

    /**
     * Method for swapping out the default PaymentSuccessDecider. Allows for passing in a mocked PaymentSuccessDecider
     * for testing purposes.
     *
     * @param paymentSuccessDecider PaymentSuccessDecider to use
     */
    public void setPaymentSuccessDecider(PaymentSuccessDecider paymentSuccessDecider) {
        this.paymentSuccessDecider = paymentSuccessDecider;
    }
}
