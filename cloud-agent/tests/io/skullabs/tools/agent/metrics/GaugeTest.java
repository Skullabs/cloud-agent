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
public class GaugeTest {

	static final int EXPECTED_RPS = 10;

	@Test
	@SneakyThrows
	public void ensureThatIsAbleToRetrieveTheGaugeDataAsExpected(){
		final AtomicLong counter = new AtomicLong( 0l );
		final Gauge gauge = new Gauge("test", () -> (double) counter.get());

		for ( int i=0; i<EXPECTED_RPS; i++ )
			counter.incrementAndGet();

		assertEquals( 10, gauge.getData().value(), 0 );
	}
}
