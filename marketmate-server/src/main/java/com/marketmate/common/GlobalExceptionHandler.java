package com.marketmate.common;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiError> build(
            HttpStatus status, String code, String message,
            String path, List<ApiError.FieldError> details) {

        String traceId = MDC.get(TraceFilter.TRACE_ID);
        ApiError body = new ApiError(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path,
                traceId,
                details
        );
        return ResponseEntity.status(status).body(body);
    }

    // Bean Validation （@Valid）本体の検証失敗
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleInvalid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiError.FieldError> fields = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> new ApiError.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "入力内容をご確認ください", req.getRequestURI(), fields);
    }

    // @RequestParam / @PathVariable の検証失敗
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        List<ApiError.FieldError> fields = ex.getConstraintViolations().stream()
                .map(v -> new ApiError.FieldError(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", "入力内容をご確認ください", req.getRequestURI(), fields);
    }

    // JSON 変換エラー
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String msg = "リクエスト本文の形式が不正です";
        if (ex.getCause() instanceof InvalidFormatException ife) {
            msg = "値の形式が不正です: " + ife.getValue();
        }
        return build(HttpStatus.BAD_REQUEST, "INVALID_JSON", msg, req.getRequestURI(), null);
    }

    // パラメータ不備/型不一致/メディアタイプ不正
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<ApiError> handleParam(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "パラメータが不正です", req.getRequestURI(), null);
    }

    // 呼び出し側が ResponseStatusException を投げたときはそのまま返す
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleRse(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus hs = HttpStatus.resolve(ex.getStatusCode().value());
        return build(hs != null ? hs : HttpStatus.INTERNAL_SERVER_ERROR,
                "ERROR",
                Optional.ofNullable(ex.getReason()).orElse("エラーが発生しました"),
                req.getRequestURI(),
                null);
    }

    // 一意制約など
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY", "データの整合性エラーが発生しました", req.getRequestURI(), null);
    }

    // サーバーエラー
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "サーバ側でエラーが発生しました", req.getRequestURI(), null);
    }
}
