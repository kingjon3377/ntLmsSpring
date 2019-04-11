package com.st.novatech.springlms.exception;

/**
 * An exception class to report the failure of an operation, of a type defined
 * by the specific subclass thrown, from the service layer to the application
 * layer.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
@SuppressWarnings("serial")
public class TransactionException extends Exception {

	/**
	 * Constructor taking only the message.
	 * @param errorMessage the exception message
	 */
	protected TransactionException(final String errorMessage) {
		super(errorMessage);
	}

	/**
	 * Constructor taking the underlying exception cause as well as a message.
	 * @param errorMessage the exception message
	 * @param cause the exception that caused this one to be thrown
	 */
	protected TransactionException(final String errorMessage, final Throwable cause) {
		super(errorMessage, cause);
	}
}
