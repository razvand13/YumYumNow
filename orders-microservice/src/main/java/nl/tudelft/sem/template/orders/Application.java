package nl.tudelft.sem.template.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**.
 * Example microservice application!
 */
@SpringBootApplication(scanBasePackages = "nl.tudelft.sem.template.orders")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
