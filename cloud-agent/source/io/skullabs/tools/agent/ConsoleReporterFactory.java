package io.skullabs.tools.agent;

import io.skullabs.tools.agent.commons.Config;
import io.skullabs.tools.agent.metrics.FairMeter.MeterData;
import io.skullabs.tools.agent.metrics.Gauge.GaugeData;
import io.skullabs.tools.agent.metrics.MetricRegistry;
import io.skullabs.tools.agent.metrics.MetricRegistry.Snapshot;
import io.skullabs.tools.agent.metrics.ScheduledReporter;

import java.util.Date;
import java.util.List;

import static io.skullabs.tools.agent.commons.Lang.println;
import static io.skullabs.tools.agent.commons.Lang.str;

/**
 * Prints the report to the console.
 */
public class ConsoleReporterFactory implements ReporterFactory {

	@Override
	public ScheduledReporter create(MetricRegistry registry, Config config) {
		return new ConsoleReporter();
	}

	public static class ConsoleReporter extends AbstractReporter<String> {

		@Override
		protected void onReport(Snapshot snapshot, List<String> reportData) {
			reportData.add( "------------------------------------------------------------------------" );
			reportData.add( str(" Report time: %s", new Date() ));
			reportData.add( "" );
		}

		@Override
		protected void reportMeters(Iterable<MeterData> meters, List<String> reportData) {
			for (final MeterData data : meters) {
				reportData.add( str(" Meter: %s", data.name()) );
				reportData.add( str("  -> counter: %13d", data.counter() ) );
				reportData.add( str("  -> rate (rps): %10.2f", data.rate() ) );
			}
			reportData.add( "" );
		}

		@Override
		protected void reportGauges(Iterable<GaugeData> gauges, List<String> reportData) {
			for (final GaugeData data : gauges) {
				reportData.add( str(" Gauge: %40s -> value: %5.2f", data.name(), data.value()) );
			}
			reportData.add( "" );
		}

		@Override
		protected void sendReport(List<String> reportData) {
			for ( String line : reportData )
				println( line );
		}
	}
}
