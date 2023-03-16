package cloud.projects.classifier.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cloud.projects.library.integration.AbstractReceiveMessageResponseMessageSource;
import lombok.Getter;
import software.amazon.awssdk.services.sqs.SqsClient;

@Getter
@Component
public class ReceiveMessageResponseMessageSource extends AbstractReceiveMessageResponseMessageSource {

	@Value("${app.aws.sqs.req-queue-url}")
	private String queueUrl;

	@Autowired
	private SqsClient sqsClient;

}
