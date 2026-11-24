package com.rental.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Arrays;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class ControllerLoggingAspect {
    private final SensitiveDataMasker masker;

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        log.info("→ {} args={}", method, Arrays.stream(joinPoint.getArgs()).map(masker::mask).toList());
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long took = System.currentTimeMillis() - start;
            log.info("← {} {} ms result={}", method, took, masker.mask(result));
            return result;
        } catch (Throwable e) {
            long took = System.currentTimeMillis() - start;
            log.warn("✗ {} {} ms exception={}: {}", method, took, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
