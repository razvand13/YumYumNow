package nl.tudelft.sem.template.orders.config;

import nl.tudelft.sem.template.orders.validator.UserAuthorizationValidator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

@Configuration
public class UserAuthorizationValidatorConfig {
    @Bean
    @Primary
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public UserAuthorizationValidator getAuthValidator() {
        return new UserAuthorizationValidator();
    }
}
