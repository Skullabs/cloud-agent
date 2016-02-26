package io.skullabs.tools.agent.metrics;

import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class MetricRegisterTest {

	static final int EXPECTED_RPS = 10;
	final ExecutorService executor = Executors.newFixedThreadPool( EXPECTED_RPS );

	@After
	public void shutdownExecutor(){
		executor.shutdownNow();
	}

	@Test
	@SneakyThrows
	public void ensureThatIsAbleToRetrieveTheMeterDataAsExpected(){
		final MetricRegistry registry = new MetricRegistry();

		for ( int i=0; i<EXPECTED_RPS; i++ )
			executor.submit(()-> registry.meter("test").mark());
		Thread.sleep( 1000l );

		FairMeter.MeterData savedMeter = first(registry.takeSnapshot().meters());
		assertEquals( 10, savedMeter.rate(), 0.9 );
	}

	@Test
	@SneakyThrows
	public void ensureThatIsAbleToRetrieveTheGaugeDataAsExpected(){
		final MetricRegistry registry = new MetricRegistry();
		final AtomicLong counter = new AtomicLong( 0l );
		registry.register( "test", ()-> (double)counter.get() );

		for ( int i=0; i<EXPECTED_RPS; i++ )
			counter.incrementAndGet();

		Gauge.GaugeData savedMeter = first(registry.takeSnapshot().gauges());
		assertEquals( 10, savedMeter.value(), 0 );
	}

	private static <T> T first( Iterable<T> iterable ){
		for ( T t : iterable )
			return t;
		return null;
	}
}
