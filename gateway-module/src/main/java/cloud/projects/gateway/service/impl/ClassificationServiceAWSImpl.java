package cloud.projects.gateway.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import cloud.projects.gateway.concurrent.ClassifierResultPoller;
import cloud.projects.library.dto.ClassificationResult;
import cloud.projects.library.exception.ClassificationException;
import cloud.projects.library.service.ClassificationService;
import lombok.val;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Log4j2
@Service
public class ClassificationServiceAWSImpl implements ClassificationService {

	private static final long TIME_OUT_VALUE = 100;
	private static final TimeUnit TIME_OUT_UNIT = TimeUnit.MILLISECONDS;

	private Map<String, ClassificationResult> cache;
	private ScheduledExecutorService executor;

	@Value("${app.aws.sqs.req-queue-url}")
	private String reqQueueUrl;

	@Value("${app.aws.sqs.res-queue-url}")
	private String resQueueUrl;

	@Value("${app.aws.s3.input-bucket}")
	private String inputBucket;

	@Value("${app.aws.s3.output-bucket}")
	private String outputBucket;

	@Autowired
	private SqsClient sqsClient;

	@Autowired
	private S3Client s3Client;

	@Autowired
	private ClassifierResultPoller poller;

	@PostConstruct
	public void postConstruct() {
		cache = new ConcurrentHashMap<>();
		executor = new ScheduledThreadPoolExecutor(1);

		// starting poller to consume results
		poller.addConsumer(result -> cache.put(result.getKey(), result));
		executor.scheduleAtFixedRate(poller, 0, TIME_OUT_VALUE, TIME_OUT_UNIT);
	}

	@PreDestroy
	public void preDestroy() {
		cache.clear();
		executor.shutdown();
	}

	@Override
	public ClassificationResult classify(String key, File image) {

		// checking if results are already present in cache
		if (cache.containsKey(key)) {
			log.info("Loading results from the cache for key: {}", key);
			return cache.get(key);
		}
		log.info("Classifying key: {}", key);

		// uploading file to S3
		uploadImage(key, image);

		log.info("Publishing request for {} to SQS...", key);
		sqsClient.sendMessage(SendMessageRequest.builder().queueUrl(reqQueueUrl).messageBody(key).build());

		// waiting for response from response queue
		while (!cache.containsKey(key)) {
			try {
				Thread.sleep(TIME_OUT_VALUE);
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				if (ex instanceof InterruptedException) {
					Thread.currentThread().interrupt();
				}
			}
		}
		return cache.get(key);
	}

	private void uploadImage(String key, File image) {
		log.info("Uploading image...");

		// checking if image contains valid bytes
		val bytes = new byte[(int) image.length()];
		try (val stream = new FileInputStream(image)) {
			stream.read(bytes);
		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
			throw new ClassificationException(ex);
		}

		// uploading image to S3 input bucket
		val request = PutObjectRequest.builder() //
				.bucket(inputBucket) //
				.key(key) //
				.contentType(MediaType.IMAGE_JPEG_VALUE) //
				.contentLength((long) bytes.length) //
				.build();
		s3Client.putObject(request, RequestBody.fromBytes(bytes));

		log.info("Image uploaded to S3 input bucket");
	}

}
