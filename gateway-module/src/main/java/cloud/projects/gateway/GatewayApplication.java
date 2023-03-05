package cloud.projects.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import cloud.projects.library.configuration.AwsConfiguration;

@SpringBootApplication
@Import(AwsConfiguration.class)
public class GatewayApplication {

	public static void main(String... args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
