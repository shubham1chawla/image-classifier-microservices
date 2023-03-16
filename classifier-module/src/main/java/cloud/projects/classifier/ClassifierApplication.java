package cloud.projects.classifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import cloud.projects.library.configuration.AwsConfiguration;
import cloud.projects.library.configuration.IntegrationConfiguration;

@SpringBootApplication
@Import({ AwsConfiguration.class, IntegrationConfiguration.class })
public class ClassifierApplication {

	public static void main(String... args) {
		SpringApplication.run(ClassifierApplication.class, args);
	}

}
