package io.skullabs.tools.agent.commons;

import lombok.RequiredArgsConstructor;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

import static io.skullabs.tools.agent.commons.Lang.asClass;

/**
 * Read agent configuration. By default, it will lookup at the Agent's Jar folder to
 * find the agent.conf file.
 */
@RequiredArgsConstructor
public class Config {

	final Properties reader;

	public Config(){
		final File propertiesFile = propertiesFile();
		reader = createFileWriter( propertiesFile );
	}

	private static File propertiesFile(){
		try {
			final URI uri = Config.class.getProtectionDomain().getCodeSource().getLocation().toURI();
			final File jarLocation = new File( uri.getPath() ).getParentFile();
			return new File( jarLocation, "agent.conf" );
		} catch (URISyntaxException e) {
			Log.info( e.getMessage() );
			throw new RuntimeException( e );
		}
	}

	private Properties createFileWriter(File propertiesFile){
		try {
			Properties p = new Properties();
			p.load( new FileInputStream(propertiesFile));
			return p;
		} catch (IOException e) {
			throw new RuntimeException( e );
		}
	}

	public Collection<String> getStringList(String prefix){
		return getStringList( prefix, s->s );
	}

	public <T> Collection<T> getStringList(String prefix, Function<String, T> mapper){
		return getKeyMap( prefix, (s)->s, mapper ).values();
	}

	public Map<String, String> getKeyMap( final String prefix ) {
		final String removablePrefix = prefix+".";
		return getKeyMap( prefix, s->s.replace(removablePrefix, ""), s->s);
	}

	public <K,T> Map<K,T> getKeyMap(String prefix, Function<String,K> keyMapper, Function<String, T> mapper) {
		Map<K,T> values = new HashMap<>();
		for (Object keyAsObject : reader.keySet()) {
			String key = (String)keyAsObject;
			if ( key.startsWith( prefix + "." ) )
				values.put(
					keyMapper.apply( key ),
					mapper.apply( reader.getProperty( key ) ) );
		}
		return values;
	}

	public int getInteger( String name, Integer defaultValue ){
		return Integer.valueOf( getString( name, defaultValue.toString() ) );
	}

	public boolean getBoolean( String name ) {
		return Boolean.valueOf( getString( name, "false" ) );
	}

	public boolean getBoolean( String name, boolean defaultValue ) {
		return Boolean.valueOf( getString( name, "" + defaultValue ) );
	}

	public String getString( String name, String defaultValue ){
		String value = (String)reader.getOrDefault( name, defaultValue );
		Log.info( "Key loaded: " + name + ". Value: " + value );
		return value;
	}

	public Class<?> getClass( String name, Class<?> defaultValue ) {
		String clazz = getString( name, defaultValue.getCanonicalName() );
		return asClass( clazz );
	}
}
