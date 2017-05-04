package org.komparator.security;

public class CryptoException extends Exception {

	CryptoException(String message) {
		super(message);
	}

	CryptoException(String message, Throwable throwable) {
		super(message, throwable);
	}

}