package com.marketmate.common;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class TraceFilter implements Filter {
    public static final String TRACE_ID = "traceId";
    private static final Logger log = LoggerFactory.getLogger(TraceFilter.class);

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;

        String traceId = Optional.ofNullable(httpReq.getHeader("X-Trace-Id"))
                .orElse(UUID.randomUUID().toString());

        // logback の JSON パターンとキー名を合わせる
        MDC.put(TRACE_ID, traceId);
        MDC.put("http.method", httpReq.getMethod());
        MDC.put("http.path", httpReq.getRequestURI());
        MDC.put("http.remote", Optional.ofNullable(httpReq.getHeader("X-Forwarded-For"))
                .orElseGet(req::getRemoteAddr));

        try {
            chain.doFilter(req, res);
        } finally {
            MDC.put("http.status", String.valueOf(httpRes.getStatus()));
            log.info("HTTP access");
            MDC.clear();
        }
    }
}
