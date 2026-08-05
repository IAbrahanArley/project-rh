package br.com.abrahanarley.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception,
			HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, List.of(exception.getMessage()), request);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException exception, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, List.of(exception.getMessage()), request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConflict(DataIntegrityViolationException exception,
			HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, List.of("Request conflicts with existing data."), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		List<String> messages = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.toList();
		return build(HttpStatus.BAD_REQUEST, messages, request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, List.of("Request body is invalid or malformed."), request);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, List.of("Invalid value for parameter: " + exception.getName()), request);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception,
			HttpServletRequest request) {
		return build(HttpStatus.METHOD_NOT_ALLOWED, List.of("HTTP method is not supported for this endpoint."),
				request);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException exception,
			HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, List.of("Invalid username or password."), request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception,
			HttpServletRequest request) {
		return build(HttpStatus.FORBIDDEN, List.of("Access denied."), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
		LOGGER.error("Unexpected error while processing {}", request.getRequestURI(), exception);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, List.of("Unexpected application error."), request);
	}

	private ResponseEntity<ApiErrorResponse> build(HttpStatus status, List<String> messages, HttpServletRequest request) {
		ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), messages,
				request.getRequestURI());
		return ResponseEntity.status(status).body(body);
	}
}
