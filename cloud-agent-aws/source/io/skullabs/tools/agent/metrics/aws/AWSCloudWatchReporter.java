package io.skullabs.tools.agent.metrics.aws;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import io.skullabs.tools.agent.AbstractReporter;
import io.skullabs.tools.agent.commons.Log;
import io.skullabs.tools.agent.metrics.FairMeter.MeterData;
import io.skullabs.tools.agent.metrics.Gauge.GaugeData;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.Future;

import static io.skullabs.tools.agent.commons.Lang.convert;
import static io.skullabs.tools.agent.commons.Lang.isBlank;

/**
 *
 */
@RequiredArgsConstructor
public class AWSCloudWatchReporter extends AbstractReporter<MetricDatum> {

	final AmazonCloudWatchAsync cloudWatch;
	final String namespace;
	final Map<String, String> dimensions;

	@Override
	protected void reportMeters(Iterable<MeterData> meters, List<MetricDatum> reportData) {
		for (MeterData meter : meters) {
			reportData.add( createMetricEntry( meter.name(), "Counter", meter.counter() ) );
			reportData.add( createMetricEntry( meter.name(), "Rate", meter.rate() ) );
		}

	}

	@Override
	protected void reportGauges(Iterable<GaugeData> gauges, List<MetricDatum> reportData) {
		for (GaugeData gauge : gauges)
			reportData.add( createMetricEntry( gauge.name(), gauge.value() ) );
	}

	@Override
	protected void sendReport(List<MetricDatum> data) {
		try {
			final List<Future<?>> cloudWatchFutures = new ArrayList<>();
			final Iterable<List<MetricDatum>> partitionedData = partition(data, 20);
			sendDataToCloudWatch(cloudWatchFutures, partitionedData);
			waitForCloudWatchResponses(cloudWatchFutures);
		} catch( Throwable t ) {
			t.printStackTrace();
		}
	}

	private Iterable<List<MetricDatum>> partition(List<MetricDatum> data, int size) {
		final List<List<MetricDatum>> newList = new ArrayList<>();
		final Iterator<MetricDatum> iterator = data.iterator();
		List<MetricDatum> current;

		while ( iterator.hasNext() ) {
			newList.add( current = new ArrayList<>() );
			for (int i = 0; i < size && iterator.hasNext(); i++)
				current.add( iterator.next() );
		}

		return newList;
	}

	private MetricDatum createMetricEntry( String key, double value ) {
		return createMetricEntry( key, null, value );
	}

	private MetricDatum createMetricEntry( String key, String type, double value ) {
		return applyDimensionsIntoMetric( new MetricDatum()
				.withMetricName(key)
				.withTimestamp(new Date())
				.withValue(value), type
		);
	}

	private MetricDatum applyDimensionsIntoMetric( MetricDatum metricDatum, String type ){
		List<Dimension> newDimensions = convert( dimensions.entrySet(), this::createDimension);
		if (  !isBlank( type ) )
			newDimensions.add( createDimension( "MetricType", type ) );
		if ( newDimensions.size() > 0 ) {
			metricDatum.withDimensions(newDimensions);
		}
		return metricDatum;
	}

	private Dimension createDimension( Map.Entry<String,String> e ){
		return createDimension(e.getKey(), e.getValue());
	}

	private Dimension createDimension( String key, String value ){
		return new Dimension()
				.withName(key)
				.withValue(value);
	}

	private void sendDataToCloudWatch( List<Future<?>> cloudWatchFutures, Iterable<List<MetricDatum>> partitionedData ){
		for (List<MetricDatum> dataSubset : partitionedData) {
			PutMetricDataRequest request = new PutMetricDataRequest()
					.withNamespace(namespace)
					.withMetricData(dataSubset);
			cloudWatchFutures.add(cloudWatch.putMetricDataAsync( request ));
		}
	}

	private void waitForCloudWatchResponses( List<Future<?>> cloudWatchFutures ){
		for (Future<?> cloudWatchFuture : cloudWatchFutures) {
			try {
				cloudWatchFuture.get();
			} catch (Exception e) {
				e.printStackTrace();
				Log.info("Exception reporting metrics to CloudWatch. The data in this CloudWatch API request " +
						"may have been discarded, did not make it to CloudWatch.");
			}
		}
	}
}
