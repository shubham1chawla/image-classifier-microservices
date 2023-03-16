package cloud.projects.library.integration;

import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import lombok.val;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

public abstract class AbstractReceiveMessageResponseMessageSource implements MessageSource<ReceiveMessageResponse> {

	@Override
	public Message<ReceiveMessageResponse> receive() {

		// receiving response from SQS queue
		val response = getSqsClient().receiveMessage(ReceiveMessageRequest.builder().queueUrl(getQueueUrl()).build());

		// publishing response as message
		return response.hasMessages() ? new GenericMessage<>(response) : null;

	}

	protected abstract String getQueueUrl();

	protected abstract SqsClient getSqsClient();

}
