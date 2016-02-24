package io.skullabs.tools.agent.commons;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 *
 */
public abstract class Lang {

	public static <T> Class<T> asClass( String canonicalName ){
		try {
			Class<T> clazz = (Class<T>) Class.forName(canonicalName);
			Log.info( "Class loaded by agent: " + clazz );
			return clazz;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException( e );
		}
	}

	public static <O,R> List<R> convert(Iterable<O> data, Function<O,R> mapper ) {
		List<R> newList = new ArrayList<>();
		for ( O original : data )
			newList.add( mapper.apply(original) );
		return newList;
	}

	public static long sum( long...data ) {
		long sum = 0;
		for ( long original : data )
			sum+= original;
		return sum;
	}

	public static double divide( long l1, long l2 ){
		final BigDecimal l1Big = new BigDecimal(l1);
		final BigDecimal l2Big = new BigDecimal(l2);
		return l1Big.divide(l2Big, 2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
	}

	public static double divide( double l1, double l2 ){
		final BigDecimal l1Big = new BigDecimal(l1);
		final BigDecimal l2Big = new BigDecimal(l2);
		return l1Big.divide(l2Big, 2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
	}
}
