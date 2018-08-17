package de.exb.platform.cloud.fileservice.resources;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import de.exb.platform.cloud.fileservice.service.FileServiceException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class InternalExceptionHandler extends ResponseEntityExceptionHandler {

	/**
	 * handles HTTP bad request case
	 * 
	 * @param e the exception as cause
	 */
	@ExceptionHandler(value = { IllegalArgumentException.class, IllegalStateException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	protected void handleConflict(RuntimeException e) {
		log.error("HttpStatus.BAD_REQUEST", e);
	}

	/**
	 * handles HTTP conflict case
	 * 
	 * @param e the exception as cause
	 */
	@ExceptionHandler(FileServiceException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	protected void handleConflict(FileServiceException e) {
		log.error("HttpStatus.CONFLICT", e);
	}

	/**
	 * handles HTTP internal error case
	 * 
	 * @param e the exception as cause
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	protected void handle(Exception e) {
		log.error("HttpStatus.INTERNAL_SERVER_ERROR", e);
	}

}
