package io.skullabs.tools.agent.metrics.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClient;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import io.skullabs.tools.agent.commons.Config;
import io.skullabs.tools.agent.metrics.ReporterFactory;

/**
 *
 */
public class AWSCloudWatchReporterFactory implements ReporterFactory {

	@Override
	public ScheduledReporter create(MetricRegistry registry, Config config) {

		final BasicAWSCredentials credentials = new BasicAWSCredentials(
			config.getString("reporter.aws.access_key", null),
			config.getString("reporter.aws.security_key", null)
		);

		final String regionAsString = config.getString("reporter.aws.region", "US_EAST_1");
		final AmazonCloudWatchAsync cloudWatch = new AmazonCloudWatchAsyncClient( credentials );
		cloudWatch.setRegion( Region.getRegion( Regions.valueOf( regionAsString ) ) );

		return new AWSCloudWatchReporter(
			registry, cloudWatch,
			config.getString( "reporter.aws.namespace", "JVMCustomMetrics" ),
			config.getKeyMap( "reporter.aws.dimensions" ));
	}
}
