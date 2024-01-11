package nl.tudelft.sem.template.orders.config;

import nl.tudelft.sem.template.orders.validator.DataValidationField;
import nl.tudelft.sem.template.orders.validator.DataValidator;
import nl.tudelft.sem.template.orders.validator.UserAuthorizationValidator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import java.util.List;

@Configuration
public class DataValidatorConfig {
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DataValidator getDataValidator(List<DataValidationField> fields) {
        return new DataValidator(fields);
    }
}
