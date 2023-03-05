package cloud.projects.library.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

public class AwsConfiguration {

	@Value("${app.aws.access-key}")
	private String accessKey;

	@Value("${app.aws.secret-key}")
	private String secretKey;

	@Value("${app.aws.region}")
	private String region;

	@Bean
	public AwsCredentials getAwsCredentials() {
		return AwsBasicCredentials.create(accessKey, secretKey);
	}

	@Bean
	public AwsCredentialsProvider getAwsCredentialsProvider(AwsCredentials credentials) {
		return StaticCredentialsProvider.create(credentials);
	}

	@Bean(destroyMethod = "close")
	public SqsClient getSqsClient(AwsCredentialsProvider provider) {
		return SqsClient.builder().region(Region.of(region)).credentialsProvider(provider).build();
	}

	@Bean(destroyMethod = "close")
	public S3Client getS3Client(AwsCredentialsProvider provider) {
		return S3Client.builder().region(Region.of(region)).credentialsProvider(provider).build();
	}

}
