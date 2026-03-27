package de.ollie.dbtools.utils;

import java.util.function.Supplier;
import lombok.Generated;

/**
 * A utility class for checkers.
 */
@Generated
public class Check {

	Check() {
		throw new UnsupportedOperationException("It's an utility class! Do not instantiate!");
	}

	/**
	 * Checks if the passed condition is "true" or throws the passed exception.
	 *
	 * @param condition        The condition which is to check.
	 * @param runtimeException The exception to throw.
	 * @throws NullPointerException Passing a null value as exception and a "false"
	 *                              condition.
	 */
	public static void ensure(boolean condition, RuntimeException runtimeException) {
		if (!condition) {
			throw runtimeException;
		}
	}

	/**
	 * Checks if the passed condition is "true" or throws the passed exception.
	 *
	 * @param condition                The condition which is to check.
	 * @param runtimeExceptionSupplier A supplier for the exception to throw.
	 * @throws NullPointerException Passing a null value as exception supplier and a
	 *                              "false" condition.
	 */
	public static void ensure(boolean condition, Supplier<RuntimeException> runtimeExceptionSupplier) {
		if (!condition) {
			throw runtimeExceptionSupplier.get();
		}
	}

	/**
	 * Checks if the passed condition is "true" or throws an
	 * IllegalArgumentException with passed message.
	 *
	 * @param condition The condition which is to check.
	 * @param message   The message for the IllegalArgumentException.
	 */
	public static void ensure(boolean condition, String message) {
		if (!condition) {
			throw new IllegalArgumentException(message);
		}
	}
}
