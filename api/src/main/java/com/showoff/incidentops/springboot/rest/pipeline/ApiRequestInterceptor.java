package com.showoff.incidentops.springboot.rest.pipeline;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiRequestInterceptor implements HandlerInterceptor {
    private static final String START_NANOS_ATTRIBUTE = "incidentops.startNanos";
    private static final String PROCESSING_TIME_HEADER = "X-Processing-Time-Ms";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_NANOS_ATTRIBUTE, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object startNanos = request.getAttribute(START_NANOS_ATTRIBUTE);
        if (startNanos instanceof Long start) {
            long elapsedNanos = Math.max(0L, System.nanoTime() - start);
            long elapsedMillis = elapsedNanos / 1_000_000L;
            response.setHeader(PROCESSING_TIME_HEADER, String.valueOf(elapsedMillis));
        }
    }
}
