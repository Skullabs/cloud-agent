package io.skullabs.tools.agent;

import io.skullabs.tools.agent.commons.Config;
import io.skullabs.tools.agent.commons.Log;
import io.skullabs.tools.agent.metrics.MetricRegistry;
import io.skullabs.tools.agent.metrics.ScheduledReporter;
import lombok.RequiredArgsConstructor;

import java.lang.instrument.Instrumentation;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Agent {

	final static MetricRegistry metrics = new MetricRegistry();
	final static Config config = new Config();
	final static ScheduledExecutorService executor = Executors.newScheduledThreadPool( 1 );

    public static void premain(String agentArgs, Instrumentation inst) {
	    Log.info( "Starting Agent..." );
	    MethodInstrumentation.with( metrics, config ).instrument( inst );
		JVMMetrics.with( metrics, config ).load();
	    startCollectingData();
    }

	private static void startCollectingData(){
		try {
			Log.info("Start collecting data...");
			final ScheduledReporter reporter = loadReporter();
			final int interval = config.getInteger("metrics.interval", 10000);

			executor.scheduleAtFixedRate(
				new SafeReporter( ()-> reporter.report( metrics.takeSnapshot() ) ),
				interval, interval, TimeUnit.MILLISECONDS
			);
		} catch (Exception e) {
			throw new RuntimeException( e );
		}
	}

	private static ScheduledReporter loadReporter() throws IllegalAccessException, InstantiationException
	{
		final Class<ReporterFactory> clazz = (Class<ReporterFactory>) config.getClass("metrics.reporter", ConsoleReporterFactory.class);
		final ReporterFactory factory = clazz.newInstance();
		return factory.create(metrics, config);
	}
}

@RequiredArgsConstructor
class SafeReporter extends TimerTask {

	final Runnable runnable;

	@Override
	public void run() {
		try {
			runnable.run();
		} catch ( Throwable e ){
			Log.info( "Failed to run reporter: " + e.getMessage() );
			e.printStackTrace();
		}
	}
}