package com.backend.common.exception;

public class UnauthorizedActionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UnauthorizedActionException(String msg) {
		super(msg);
	}

}
