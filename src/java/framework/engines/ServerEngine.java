package framework.engines;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds all methods mapped to paths
 * and their controllers.
 */
public class ServerEngine {
    private static volatile ServerEngine instance = null;

    private static Map<String, Object> controllerMap;   //path to controller's object
    private static Map<String, Method> methodMap;      //path to method of controller

    private ServerEngine() {
        controllerMap = new HashMap<>();
        methodMap = new HashMap<>();
    }

    public static ServerEngine getInstance() {
        if (instance == null) {
            instance = new ServerEngine();
        }
        return instance;
    }

    protected void insertMethod(String path, Method method, Object controller){
        controllerMap.put(path, controller);
        methodMap.put(path, method);
        System.out.println("Inserted method: " + path);
    }
}
