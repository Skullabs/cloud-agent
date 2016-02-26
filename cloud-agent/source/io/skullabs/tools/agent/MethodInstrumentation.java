package io.skullabs.tools.agent;

import io.skullabs.tools.agent.commons.*;
import io.skullabs.tools.agent.metrics.MetricRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.annotation.AnnotatedCodeElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.util.Collection;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Responsible for method instrumentation. It will wrap methods, gather
 * method execution metrics and store data into the {@link MetricRegistry}
 * instance.
 */
@Accessors(fluent = true)
@RequiredArgsConstructor( staticName = "with")
public class MethodInstrumentation {

	final MetricRegistry metrics;
	final Config config;

	@Getter(lazy = true)
	private final boolean enableDetailedMethodMetrics = config.getBoolean( "metrics.enabled.usage.methods.detailed" );

	@Getter(lazy = true)
	private final boolean enableSummarizedMethodMetrics = config.getBoolean( "metrics.enabled.usage.methods.summarized" );

	@Getter(lazy = true)
	private final ElementMatcher.Junction<NamedElement> ignoreClasses = getIgnoreClasses();

	@Getter(lazy = true)
	private final ElementMatcher.Junction<TypeDescription> readInterfaces = getReadInterfaces();

	@Getter(lazy = true)
	private final ElementMatcher.Junction<NamedElement> readIncludes = getReadIncludes();

	@Getter(lazy = true)
	private final ElementMatcher.Junction<Object> allValidClasses = getAllValidClasses();

	@Getter(lazy = true)
	private final ElementMatcher.Junction<MethodDescription> publicOnlyMethods = getPublicOnlyMethods();

	@Getter(lazy = true)
	private final ElementMatcher.Junction<AnnotatedCodeElement> annotatedMethods = getAnnotatedMethods();

	@Getter(lazy = true)
	private final ElementMatcher.Junction<Object> allValidMethods = getAllValidMethods();

	public void instrument( Instrumentation inst )
	{
		Log.info("Applying method instrumentations...");
		configureMetricInterceptor();

		new AgentBuilder.Default()
			.type(
				ignoreClasses().and( not( isInterface() ).and( not( isAbstract() ) ) )
					.and( readInterfaces().or( readIncludes() ).or( allValidClasses() ) )
			)
			.transform( this::instrumentMethods )
			.installOn(inst);
	}

	private void configureMetricInterceptor(){
		ElapsedTimeMetricInterceptorConfig.initialize(
				metrics, enableDetailedMethodMetrics(), enableSummarizedMethodMetrics()
		);
	}

	private DynamicType.Builder<?> instrumentMethods(DynamicType.Builder<?> builder, TypeDescription type, ClassLoader var3) {
		Log.info( "Transforming " + type);
		return builder
				.method( publicOnlyMethods().and( annotatedMethods().or( allValidMethods() ) ) )
				.intercept( MethodDelegation.to( ElapsedTimeMetricInterceptor.class ) );
	}

	private static ElementMatcher.Junction<NamedElement> getIgnoreClasses(){
		return not( nameStartsWith("java.") )
				.and( not( nameStartsWith("sun.") ) )
				.and( not( nameStartsWith("io.skullabs.tools.agent.") ) );
	}

	private ElementMatcher.Junction<TypeDescription> getReadInterfaces(){
		Collection<Class<Object>> interfaces = config.getStringList("includes.interfaces", Lang::asClass);
		ElementMatcher.Junction<TypeDescription> matcher = isSubTypeOf( InvalidInterface.class );
		for ( Class<?> interfaceClazz : interfaces ){
			Log.info( "Classes implementing SuperInterface " + interfaceClazz + " marked to be instrumented" );
			matcher = matcher.or( isSubTypeOf( interfaceClazz ) );
		}
		return matcher;
	}

	private ElementMatcher.Junction<NamedElement> getReadIncludes(){
		final Collection<String> includes = config.getStringList("includes.class.names");
		ElementMatcher.Junction<NamedElement> named = nameStartsWith( "skull.tools.invalid" );
		for ( String next : includes ) {
			Log.info( "Package/Class marked to be instrumented: " + next );
			named = named.or(nameStartsWith( next ));
		}
		return named;
	}

	private ElementMatcher.Junction<Object> getAllValidClasses(){
		boolean includeAllClasses = config.getBoolean("includes.class.all");
		return includeAllClasses ? any() : none();
	}

	private ElementMatcher.Junction<MethodDescription> getPublicOnlyMethods(){
		boolean publicOnlyMethods = config.getBoolean("includes.methods.public-only", true);
		return publicOnlyMethods ? isPublic()
				: isPublic().or( isPrivate() ).or( isProtected() ).or( isPackagePrivate() );
	}

	private ElementMatcher.Junction<AnnotatedCodeElement> getAnnotatedMethods(){
		final Collection<Class<? extends Annotation>> interfaces = config.getStringList("includes.methods.annotations", Lang::asClass);
		ElementMatcher.Junction<AnnotatedCodeElement> matcher = isAnnotatedWith(InvalidAnnotation.class);
		for ( Class<? extends Annotation> interfaceClazz : interfaces ){
			Log.info( "Methods annotated with " + interfaceClazz + " is marked to be instrumented" );
			matcher = matcher.or( isAnnotatedWith( interfaceClazz ) );
		}
		return matcher;
	}

	private ElementMatcher.Junction<Object> getAllValidMethods(){
		final Collection<String> foundAnnotatedMethods = config.getStringList("includes.methods.annotations");
		return foundAnnotatedMethods.isEmpty() ? any() : none();
	}
}
