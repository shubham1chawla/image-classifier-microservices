package cloud.projects.library.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import cloud.projects.library.integration.AbstractReceiveMessageResponseMessageHandler;
import cloud.projects.library.integration.AbstractReceiveMessageResponseMessageSource;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Configuration
@EnableIntegration
public class IntegrationConfiguration {

	private static final String SQS_QUEUE_CHANNEL = "sqsQueueChannel";

	@Bean(name = SQS_QUEUE_CHANNEL)
	public MessageChannel getSqsQueueChannel() {
		return new DirectChannel();
	}

	@Bean
	@InboundChannelAdapter(channel = SQS_QUEUE_CHANNEL, poller = @Poller(fixedDelay = "1000"))
	public MessageSource<ReceiveMessageResponse> getMessageSource(AbstractReceiveMessageResponseMessageSource source) {
		return source;
	}

	@Bean
	@ServiceActivator(inputChannel = SQS_QUEUE_CHANNEL)
	public MessageHandler getMessageHandler(AbstractReceiveMessageResponseMessageHandler handler) {
		return handler;
	}

}
