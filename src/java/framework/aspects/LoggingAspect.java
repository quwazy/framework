package framework.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * First aspect I created.
 * It logs all method executions from the Calculator class.
 */
@Aspect
public class LoggingAspect {
    @Pointcut("execution(* playground.old.Calculator.*(..))")
    public void calculatorMethods() {}

    @Before("calculatorMethods()")
    public void beforeMethodExecution(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("About to execute method: " + methodName);

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
