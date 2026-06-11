package com.marcus.eventhub.common.ratelimit;

import com.marcus.eventhub.common.exception.ApiErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Pattern REGISTRATION_PATH = Pattern.compile(
            "^/events/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/registrations$"
    );

    private final RateLimitService rateLimitService;
    private final ApiErrorWriter apiErrorWriter;

    public RateLimitFilter(RateLimitService rateLimitService, ApiErrorWriter apiErrorWriter) {
        this.rateLimitService = rateLimitService;
        this.apiErrorWriter = apiErrorWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        RateLimitBucket bucketType = resolveBucketType(request);

        if (bucketType != null && !rateLimitService.tryConsume(bucketType, resolveClientKey(request))) {
            apiErrorWriter.write(response, HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please try again later.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitBucket resolveBucketType(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return null;
        }

        String path = request.getRequestURI();
        if (path.equals("/auth/register") || path.equals("/auth/login") || path.equals("/auth/refresh")) {
            return RateLimitBucket.AUTH;
        }

        if (REGISTRATION_PATH.matcher(path).matches()) {
            return RateLimitBucket.REGISTRATION;
        }

        return null;
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
