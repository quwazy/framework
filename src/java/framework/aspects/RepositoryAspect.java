package framework.aspects;

import framework.engines.DatabaseEngine;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

@Aspect
public class RepositoryAspect {

    @Pointcut("call (void playground.*.*.add(..))")
    public void repositoryMethodCalls() {}

    @Around("repositoryMethodCalls()")
    public void aroundRepositoryMethods(ProceedingJoinPoint joinPoint) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        System.out.println("Name: " + joinPoint.getSignature().getName());      //ime funckije koju smo presreli
        System.out.println("Declaring type: " + joinPoint.getSignature().getDeclaringType());       //kog je tipa klasa u kojoj se nalazi presretnuta funckija
        System.out.println("Declaring type name: " + joinPoint.getSignature().getDeclaringTypeName());      //full package name klase u kojoj je presretnuta funckija
        System.out.println("Modifiers: " + Modifier.toString(joinPoint.getSignature().getModifiers()));        //predstavlja koje vidjivosti je presretnuta funckija (public, private, abstact)
        System.out.println("To string: " + joinPoint.getSignature().toString());        //ime fukcije sa popunjenim parametrima iz Pointcut-a
        System.out.println("To short string: " + joinPoint.getSignature().toShortString());     //ime klase i ime funckije
        System.out.println("To long string: " + joinPoint.getSignature().toLongString());       //svi detalji

        System.out.println("Kind: " + joinPoint.getKind());     //kog je bilo tipa presretanje (execution, call...)
        System.out.println("Source location: " + joinPoint.getSourceLocation());    //ime klase i linija u kojoj se presretanje desilo
        System.out.println("Args: " + Arrays.toString(joinPoint.getArgs()));        //argumenti koje je presretnuta funckija primila
        System.out.println("Target: " + joinPoint.getTarget());     //vraca objekat koji je pozvao metodu
        System.out.println("This: " + joinPoint.getThis());     //objekat klase u kojoj se presretanje desilo

        if (joinPoint.getArgs().length == 1 && joinPoint.getArgs()[0] != null) {
            DatabaseEngine.getInstance().insertEntity(joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getArgs()[0]);
        }
    }

//    @Around("call (void playground.*.*.get(int)")
//    public void repositoryGetCall(ProceedingJoinPoint joinPoint) {
//        System.out.println("Get method called");
//    }
//
    @Pointcut("call (void playground.*.*.getAll(..))")
    public void repositoryMethodGetAll() {}

    @Around("repositoryMethodGetAll()")
    public void repositoryGetAllCall(ProceedingJoinPoint joinPoint) {
        System.out.println("Get All method called");
    }
//
//    @Around("call (void playground.*.*.add()")
//    public void repositoryAddCall(ProceedingJoinPoint joinPoint) {
//        System.out.println("Add method called");
//    }
//
//    @Around("call (void playground.*.*.update(..)")
//    public void repositoryUpdateCall(ProceedingJoinPoint joinPoint) {
//        System.out.println("Update method called");
//    }
//
//    @Around("call (void playground.*.*.delete(int )")
//    public void repositoryDeleteCall(ProceedingJoinPoint joinPoint) {
//        System.out.println("Delete method called");
//    }
}
