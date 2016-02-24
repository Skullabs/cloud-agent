package io.skullabs.tools.agent.metrics.aws;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.model.*;
import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import io.skullabs.tools.agent.commons.Log;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.skullabs.tools.agent.commons.Lang.convert;
import static io.skullabs.tools.agent.commons.Lang.sum;

/**
 *
 */
public class AWSCloudWatchReporter extends ScheduledReporter {

	private static final double RATE_NS_MS = 0.000001;

	final String namespace;
	final Map<String, String> dimensions;
	final AmazonCloudWatchAsync cloudWatch;

	public AWSCloudWatchReporter(MetricRegistry registry, AmazonCloudWatchAsync cloudWatch, String namespace, final Map<String, String> dimensions ) {
		super(registry, "aws-cloud-watch-reporter", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
		this.namespace = namespace;
		this.dimensions = dimensions;
		this.cloudWatch = cloudWatch;
	}

	@Override
	public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
		try {
			final List<MetricDatum> data = new ArrayList<>(gauges.size()+timers.size());

			Log.info( "Registered "+gauges.size()+" gauge metrics." );
			for (Map.Entry<String, Gauge> gaugeEntry : gauges.entrySet())
				reportGauge(gaugeEntry, data);

			Log.info( "Registered "+timers.size()+" timer metrics." );
			for (Map.Entry<String, Timer> timerEntry : timers.entrySet())
				reportTimer(timerEntry, data);

			final List<Future<?>> cloudWatchFutures = new ArrayList<>();
			final Iterable<List<MetricDatum>> partitionedData = partition(data, 20);
			sendDataToCloudWatch(cloudWatchFutures, partitionedData);
			waitForCloudWatchResponses(cloudWatchFutures);
		} catch( Throwable t ) {
			t.printStackTrace();
		}
	}

	private void reportGauge(Map.Entry<String, Gauge> gaugeEntry, List<MetricDatum> data) {
		Gauge gauge = gaugeEntry.getValue();
		Object value = gauge.getValue();

		if ( value != null ){
			double number = Double.valueOf( value.toString() );
			data.add( createMetricEntry( gaugeEntry.getKey(), number ) );
		}
	}

	private void reportTimer(Map.Entry<String, Timer> timerEntry, List<MetricDatum> data) {
		final Timer timer = timerEntry.getValue();
		final Snapshot snapshot = timer.getSnapshot();
		final String key = timerEntry.getKey();
		data.add( createStatsMetricEntry( key, snapshot, RATE_NS_MS ) );
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

	private MetricDatum createMetricEntry( String key, Double value ) {
		return applyDimensionsIntoMetric( new MetricDatum()
				.withMetricName(key)
				.withTimestamp(new Date())
				.withValue(value)
		);
	}

	private MetricDatum createStatsMetricEntry( String key, Snapshot snapshot, double rescale ) {
		return applyDimensionsIntoMetric(new MetricDatum()
				.withMetricName(key)
				.withTimestamp(new Date()))
				.withStatisticValues(
					new StatisticSet()
						.withSum( sum( snapshot.getValues() ) * rescale )
						.withSampleCount((double) snapshot.size())
						.withMinimum((double) snapshot.getMin() * rescale)
						.withMaximum((double) snapshot.getMax() * rescale)
				);
	}

	private MetricDatum applyDimensionsIntoMetric( MetricDatum metricDatum ){
		if ( dimensions.size() > 0 ) {
			List<Dimension> newDimensions = convert(
					dimensions.entrySet(),
					e -> new Dimension()
							.withName(e.getKey())
							.withValue(e.getValue()));
			metricDatum.withDimensions(newDimensions);
		}
		return metricDatum;
	}

	private void sendDataToCloudWatch( List<Future<?>> cloudWatchFutures, Iterable<List<MetricDatum>> partitionedData ){
		for (List<MetricDatum> dataSubset : partitionedData) {
			PutMetricDataRequest request = new PutMetricDataRequest()
					.withNamespace(namespace)
					.withMetricData(dataSubset);
			Log.info( "Sending " + request );
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
