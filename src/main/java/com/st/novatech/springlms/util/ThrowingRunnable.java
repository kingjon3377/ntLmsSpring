package com.st.novatech.springlms.util;

/**
 * A no-arg method that may throw an exception. This is intended to be used as
 * the type for method references.
 *
 * @param <E> the type of exception thrown
 *
 * @author Jonathan Lovelace
 */
@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> {
	/**
	 * The method reference.
	 * @throws E when thrown by the referenced method.
	 */
	void run() throws E;
}
