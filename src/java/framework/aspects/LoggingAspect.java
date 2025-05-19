package framework.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class LoggingAspect {
    @Pointcut("execution(* playground.Calculator.*(..))")
    public void calculatorMethods() {}

    @Before("calculatorMethods()")
    public void beforeMethodExecution(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("About to execute method: " + methodName);

        // Log parameters
        Object[] args = joinPoint.getArgs();
        System.out.println("Parameters from aspect: ");
        for (Object arg : args) {
            System.out.println(arg);
        }
    }

    @After("calculatorMethods()")
    public void afterMethodExecution(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("Finished executing method: " + methodName);
    }
}
