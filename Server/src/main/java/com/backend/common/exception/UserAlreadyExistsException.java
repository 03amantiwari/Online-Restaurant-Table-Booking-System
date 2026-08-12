package com.backend.common.exception;

public class UserAlreadyExistsException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserAlreadyExistsException(String message , String uid) {
        super(message + uid);
    }
}
