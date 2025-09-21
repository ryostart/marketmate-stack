package com.marketmate.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,     // 例: "Bad Request"
        String code,      // 任意のアプリコード（"VALIDATION_ERROR" など）
        String message,   // 人が読むメッセージ
        String path,      // リクエストパス
        String traceId,   // X-Trace-Id と一致
        List<FieldError> details // バリデーション詳細など
) {
    public record FieldError(String field, String message) {
    }
}
