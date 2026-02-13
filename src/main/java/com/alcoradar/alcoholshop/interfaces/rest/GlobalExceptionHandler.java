package com.alcoradar.alcoholshop.interfaces.rest;

import com.alcoradar.alcoholshop.domain.exception.AlcoholShopNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

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
