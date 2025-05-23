package framework.aspects;

import framework.engines.DiscoveryEngine;
import framework.engines.ServerEngine;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Aspect
public class RunAspect {
    // Specific pointcut for main methods in playground package
    @Pointcut("execution(public static void playground.*.main(String[])) ")
    public void playgroundMainPoint() {
    }

    @Before("playgroundMainPoint()")
    public void beforePlaygroundMain(JoinPoint joinPoint) throws Exception {
        System.out.println("Intercepted main method from playground: " + joinPoint.getSignature().getDeclaringTypeName());
        System.out.println("Running application");
        DiscoveryEngine.getInstance();
        // Start server in a separate thread so it doesn't block the main application
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                ServerEngine.run();
            } catch (Exception e) {
                System.err.println("Failed to start server: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
