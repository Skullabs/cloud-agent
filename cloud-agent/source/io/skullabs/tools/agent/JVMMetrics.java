package io.skullabs.tools.agent;

import io.skullabs.tools.agent.commons.Config;
import io.skullabs.tools.agent.commons.Log;
import io.skullabs.tools.agent.metrics.MetricRegistry;
import lombok.RequiredArgsConstructor;

import static io.skullabs.tools.agent.commons.Lang.divide;

/**
 *
 */
@RequiredArgsConstructor( staticName = "with" )
public class JVMMetrics {

	final MetricRegistry metrics;
	final Config config;

	public void load(){
		Log.info( "Initializing enabled JVM metrics..." );
		final Runtime runtime = Runtime.getRuntime();
		long usedMemory = runtime.maxMemory() - runtime.freeMemory();

		if ( config.getBoolean( "metrics.enabled.application.mem.heap" ) )
			metrics.register("UsedHeapMemory", () -> asMB( usedMemory ));
		if ( config.getBoolean( "metrics.enabled.application.mem.total" ) )
			metrics.register("TotalAllocatedHeapMemory", () -> asMB(runtime.totalMemory()));
		if ( config.getBoolean( "metrics.enabled.application.mem.max" ) )
			metrics.register("MaxHeapMemory", () -> asMB(runtime.maxMemory()));
		if ( config.getBoolean( "metrics.enabled.application.mem.usage" ) )
			metrics.register( "HeapUsagePercent", ()-> divide( usedMemory, runtime.maxMemory() )*100 );
	}

	private static double asMB( double d ){
		return divide( d, (double)(1024*1024) );
	}
}
