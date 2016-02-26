package io.skullabs.tools.agent;

import io.skullabs.tools.agent.metrics.FairMeter.MeterData;
import io.skullabs.tools.agent.metrics.Gauge.GaugeData;
import io.skullabs.tools.agent.metrics.MetricRegistry.Snapshot;
import io.skullabs.tools.agent.metrics.ScheduledReporter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class AbstractReporter<T> implements ScheduledReporter {

	@Override
	public void report(Snapshot snapshot) {
		List<T> reportData = new ArrayList<>();
		onReport( snapshot, reportData );
		reportMeters( snapshot.meters(), reportData );
		reportGauges( snapshot.gauges(), reportData );
		sendReport( reportData );
	}

	protected void onReport(Snapshot snapshot, List<T> reportData) {}

	protected abstract void reportMeters(Iterable<MeterData> meters, List<T> reportData);

	protected abstract void reportGauges(Iterable<GaugeData> gauges, List<T> reportData);

	protected abstract void sendReport(List<T> reportData);
}
