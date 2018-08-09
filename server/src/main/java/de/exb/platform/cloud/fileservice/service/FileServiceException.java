package de.exb.platform.cloud.fileservice.service;

import java.io.IOException;

/**
 * Generic file service exception
 * 
 * @author resulav
 *
 */
public class FileServiceException extends IOException {

	private static final long serialVersionUID = 3689197784645132447L;

	/**
	 * constructor for situation
	 * 
	 * @param aMessage situation message
	 */
	public FileServiceException(final String aMessage) {
		super(aMessage);
	}

	/**
	 * constructor for situation with cause
	 * 
	 * @param aMessage situation message
	 * @param aThrowable the cause
	 */
	public FileServiceException(final String aMessage, final Throwable aThrowable) {
		super(aMessage, aThrowable);
	}
}
