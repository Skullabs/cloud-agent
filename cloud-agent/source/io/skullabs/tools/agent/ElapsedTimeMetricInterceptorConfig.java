package io.skullabs.tools.agent;

import io.skullabs.tools.agent.metrics.MetricRegistry;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Configurations for ElapsedTimeMetricInterceptor. It will be called
 * by the ElapsedTimeMetricInterceptor, once ByteBuddy makes instance copies
 * from this classes, once it act like a template for method instrumentation.
 */
public class ElapsedTimeMetricInterceptorConfig {

	static BiConsumer<Method, Class<?>> detailedMethodMarker;
	static Consumer<Method> methodSummarizedMark;

	/**
	 * Simulates JIT optimization, ensuring the best branch execution possible,
	 * avoiding 'if' conditions that could be possibly optimized latter.
	 *
	 * @param metricRegistry
	 * @param enableDetailedMethodMetrics
	 * @param enableSummarizedMethodMetrics
	 */
	static void initialize( MetricRegistry metricRegistry, boolean enableDetailedMethodMetrics, boolean enableSummarizedMethodMetrics )
	{
		detailedMethodMarker = enableDetailedMethodMetrics
			? new DetailedMethodMarker( metricRegistry )
			: new EmptyDetailedMethodMarker();

		methodSummarizedMark = enableSummarizedMethodMetrics
			? new MethodSummarizerMarker( metricRegistry )
			: new EmptyMethodSummarizerMarker();
	}

	static public void registerMetrics(Method method, Class<?> clazz ){
		detailedMethodMarker.accept( method, clazz );
		methodSummarizedMark.accept( method );
	}
}

@RequiredArgsConstructor
class DetailedMethodMarker implements BiConsumer<Method, Class<?>> {

	final MetricRegistry metricRegistry;

	@Override
	public void accept(Method method, Class<?> clazz) {
		final String name = clazz.getCanonicalName() + "." + method.getName();
		metricRegistry.meter("Method/" + name ).mark();
	}
}

@RequiredArgsConstructor
class EmptyDetailedMethodMarker implements BiConsumer<Method, Class<?>> {

	@Override
	public void accept(Method method, Class<?> aClass) {}
}

@RequiredArgsConstructor
class MethodSummarizerMarker implements Consumer<Method> {

	final MetricRegistry metricRegistry;

	@Override
	public void accept(Method method) {
		metricRegistry.meter( "Requests" ).mark();
	}
}

@RequiredArgsConstructor
class EmptyMethodSummarizerMarker implements Consumer<Method> {

	@Override
	public void accept(Method method) {

	}
}