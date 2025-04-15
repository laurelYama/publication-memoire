package com.esiitech.publication_memoire.logging;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Pointcut("@annotation(com.esiitech.publication_memoire.logging.Loggable)")
    public void executeLogging() {}

    @Before("executeLogging()")
    public void logMethodCall(JoinPoint joinPoint) {
        logger.info("Appel méthode : {} avec les arguments : {}",
                joinPoint.getSignature(), joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "executeLogging()", returning = "result")
    public void logMethodReturn(JoinPoint joinPoint, Object result) {
        logger.info("Fin méthode : {} - Retour : {}", joinPoint.getSignature(), result);
    }

    @AfterThrowing(pointcut = "executeLogging()", throwing = "ex")
    public void logMethodException(JoinPoint joinPoint, Throwable ex) {
        logger.error("Exception dans la méthode : {} - Erreur : {}", joinPoint.getSignature(), ex.getMessage());
    }
}

