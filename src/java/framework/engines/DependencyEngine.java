package framework.engines;

import framework.annotations.components.Autowired;
import framework.annotations.components.Controller;
import framework.annotations.components.Service;
import framework.annotations.methodes.Delete;
import framework.annotations.methodes.Get;
import framework.annotations.methodes.Post;
import framework.annotations.methodes.Put;
import framework.exceptions.FrameworkException;
import framework.http.responses.Response;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates new instances of classes
 * annotated with @Service, @Controller, @Component.
 * Injects instances if annotation @Autowired
 * is present as filed annotation in the class.
 */
public class DependencyEngine {
    private static volatile DependencyEngine instance = null;

    private static Map<String, Object> serviceMap;      //class name -> service object
    private static Map<String, Object> controllerMap;   //class name -> controller object
    private static Map<String, Object> componentMap;    //class name -> component object

    private DependencyEngine() {
        serviceMap = new HashMap<>();
        controllerMap = new HashMap<>();
        componentMap = new HashMap<>();
    }

    protected static DependencyEngine getInstance() {
        if (instance == null) {
            instance = new DependencyEngine();
        }
        return instance;
    }

    protected void creteService(List<Class<?>> classes) throws Exception {
        for (Class<?> cls : classes) {
            Object obj = cls.getDeclaredConstructor().newInstance();
            serviceMap.put(cls.getName(), obj);
        }
    }

    protected void creteController(List<Class<?>> classes) throws Exception {
        for (Class<?> cls : classes) {
            for (Field field : cls.getDeclaredFields()){
                /// Forbidden to use @Autowired in Controller classes
                if (field.isAnnotationPresent(Autowired.class)){
                    throw new FrameworkException("Forbidden to use @Autowired annotation in @Controller classes. Class: " + cls.getName() + " has @Autowired annotation in field: " + field.getName() );
                }
            }

            Object obj = cls.getDeclaredConstructor().newInstance();
            controllerMap.put(cls.getName(), obj);

            String controllerPath = cls.getAnnotation(Controller.class).path();

            for (Method method : cls.getDeclaredMethods()){
                if (method.isAnnotationPresent(Get.class)){
                    if (method.getReturnType() != Response.class){
                        throw new FrameworkException("Method with @Get annotation must return Response object. Method: " + method.getName() + " in class: " + cls.getName() + " does not return Response object");
                    }
                    ServerEngine.getInstance().insertMethod("GET " + controllerPath + method.getAnnotation(Get.class).path(), method, obj);
                    continue;
                }
                if (method.isAnnotationPresent(Post.class)){
                    ServerEngine.getInstance().insertMethod("POST " + controllerPath + method.getAnnotation(Post.class).path(), method, obj);
                    continue;
                }
                if (method.isAnnotationPresent(Put.class)){
                    ServerEngine.getInstance().insertMethod("PUT " + controllerPath + method.getAnnotation(Put.class).path(), method, obj);
                    continue;
                }
                if (method.isAnnotationPresent(Delete.class)){
                    ServerEngine.getInstance().insertMethod("DELETE " + controllerPath + method.getAnnotation(Delete.class).path(), method, obj);
                    continue;
                }
            }
        }
    }

    protected void createComponents(List<Class<?>> classes) throws Exception {
        for (Class<?> cls : classes) {
            for (Field field : cls.getDeclaredFields()){
                if (field.isAnnotationPresent(Autowired.class)){
                    throw new FrameworkException("Forbidden to use @Autowired in @Component classes. Class: " + cls.getName() + " has @Autowired annotation in field: " + field.getName());
                }
            }

            Object obj = cls.getDeclaredConstructor().newInstance();
            componentMap.put(cls.getName(), obj);
        }
    }

    protected void injectDependencies() throws IllegalAccessException {

        /// inserting components into service classes
        for (String serviceName : serviceMap.keySet()){
            Object serviceObject = serviceMap.get(serviceName);

            for (Field field : serviceObject.getClass().getDeclaredFields()){
                if (field.isAnnotationPresent(Autowired.class)) {
                    if (field.getType().isAnnotationPresent(Controller.class)){
                        throw new FrameworkException("Classes with @Controller annotation cannot be injected into @Service classes with @Autowired. Class: " + serviceObject.getClass().getName() + " has @Autowired annotation in field: " + field.getName());
                    }

                    field.setAccessible(true);
                    field.set(serviceObject, componentMap.get(field.getType().getName()));
                }
            }
        }

        /// injecting services into controller classes
        for (String controllerName : controllerMap.keySet()){
            Object controllerObject = controllerMap.get(controllerName);

            for (Field field : controllerObject.getClass().getDeclaredFields()){
                if (field.getType().isAnnotationPresent(Service.class)){
                    field.setAccessible(true);
                    field.set(controllerObject, serviceMap.get(field.getType().getName()));
                }
            }
        }
    }
}
