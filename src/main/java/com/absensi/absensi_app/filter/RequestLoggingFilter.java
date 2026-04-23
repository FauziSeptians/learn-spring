package com.absensi.absensi_app.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(2)
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        long start = System.currentTimeMillis();

        log.info("HTTP_REQUEST | [{} {}] | IP: [{}]",
            request.getMethod(),
            request.getRequestURI(),
            request.getRemoteAddr());

        chain.doFilter(req, res);

        log.info("HTTP_RESPONSE | [{} {}] | Status: [{}] | Duration: [{}ms]",
            request.getMethod(),
            request.getRequestURI(),
            response.getStatus(),
            System.currentTimeMillis() - start);
    }
}
