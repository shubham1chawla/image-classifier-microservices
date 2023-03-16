package cloud.projects.library.integration;

import java.util.Objects;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import lombok.val;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

public abstract class AbstractReceiveMessageResponseMessageHandler implements MessageHandler {

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		if (!Objects.isNull(message) && message.getPayload() instanceof ReceiveMessageResponse) {

			// extracting response from the message
			val response = (ReceiveMessageResponse) message.getPayload();

			// handling ReceiveMessageResponse instance
			handleReceiveMessageResponse(response);

			// deleting the message from the queue
			response.messages().stream().map(m -> DeleteMessageRequest.builder().queueUrl(getQueueUrl())
					.receiptHandle(m.receiptHandle()).build()).forEach(req -> getSqsClient().deleteMessage(req));

		}
	}

	protected abstract void handleReceiveMessageResponse(ReceiveMessageResponse response);

	protected abstract String getQueueUrl();

	protected abstract SqsClient getSqsClient();

}
