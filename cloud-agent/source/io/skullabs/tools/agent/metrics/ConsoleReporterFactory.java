package io.skullabs.tools.agent.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import io.skullabs.tools.agent.commons.Config;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ConsoleReporterFactory implements ReporterFactory {

	@Override
	public ScheduledReporter create(MetricRegistry registry, Config config) {
		return ConsoleReporter.forRegistry( registry )
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.build();
	}
}
