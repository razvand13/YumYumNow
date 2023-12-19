package nl.tudelft.sem.template.orders.config;

import nl.tudelft.sem.template.orders.external.PaymentMock;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PaymentConfig {
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public PaymentMock singletonPayment() {
        return new PaymentMock();
    }
}
