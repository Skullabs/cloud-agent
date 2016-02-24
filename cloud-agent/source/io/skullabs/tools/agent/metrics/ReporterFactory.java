package io.skullabs.tools.agent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import io.skullabs.tools.agent.commons.Config;

/**
 *
 */
public interface ReporterFactory {

	ScheduledReporter create(MetricRegistry registry, Config config );
}
