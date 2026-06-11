package com.marcus.eventhub.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ApiErrorWriter {

    private final ObjectMapper objectMapper;

    public ApiErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        GlobalExceptionHandler.ApiErrorResponse body = new GlobalExceptionHandler.ApiErrorResponse(
                java.time.Instant.now(),
                status.value(),
                message,
                null
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
