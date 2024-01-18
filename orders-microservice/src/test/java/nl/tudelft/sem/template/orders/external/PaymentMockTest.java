package nl.tudelft.sem.template.orders.external;

import nl.tudelft.sem.template.model.PayOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PaymentMockTest {
    private PaymentMock paymentMock;
    private PayOrderRequest payOrderRequest;
    private PaymentSuccessDecider paymentSuccessDecider;

    private UUID orderId;

    @BeforeEach
    void setup() {
        this.paymentMock = new PaymentMock();
        this.payOrderRequest = new PayOrderRequest();
        paymentSuccessDecider = mock(PaymentSuccessDecider.class);
        paymentMock.setPaymentSuccessDecider(paymentSuccessDecider);
        this.orderId = UUID.randomUUID();
    }

    @Test
    void testPayReturnsTrue() {
        when(paymentSuccessDecider.doesPaymentSucceed(orderId)).thenReturn(true);
        assertThat(paymentMock.pay(orderId, payOrderRequest)).isTrue();
    }

    @Test
    void testPayReturnsFalse() {
        when(paymentSuccessDecider.doesPaymentSucceed(orderId)).thenReturn(false);
        assertThat(paymentMock.pay(orderId, payOrderRequest)).isFalse();
    }
}
