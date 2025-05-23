package framework.engines;

import framework.exceptions.FrameworkException;
import framework.server.http.Request;
import framework.server.http.Response;
import framework.server.http.SuccessfulResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
        if (controllerMap.containsKey(path)) {
            throw new FrameworkException("Path: " + path + " already exists.");
        }

        System.out.println("Inserted mehtod on path: " + path);
        controllerMap.put(path, controller);
        methodMap.put(path, method);
    }

    public Response makeResponse(Request request) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InstantiationException {
        if (request.getMethod() == framework.server.http.Method.POST){
            Method method = methodMap.get(request.getPath());
            if (method == null){
                throw new FrameworkException("POST method not found for path: " + request.getPath());
            }
            method.setAccessible(true);

            if (method.getParameterTypes().length ==1){
                Object obj = DatabaseEngine.getInstance().createEntity(method.getParameterTypes()[0].getName(), request.getParameters());
                method.invoke(controllerMap.get(request.getPath()), obj);
            }
            else {
                throw new FrameworkException("POST method must have only one parameter");
            }
        }

        return new SuccessfulResponse();
    }

}
