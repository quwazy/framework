package framework.engines;

import framework.exceptions.FrameworkException;
import framework.http.Request;
import framework.http.Response;
import framework.http.SuccessfulResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerEngine {
    private static volatile ServerEngine instance = null;
    private static Map<String, Object> controllerMap;   //path to controller's object
    private static Map<String, Method> methodMap;      //path to method of controller
    private static final int TCP_PORT = 9999;

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

    public static void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(TCP_PORT);
            System.out.println("Server is running at http://localhost:"+TCP_PORT);

            while(true){
                Socket socket = serverSocket.accept();
                new Thread(new ThreadEngine(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void insertMethod(String path, Method method, Object controller){
        if (controllerMap.containsKey(path)) {
            throw new FrameworkException("Path: " + path + " already exists.");
        }
        controllerMap.put(path, controller);
        methodMap.put(path, method);
    }

    protected Response makeResponse(Request request) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InstantiationException {
        if (request.getMethod() == framework.http.Method.POST){
            Method method = methodMap.get(request.getPath());
            if (method == null){
                throw new FrameworkException("POST method not found for path: " + request.getPath());
            }
            method.setAccessible(true);

            if (method.getParameterTypes().length == 1){
                Object obj = DatabaseEngine.getInstance().createEntity(method.getParameterTypes()[0].getName(), request.getParameters());
                method.invoke(controllerMap.get(request.getPath()), obj);
            }
            else {
                throw new FrameworkException("POST method must have only one parameter");
            }
        }
        if (request.getMethod() == framework.http.Method.GET){
            if (request.getPath().contains("?")){
                Long id = Long.parseLong(request.getPath().split("\\?")[1].split("=")[1]);
                String path = request.getPath().split("\\?")[0];

                request.setPath(path);
                Method method = methodMap.get(request.getPath());
                if (method == null) {
                    throw new FrameworkException("GET method not found for path: " + request.getPath());
                }
                method.setAccessible(true);
                if (method.getParameterTypes().length == 1) {
                    return (Response) method.invoke(controllerMap.get(request.getPath()), id);
                }
            }
            else {
                Method method = methodMap.get(request.getPath());
                if (method == null) {
                    throw new FrameworkException("POST method not found for path: " + request.getPath());
                }
                method.setAccessible(true);
                if (method.getParameterTypes().length == 0) {
                    System.out.println("METHOD NAME: " + method.getName());
                    return (Response) method.invoke(controllerMap.get(request.getPath()));
                } else {
                    throw new FrameworkException("GET method don't have any parameters");
                }
            }
        }
        if (request.getMethod() == framework.http.Method.DELETE){
            Long id = Long.parseLong(request.getPath().split("\\?")[1].split("=")[1]);
            String path = request.getPath().split("\\?")[0];

            request.setPath(path);
            Method method = methodMap.get(request.getPath());
            if (method == null) {
                throw new FrameworkException("DELETE method not found for path: " + request.getPath());
            }
            method.setAccessible(true);
            if (method.getParameterTypes().length == 1) {
                method.invoke(controllerMap.get(request.getPath()), id);
                return new SuccessfulResponse();
            }
        }

//        switch (request.getMethod()){
//            case GET -> {
//                Method method = methodMap.get(request.getPath());
//                if (method == null) {
//                    throw new FrameworkException("GET method not found for path: " + request.getPath());
//                }
//                method.setAccessible(true);
//                if (method.getParameterTypes().length == 0) {
//                    return (Response) method.invoke(controllerMap.get(request.getPath()));
//                } else {
//                    throw new FrameworkException("GET method don't have any parameters");
//                }
//            }
//            case POST -> {
//                Method method = methodMap.get(request.getPath());
//                if (method == null) {
//                    throw new FrameworkException("POST method not found for path: " + request.getPath());
//                }
//                method.setAccessible(true);
//                if (method.getParameterTypes().length == 1) {
//                    Object obj = DatabaseEngine.getInstance().createEntity(method.getParameterTypes()[0].getName(), request.getParameters());
//                    method.invoke(controllerMap.get(request.getPath()), obj);
//                }
//            }
//
//            case DELETE -> {
//
//            }
//
//            default ->  {
//                return new SuccessfulResponse();
//            }
//        }

        return new SuccessfulResponse();
    }

}
