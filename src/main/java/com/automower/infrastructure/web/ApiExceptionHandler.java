package com.automower.infrastructure.web;

import com.automower.domain.exception.BusinessValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    // erreurs propres au format de la requête : leur code vit dans l'adapter, pas dans le catalogue du domaine
    private static final String MALFORMED_REQUEST_CODE = "MALFORMED_REQUEST";
    private static final String MALFORMED_REQUEST_TITLE = "Malformed request";
    private static final String MISSING_MOWER_CODE = "MISSING_MOWER";
    private static final String MISSING_MOWER_TITLE = "Missing mower";

    @ExceptionHandler(BusinessValidationException.class)
    public ProblemDetail handleValidation(BusinessValidationException exception) {
        return problem(exception.getCode().name(), exception.getTitle(), exception.getMessage());
    }

    @ExceptionHandler(MissingMowerException.class)
    public ProblemDetail handleMissingMower(MissingMowerException exception) {
        return problem(MISSING_MOWER_CODE, MISSING_MOWER_TITLE, exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException exception) {
        // le message Jackson décrit la structure interne des DTO : il n'est pas exposé
        return problem(MALFORMED_REQUEST_CODE, MALFORMED_REQUEST_TITLE, "request body is missing, not valid JSON or has a missing, null or mistyped field");
    }

    private static ProblemDetail problem(String code, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        return problem;
    }
}
