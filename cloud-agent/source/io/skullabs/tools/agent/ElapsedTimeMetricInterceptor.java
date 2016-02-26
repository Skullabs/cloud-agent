package io.skullabs.tools.agent;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import static io.skullabs.tools.agent.ElapsedTimeMetricInterceptorConfig.registerMetrics;

/**
 *
 */
@RequiredArgsConstructor
public class ElapsedTimeMetricInterceptor {

	@RuntimeType
	static public Object intercept(
			@SuperCall Callable<?> callable,
			@AllArguments Object[] allArguments,
			@Origin Method method,
			@Origin Class<?> clazz ) throws Exception
	{
		try {
			return callable.call();
		} finally {
			registerMetrics( method, clazz );
		}
	}
}
