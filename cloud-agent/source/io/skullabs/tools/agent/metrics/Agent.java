package io.skullabs.tools.agent.metrics;

import io.skullabs.tools.agent.commons.Lang;
import io.skullabs.tools.agent.commons.Log;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import io.skullabs.tools.agent.commons.Config;
import io.skullabs.tools.agent.commons.InvalidInterface;

import java.lang.instrument.Instrumentation;
import java.util.Collection;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class Agent {

	final static Config config = new Config();

    public static void premain(String agentArgs, Instrumentation inst) {
	    Log.info( "Starting Agent..." );

	    final Metrics metrics = new Metrics( config );
	    metrics.startCollectingData();

	    new AgentBuilder.Default()
		    .type(
			    ignoreJDKClasses().and( not( isInterface() ).and( not( isAbstract() ) ) )
				    .and( readInterfaces().or( readIncludes() ) )
		    )
		    .transform((builder, type, classLoader) -> {
			    Log.info( "Transforming " + type);
			    return builder
				    .method(any())
				    .intercept( MethodDelegation.to(new ElapsedTimeMetricInterceptor( metrics, config )) );
			    }
		    )
		    .installOn(inst);
    }

	public static ElementMatcher.Junction<NamedElement> ignoreJDKClasses(){
		return not( nameStartsWith("java.") ).and( not( nameStartsWith("sun.") ) );
	}

	public static ElementMatcher.Junction<TypeDescription> readInterfaces(){
		Collection<Class<Object>> interfaces = config.getStringList("interfaces", Lang::asClass);
		ElementMatcher.Junction<TypeDescription> matcher = isSubTypeOf( InvalidInterface.class );
		for ( Class<?> interfaceClazz : interfaces ){
			Log.info( "Classes implementing SuperInterface " + interfaceClazz + " marked to be instrumented" );
			matcher = matcher.or( isSubTypeOf( interfaceClazz ) );
		}
		return matcher;
	}

	public static ElementMatcher.Junction<NamedElement> readIncludes(){
		Collection<String> includes = config.getStringList("includes");
		ElementMatcher.Junction<NamedElement> named = nameStartsWith( "skull.tools.invalid" );
		for ( String next : includes ) {
			Log.info( "Package/Class marked to be instrumented: " + next );
			named = named.or(nameStartsWith( next ));
		}
		return named;
	}
}
