package io.skullabs.tools.agent.metrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import static io.skullabs.tools.agent.commons.Lang.divide;
import static io.skullabs.tools.agent.commons.Lang.multiply;

/**
 * Basic meter metricManager. It makes implies in lower memory footprint
 * than Codahale's MetricManager, but it has less features too.
 */
@RequiredArgsConstructor
public class FairMeter implements ReadOnlyData<FairMeter.MeterData> {

	final AtomicLong counter = new AtomicLong(0l);
	final AtomicLong startTime = new AtomicLong(System.nanoTime());

	@Getter
	final String name;

	public MeterData getData(){
		long elapsedTime = System.nanoTime();
		return new MeterData( name,
			counter.getAndSet( 0 ),
			elapsedTime - startTime.getAndSet( elapsedTime )
		);
	}

	public void mark() {
		for (;;) {
			long current = get();
			long next = current + 1;
			if (compareAndSet(current, next))
				return;
		}
	}

	private long get() {
		return counter.get();
	}

	/**
	 * Fair compareAndSet based on Dave Dice, Danny Hendler and Ilya Mirsky research.
	 *
	 * @implNote http://arxiv.org/abs/1305.5800
	 * @param current
	 * @param next
	 * @return true if could set the new value
	 */
	private boolean compareAndSet(final long current, final long next) {
		if (counter.compareAndSet(current, next)) {
			return true;
		} else {
			LockSupport.parkNanos(1);
			return false;
		}
	}

	@Getter
	@Accessors(fluent = true)
	@RequiredArgsConstructor
	public static class MeterData {

		private static final double NS_SEC_RATE = 0.000000001d;

		final String name;
		final long counter;
		final long elapsedTime;

		public final double rate(){
			return divide( counter, multiply( elapsedTime, NS_SEC_RATE ) );
		}
	}
}
