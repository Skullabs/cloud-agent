package io.skullabs.tools.agent.commons;

import java.io.*;
import java.util.*;
import java.util.function.Function;

/**
 *
 */
public class Config {

	final File propertiesFile = new File( "agent.conf" );
	final Properties reader = createFileWriter();

	private Properties createFileWriter(){
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

	public int getInteger( String name ){
		return Integer.valueOf( (String)reader.getOrDefault( name, "0" ) );
	}

	public boolean getBoolean( String name ) {
		return Boolean.valueOf( getString( name, "false" ) );
	}

	public String getString( String name, String defaultValue ){
		String value = (String)reader.getOrDefault( name, defaultValue );
		Log.info( "Key loaded: " + name + ". Value: " + value );
		return value;
	}
}
