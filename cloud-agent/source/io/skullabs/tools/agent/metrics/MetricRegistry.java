package io.skullabs.tools.agent.metrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static io.skullabs.tools.agent.commons.Lang.convert;

/**
 * A registry for metrics.
 */
public class MetricRegistry {

	final ConcurrentHashMap<String, FairMeter> meters = new ConcurrentHashMap<>();
	final ConcurrentHashMap<String, Gauge> gauges = new ConcurrentHashMap<>();

	public MetricRegistry register(String key, Supplier<Double> supplier ) {
		gauges.put( key, new Gauge( key, supplier ) );
		return this;
	}

	public FairMeter meter( String key ) {
		FairMeter newMeter = new FairMeter( key);
		FairMeter foundMeter = meters.getOrDefault(key, newMeter);
		if ( newMeter == foundMeter )
			foundMeter = meters.putIfAbsent( key, foundMeter );
		if ( foundMeter == null )
			foundMeter = newMeter;
		return foundMeter;
	}

	public Snapshot takeSnapshot(){
		return new Snapshot(
			convert( meters.values(), MetricRegistry::extractReadOnlyData ),
			convert( gauges.values(), MetricRegistry::extractReadOnlyData )
		);
	}

	private static <T> T extractReadOnlyData( ReadOnlyData<T> data ) {
		return data.getData();
	}

	@Getter
	@Accessors( fluent = true )
	@RequiredArgsConstructor
	public static class Snapshot {
		final Iterable<FairMeter.MeterData> meters;
		final Iterable<Gauge.GaugeData> gauges;
	}
}
