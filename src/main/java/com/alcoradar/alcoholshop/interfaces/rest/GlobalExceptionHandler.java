package com.alcoradar.alcoholshop.interfaces.rest;

import com.alcoradar.alcoholshop.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Глобальный обработчик исключений для REST API
 *
 * <p>Этот класс перехватывает исключения, выбрасываемые в контроллерах,
 * и преобразует их в корректные HTTP ответы с meaningful сообщениями об ошибках.</p>
 *
 * <p>Использует RFC 7807 Problem Details для HTTP APIs формат для ошибок.</p>
 *
 * <p><b>Обрабатываемые исключения:</b></p>
 * <ul>
 *   <li>{@link AlcoholShopNotFoundException} → 404 Not Found</li>
 *   <li>{@link InvalidCredentialsException} → 401 Unauthorized</li>
 *   <li>{@link ExpiredTokenException} → 401 Unauthorized</li>
 *   <li>{@link InvalidTokenException} → 401 Unauthorized</li>
 *   <li>{@link AccessDeniedException} → 403 Forbidden</li>
 *   <li>{@link UserNotFoundException} → 404 Not Found</li>
 *   <li>{@link UsernameAlreadyExistsException} → 409 Conflict</li>
 *   <li>Другие исключения → 500 Internal Server Error</li>
 * </ul>
 *
 * @author AlcoRadar Team
 * @version 1.0.0
 * @since 2025
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Обрабатывает {@link InvalidCredentialsException} и возвращает 401 Unauthorized
     *
     * @param ex исключение, выброшенное при неверных учетных данных
     * @return ProblemDetail с информацией об ошибке 401
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    protected ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
        problemDetail.setTitle("Unauthorized");
        problemDetail.setType(URI.create("https://api.alcoradar.com/errors/unauthorized"));
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("errorCode", "INVALID_CREDENTIALS");
        return problemDetail;
    }

    /**
     * Обрабатывает {@link ExpiredTokenException} и возвращает 401 Unauthorized
     *
     * @param ex исключение, выброшенное при просроченном токене
     * @return ProblemDetail с информацией об ошибке 401
     */
    @ExceptionHandler(ExpiredTokenException.class)
    protected ProblemDetail handleExpiredToken(ExpiredTokenException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
        problemDetail.setTitle("Unauthorized - Token Expired");
        problemDetail.setType(URI.create("https://api.alcoradar.com/errors/token-expired"));
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("errorCode", "TOKEN_EXPIRED");
        return problemDetail;
    }

    /**
     * Обрабатывает {@link InvalidTokenException} и возвращает 401 Unauthorized
     *
     * @param ex исключение, выброшенное при невалидном токене
     * @return ProblemDetail с информацией об ошибке 401
     */
    @ExceptionHandler(InvalidTokenException.class)
    protected ProblemDetail handleInvalidToken(InvalidTokenException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
        problemDetail.setTitle("Unauthorized - Invalid Token");
        problemDetail.setType(URI.create("https://api.alcoradar.com/errors/invalid-token"));
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("errorCode", "INVALID_TOKEN");
        return problemDetail;
    }

    /**
     * Обрабатывает {@link AccessDeniedException} и возвращает 403 Forbidden
     *
     * @param ex исключение, выброшенное при недостатке прав
     * @return ProblemDetail с информацией об ошибке 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
        problemDetail.setTitle("Forbidden");
        problemDetail.setType(URI.create("https://api.alcoradar.com/errors/forbidden"));
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("errorCode", "ACCESS_DENIED");
        return problemDetail;
    }

    /**
     * Обрабатывает {@link UserNotFoundException} и возвращает 404 Not Found
     *
     * @param ex исключение, выброшенное при отсутствии пользователя
     * @return ProblemDetail с информацией об ошибке 404
     */
    @ExceptionHandler(UserNotFoundException.class)
    protected ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("User Not Found");
        problemDetail.setType(URI.create("https://api.alcoradar.com/errors/not-found"));
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("errorCode", "USER_NOT_FOUND");
        return problemDetail;
    }

    /**
     * Обрабатывает {@link UsernameAlreadyExistsException} и возвращает 409 Conflict
     *
     * @param ex исключение, выброшенное при существующем username
     * @return ProblemDetail с информацией об ошибке 409
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    protected ProblemDetail handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Conflict");
        problemDetail.setType(URI.create("https://api.alcoradar.com/errors/conflict"));
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("errorCode", "USERNAME_ALREADY_EXISTS");
        return problemDetail;
    }

    /**
     * Обрабатывает {@link AlcoholShopNotFoundException} и возвращает 404 Not Found
     *
     * @param ex исключение, выброшенное при отсутствии алкомаркета
     * @return ProblemDetail с информацией об ошибке 404
     */
    @ExceptionHandler(AlcoholShopNotFoundException.class)
    protected ProblemDetail handleAlcoholShopNotFound(AlcoholShopNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Alcohol Shop Not Found");
        problemDetail.setType(URI.create("https://api.alcoradar.com/errors/not-found"));
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("errorCode", "ALCOHOL_SHOP_NOT_FOUND");
        return problemDetail;
    }

    /**
     * Обрабатывает все остальные исключения и возвращает 500 Internal Server Error
     *
     * @param ex непредвиденное исключение
     * @return ProblemDetail с информацией об ошибке 500
     */
    @ExceptionHandler(Exception.class)
    protected ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage()
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://api.alcoradar.com/errors/internal-server-error"));
        problemDetail.setProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        problemDetail.setProperty("errorCode", "INTERNAL_SERVER_ERROR");
        return problemDetail;
    }
}
