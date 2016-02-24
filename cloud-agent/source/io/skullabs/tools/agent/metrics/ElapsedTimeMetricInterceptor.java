package io.skullabs.tools.agent.metrics;

import io.skullabs.tools.agent.commons.Config;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ElapsedTimeMetricInterceptor {

	final Metrics metrics;
	final boolean enableDetailedMethodMetrics;
	final boolean enableSummarizedMethodMetrics;

	public ElapsedTimeMetricInterceptor( Metrics metrics, Config config ) {
		this.metrics = metrics;
		enableDetailedMethodMetrics = config.getBoolean( "metrics.enabled.usage.methods.detailed" );
		enableSummarizedMethodMetrics = config.getBoolean( "metrics.enabled.usage.methods.summarized" );
	}

	@RuntimeType
	public Object intercept(
			@SuperCall Callable<?> callable,
			@AllArguments Object[] allArguments,
			@Origin Method method,
			@Origin Class<?> clazz ) throws Exception
	{
		long start = System.nanoTime();
		try {
			return callable.call();
		} finally {
			long elapsed = System.nanoTime() - start;
			registerMetrics( method, clazz, elapsed );
		}
	}

	private void registerMetrics( Method method, Class<?> clazz, long elapsed ){
		if ( enableDetailedMethodMetrics ) {
			String name = clazz.getCanonicalName() + "." + method.getName();
			metrics.timer("Method/" + name ).update(elapsed, TimeUnit.NANOSECONDS);
		}
		if ( enableSummarizedMethodMetrics )
			metrics.timer( "Requests" ).update( elapsed, TimeUnit.NANOSECONDS );
	}
}
