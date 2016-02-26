package io.skullabs.tools.agent.metrics;

/**
 * Represents a read only data.
 */
public interface ReadOnlyData<T> {

	/**
	 * Retrieves an read-only data. It is expected that the returned object
	 * is immutable.
	 *
	 * @return
	 */
	T getData();
}
