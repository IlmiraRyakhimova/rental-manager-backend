package com.rental.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Arrays;

@Slf4j
@Aspect
public class ControllerLoggingAspect {

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        log.info("→ {} args={}", method, Arrays.toString(joinPoint.getArgs()));
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long took = System.currentTimeMillis() - start;
            log.info("← {} {} ms result={}", method, took, result);
            return result;
        } catch (Throwable e) {
            long took = System.currentTimeMillis() - start;
            log.warn("✗ {} {} ms exception={}: {}", method, took, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
