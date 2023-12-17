package nl.tudelft.sem.template.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**.
 * Example microservice application!
 */
@SpringBootApplication
@EnableJpaRepositories("nl.tudelft.sem.template.orders.repositories")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
