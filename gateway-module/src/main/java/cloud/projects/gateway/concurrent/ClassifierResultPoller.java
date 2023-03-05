package cloud.projects.gateway.concurrent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cloud.projects.library.dto.ClassificationResult;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Log4j2
@Component
public class ClassifierResultPoller implements Runnable {

	@Value("${app.aws.sqs.res-queue-url}")
	private String resQueueUrl;

	@Value("${app.aws.s3.output-bucket}")
	private String outputBucket;

	@Autowired
	private SqsClient sqsClient;

	@Autowired
	private S3Client s3Client;

	private List<Consumer<ClassificationResult>> consumers = new LinkedList<>();

	@Override
	public void run() {

		// getting key from the response queue
		val response = sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(resQueueUrl).build());
		if (!response.hasMessages()) {
			return;
		}
		val key = response.messages().get(0).body();
		log.info("Received acknowledgement for key: {}", key);

		// getting classification results from S3
		String result = null;
		try {
			val object = s3Client.getObject(GetObjectRequest.builder().bucket(outputBucket).key(key).build());
			result = new String(object.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
		}

		// deleting object from bucket
		s3Client.deleteObject(DeleteObjectRequest.builder().bucket(outputBucket).key(key).build());

		// deleting key from response queue
		response.messages().stream()
				.map(m -> DeleteMessageRequest.builder().queueUrl(resQueueUrl).receiptHandle(m.receiptHandle()).build())
				.forEach(sqsClient::deleteMessage);

		// publishing results
		publish(new ClassificationResult(key, result));
	}

	public void addConsumer(Consumer<ClassificationResult> consumer) {
		consumers.add(consumer);
	}

	private void publish(ClassificationResult result) {
		consumers.forEach(c -> c.accept(result));
	}

}
