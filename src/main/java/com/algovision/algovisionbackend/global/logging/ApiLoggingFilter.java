package com.algovision.algovisionbackend.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Setter
public class ApiLoggingFilter extends OncePerRequestFilter {

    private long slowRequestThreshold;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString().substring(0, 12);
        MDC.put("traceId", traceId);

        String uri = request.getRequestURI();
        String method = request.getMethod();
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (duration >= slowRequestThreshold) {
                log.warn("[TRACE:{}] Slow Request {} {} -> {} ({} ms)",
                        traceId, method, uri, status, duration);
            } else {
                log.info("[TRACE:{}] {} {} -> {} ({} ms)",
                        traceId, method, uri, status, duration);
            }

            MDC.clear();
        }
    }
}
