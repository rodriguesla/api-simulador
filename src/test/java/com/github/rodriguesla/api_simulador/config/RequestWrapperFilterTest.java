package com.github.rodriguesla.api_simulador.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestWrapperFilterTest {
    @Test
    void testDoFilterwrapsHttpServletRequest() throws IOException, ServletException {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        RequestWrapperFilter filter = new RequestWrapperFilter();
        filter.doFilter(httpRequest, response, chain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain).doFilter(requestCaptor.capture(), eq(response));
        ServletRequest wrappedRequest = requestCaptor.getValue();
        assertTrue(wrappedRequest instanceof ContentCachingRequestWrapper,
                "Request should be wrapped with ContentCachingRequestWrapper");
    }

    @Test
    void testDoFilterpassesNonHttpServletRequest() throws IOException, ServletException {
        ServletRequest nonHttpRequest = mock(ServletRequest.class);
        ServletResponse response = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        RequestWrapperFilter filter = new RequestWrapperFilter();
        filter.doFilter(nonHttpRequest, response, chain);

        verify(chain).doFilter(nonHttpRequest, response);
    }
}

