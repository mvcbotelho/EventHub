package com.marcus.eventhub.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marcus.eventhub.common.exception.ApiErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private ApiErrorWriter apiErrorWriter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter(rateLimitService, apiErrorWriter);
    }

    @Test
    void shouldAllowNonRateLimitedRoutes() throws Exception {
        when(request.getMethod()).thenReturn("GET");

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(rateLimitService, never()).tryConsume(any(), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldBlockAuthEndpointWhenLimitExceeded() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimitService.tryConsume(eq(RateLimitBucket.AUTH), eq("127.0.0.1"))).thenReturn(false);

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(apiErrorWriter).write(response, HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please try again later.");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldAllowAuthEndpointWhenUnderLimit() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimitService.tryConsume(eq(RateLimitBucket.AUTH), eq("127.0.0.1"))).thenReturn(true);

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
