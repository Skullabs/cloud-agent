package io.skullabs.tools.agent.metrics;

import lombok.SneakyThrows;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class FairMeterTest {

	static final int EXPECTED_RPS = 10;
	final ExecutorService executor = Executors.newSingleThreadExecutor();

	@Test
	@SneakyThrows
	public void ensureThatIsAbleToRetrieveTheMeterDataAsExpected(){
		final FairMeter meter = new FairMeter( "meter" );

		for ( int i=0; i<EXPECTED_RPS; i++ )
			executor.submit(meter::mark);

		Thread.sleep( 1000l );
		assertEquals( 10, meter.getData().rate(), 0.9 );
	}
}
