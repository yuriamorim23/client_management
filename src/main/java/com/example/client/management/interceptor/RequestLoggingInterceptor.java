package com.example.client.management.interceptor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;
        HttpStatus status = HttpStatus.resolve(response.getStatus());
        String statusMessage = (status != null) ? status.getReasonPhrase() : "Unknown Status";

        System.out.println(formatLogEntry(request, response.getStatus(), statusMessage, duration));
    }

    private String formatLogEntry(HttpServletRequest request, int status, String statusMessage, long duration) {
        return String.format("%s %s %d - %s %dms",
            request.getMethod(),
            request.getRequestURI(),
            status,
            statusMessage,
            duration);
    }
}
