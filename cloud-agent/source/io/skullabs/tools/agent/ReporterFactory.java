package io.skullabs.tools.agent;

import io.skullabs.tools.agent.commons.Config;
import io.skullabs.tools.agent.metrics.MetricRegistry;
import io.skullabs.tools.agent.metrics.ScheduledReporter;

/**
 *
 */
public interface ReporterFactory {

	ScheduledReporter create(MetricRegistry registry, Config config );
}
