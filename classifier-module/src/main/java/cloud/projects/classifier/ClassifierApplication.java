package cloud.projects.classifier;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import cloud.projects.classifier.concurrent.ClassifierRequestPoller;
import cloud.projects.library.configuration.AwsConfiguration;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootApplication
@Import(AwsConfiguration.class)
public class ClassifierApplication implements CommandLineRunner {

	private static final int TIME_OUT_SEC = 5;

	private ScheduledExecutorService executor;

	@Autowired
	private ClassifierRequestPoller poller;

	@PostConstruct
	public void setup() {
		executor = new ScheduledThreadPoolExecutor(1);
	}

	@PreDestroy
	public void cleanup() {
		executor.shutdown();
	}

	public static void main(String... args) {
		SpringApplication.run(ClassifierApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Starting poller...");
		executor.scheduleAtFixedRate(poller, 0, TIME_OUT_SEC, TimeUnit.SECONDS);
	}

}
