package framework.aspects;

import framework.engines.DiscoveryEngine;
import framework.engines.ServerEngine;
import framework.exceptions.FrameworkException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Aspect
public class RunAspect {
    /**
     * Intercepting any main method in the playground package.
     */
    @Pointcut("execution(public static void playground.*.main(String[])) ")
    public void playgroundMainPoint() {
    }

    @Before("playgroundMainPoint()")
    public void beforePlaygroundMain(JoinPoint joinPoint) throws Exception {
        System.out.println("########## Framework is starting ##########");

        /// Start the initialization process
        DiscoveryEngine.getInstance();

        /// Start the server
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                ServerEngine.run();
            } catch (Exception e) {
                throw new FrameworkException("Failed to start server: " + e.getMessage());
            }
        });
    }
}
