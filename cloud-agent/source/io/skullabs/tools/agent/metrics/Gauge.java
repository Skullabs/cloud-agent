package io.skullabs.tools.agent.metrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

/**
 *
 */
@RequiredArgsConstructor
public class Gauge implements ReadOnlyData<Gauge.GaugeData> {

	final String name;
	final Supplier<Double> supplier;

	private double getSuppliedData(){
		return supplier.get();
	}

	@Override
	public GaugeData getData() {
		return new GaugeData(
			name, getSuppliedData()
		);
	}

	@Getter
	@Accessors(fluent = true)
	@RequiredArgsConstructor
	public static class GaugeData {
		final String name;
		final double value;
	}
}
