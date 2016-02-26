package io.skullabs.tools.agent.metrics;

/**
 *
 */
public interface ScheduledReporter {

	void report( MetricRegistry.Snapshot snapshot );
}
