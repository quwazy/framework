package framework.engines;

import framework.annotations.components.Controller;
import framework.annotations.components.Repository;
import framework.annotations.methodes.Delete;
import framework.annotations.methodes.Get;
import framework.annotations.methodes.Post;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyEngine {
    private static volatile DependencyEngine instance = null;

    private static Map<String, Object> serviceMap;
    private static Map<String, Object> controllerMap;
    private static Map<String, Object> componentMap;

    private DependencyEngine() {
        serviceMap = new HashMap<>();
        controllerMap = new HashMap<>();
        componentMap = new HashMap<>();
    }

    public static DependencyEngine getInstance() {
        if (instance == null) {
            instance = new DependencyEngine();
        }
        return instance;
    }

    protected void creteService(List<Class<?>> classes) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Class<?> cls : classes) {
            Object obj = cls.getDeclaredConstructor().newInstance();
            serviceMap.put(cls.getName(), obj);
        }
    }

    protected void creteController(List<Class<?>> classes) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Class<?> cls : classes) {
            Object obj = cls.getDeclaredConstructor().newInstance();
            controllerMap.put(cls.getName(), obj);

            String controllerPath = cls.getAnnotation(Controller.class).path();

            for (Method method : cls.getDeclaredMethods()){
                if (method.isAnnotationPresent(Get.class)){
                    ServerEngine.getInstance().insertMethod("GET "+ controllerPath + method.getAnnotation(Get.class).path(), method, obj);
                    break;
                }
                if (method.isAnnotationPresent(Post.class)){
                    ServerEngine.getInstance().insertMethod("POST "+ controllerPath + method.getAnnotation(Post.class).path(), method, obj);
                    break;
                }
                if (method.isAnnotationPresent(Delete.class)){
                    ServerEngine.getInstance().insertMethod("DELETE "+ controllerPath + method.getAnnotation(Delete.class).path(), method, obj);
                    break;
                }
            }
        }
    }

    protected void createComponents(List<Class<?>> classes) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Class<?> cls : classes) {
            Object obj = cls.getDeclaredConstructor().newInstance();
            componentMap.put(cls.getName(), obj);
        }
    }

}
