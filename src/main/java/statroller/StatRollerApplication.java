package statroller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The REST service. Start with: gradlew bootRun
 * Web page: http://localhost:8090/   API page: http://localhost:8090/swagger-ui.html
 */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Stat Roller API", version = "1.0",
        description = "Rolls D&D 5e ability scores (4d6 drop lowest) and compares them to 27-point point buy."))
public class StatRollerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatRollerApplication.class, args);
    }
}
