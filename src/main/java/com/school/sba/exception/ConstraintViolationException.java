package com.school.sba.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@SuppressWarnings("serial")
@Getter
@AllArgsConstructor
public class ConstraintViolationException extends RuntimeException {

	private String message;
	private HttpStatus status;
	private String rootCause;
	
}