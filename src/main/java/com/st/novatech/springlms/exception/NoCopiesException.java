package com.st.novatech.springlms.exception;

public class NoCopiesException extends Exception {
	/**
	 * To throw an instance of this exception class, the caller must supply the
	 * exception message.
	 *
	 * @param errorMessage the exception message
	 */
	public NoCopiesException(final String errorMessage) {
		super(errorMessage);
	}
	/**
	 * To throw an instance of this exception class, the caller must supply the
	 * exception message and may supply a cause.
	 *
	 * @param errorMessage the exception message
	 * @param cause the exception that caused this one
	 */
	public NoCopiesException(final String errorMessage, final Throwable cause) {
		super(errorMessage, cause);
	}
}
