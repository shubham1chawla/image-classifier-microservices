package cloud.projects.classifier.integration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import cloud.projects.library.integration.AbstractReceiveMessageResponseMessageHandler;
import cloud.projects.library.service.ClassificationService;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Log4j2
@Component
public class ReceiveMessageResponseMessageHandler extends AbstractReceiveMessageResponseMessageHandler {

	@Value("${app.download-dir}")
	private File directory;

	@Getter
	@Value("${app.aws.sqs.req-queue-url}")
	private String queueUrl;

	@Value("${app.aws.sqs.res-queue-url}")
	private String resQueueUrl;

	@Value("${app.aws.s3.input-bucket}")
	private String inputBucket;

	@Value("${app.aws.s3.output-bucket}")
	private String outputBucket;

	@Autowired
	private ClassificationService service;

	@Getter
	@Autowired
	private SqsClient sqsClient;

	@Autowired
	private S3Client s3Client;

	@PostConstruct
	public void setup() throws IOException {
		if (!directory.exists()) {
			Assert.state(directory.mkdir(), "Unable to create download directory!");
		}
		log.info("Image download directory: {}", directory.getAbsolutePath());
	}

	@Override
	protected void handleReceiveMessageResponse(ReceiveMessageResponse response) {

		// extracting the key
		val key = response.messages().get(0).body();
		log.info("Received key: {}", key);

		// downloading the image
		val image = downloadImage(key);
		log.info("Downloaded image: {}", image);

		// classifying the image
		val result = service.classify(key, image);
		log.info("Result: {}", result);

		// posting result to output bucket
		val request = PutObjectRequest.builder().bucket(outputBucket).key(key).build();
		s3Client.putObject(request, RequestBody.fromString(result.getResult()));

		// deleting image from input bucket
		s3Client.deleteObject(DeleteObjectRequest.builder().bucket(inputBucket).key(key).build());

		// deleting image from file system
		deleteImage(image);

		// publishing result acknowledgement
		sqsClient.sendMessage(SendMessageRequest.builder().queueUrl(resQueueUrl).messageBody(key).build());
		log.info("Results published!");

	}

	private File downloadImage(String key) {
		val image = new File(String.format("%s%s%s.JPEG", directory, File.separator, key));
		val object = s3Client.getObject(GetObjectRequest.builder().bucket(inputBucket).key(key).build());
		try {
			if (!image.exists()) {
				image.createNewFile();
			}
			try (val stream = new FileOutputStream(image.getAbsolutePath())) {
				stream.write(object.readAllBytes());
			}
			return image;
		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

	private void deleteImage(File image) {
		if (Objects.isNull(image)) {
			return;
		}
		try {
			Files.deleteIfExists(image.toPath());
		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
		}
	}

}
