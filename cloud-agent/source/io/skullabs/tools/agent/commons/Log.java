package io.skullabs.tools.agent.commons;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static java.lang.String.format;

/**
 *
 */
public class Log {

	static final File logFile = new File( "agent.log" );
	static Writer writer = createFileWriter();

	static Writer createFileWriter(){
		try {
			return new FileWriter( logFile );
		} catch (IOException e) {
			throw new RuntimeException( e );
		}
	}

	public static void info( String msg ){
		try {
			writer.write( format( "[INFO] %s\n", msg ) );
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException( e );
		}
	}
}
