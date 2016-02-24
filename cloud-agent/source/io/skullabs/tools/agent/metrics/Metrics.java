package io.skullabs.tools.agent.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.skullabs.tools.agent.commons.Config;

import java.util.concurrent.TimeUnit;

import static io.skullabs.tools.agent.commons.Lang.asClass;
import static io.skullabs.tools.agent.commons.Lang.divide;

/**
 *
 */
public class Metrics {

	final MetricRegistry metrics = new MetricRegistry();
	final Config config;

	public Metrics( Config config ){
		this.config = config;
		createDefaultMetrics();
	}

	private void createDefaultMetrics(){
		final Runtime runtime = Runtime.getRuntime();
		if ( config.getBoolean( "metrics.enabled.application.mem.heap" ) )
			metrics.register( "HeapFreeMemory", (Gauge<Double>) ()-> asMB( runtime.freeMemory() ) );
		if ( config.getBoolean( "metrics.enabled.application.mem.total" ) )
			metrics.register( "HeapTotalMemory", (Gauge<Double>) ()-> asMB( runtime.totalMemory() ) );
		if ( config.getBoolean( "metrics.enabled.application.mem.usage" ) )
			metrics.register( "HeapUsagePercent", (Gauge<Double>) ()-> divide( runtime.freeMemory(), runtime.maxMemory() )*100 );
	}

	private static double asMB( double d ){
		return divide( d, (double)(1024*1024) );
	}

	public Timer timer( String name ) {
		return metrics.timer( name );
	}

	public void startCollectingData(){
		try {
			Class<ReporterFactory> clazz = asClass(config.getString("metrics.reporter", "ConsoleReporterFactory"));
			ReporterFactory factory = clazz.newInstance();
			factory.create( metrics, config )
				   .start( config.getInteger( "metrics.interval" ), TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw new RuntimeException( e );
		}
	}
}