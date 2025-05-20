package framework.aspects;

import framework.engines.DiscoveryEngine;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.InvocationTargetException;

@Aspect
public class RunAspect {
    @Pointcut("execution(public static void *.main(String[]))")
    public void beginningPoint() {
    }

    @Before("beginningPoint()")
    public void beforeMain() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        System.out.println("Running application");
        DiscoveryEngine.getInstance();
    }
}
