package com.absensi.absensi_app.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(com.absensi.absensi_app.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.info("START_EXECUTION | Method: [{}.{}]", className, methodName);

        Object proceed;
        try {
            proceed = joinPoint.proceed();
        } catch (Throwable t) {
            log.error("ERROR_EXECUTION | Method: [{}.{}] | Error: [{}]", className, methodName, t.getMessage());
            throw t;
        }

        long executionTime = System.currentTimeMillis() - start;

        log.info("END_EXECUTION | Method: [{}.{}] | Executed in {} ms", className, methodName, executionTime);

        return proceed;
    }
}
