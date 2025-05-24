package framework.engines;

import framework.exceptions.FrameworkException;
import framework.http.responses.ErrorResponse;
import framework.http.Request;
import framework.http.responses.Response;
import framework.http.responses.SuccessfulResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Starts the server on a given port and waits for incoming connections.
 * Holds all the methods and objects for the controllers,
 * mapped by the HTTP path.
 * Invokes the methods when a request is received.
 */
public class ServerEngine {
    private static volatile ServerEngine instance = null;
    private static final int TCP_PORT = 9999;

    private static Map<String, Object> controllerMap;   //path to controller's object
    private static Map<String, Method> methodMap;       //path to controller's method

    private ServerEngine() {
        controllerMap = new HashMap<>();
        methodMap = new HashMap<>();
    }

    protected static ServerEngine getInstance() {
        if (instance == null) {
            instance = new ServerEngine();
        }
        return instance;
    }

    /**
     * Starts the server and waits for incoming connections.
     */
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

    /**
     * Inserting method and object into maps
     * @param path HTTP path as a key, example GET /employees/all
     * @param method Method for a specific path
     * @param controller Controller which has the method
     */
    protected void insertMethod(String path, Method method, Object controller){
        if (controllerMap.containsKey(path)) {
            throw new FrameworkException("Path: " + path + " already exists.");
        }

        controllerMap.put(path, controller);
        methodMap.put(path, method);
    }

    /**
     * Return response for specified route
     * @param request Request object received from the client.
     * @return Response object based on the request method.
     */
    protected Response makeResponse(Request request) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InstantiationException {
        switch (request.getMethod()){
            case GET -> {
                if (request.getPath().contains("?")) {
                    Long id = Long.parseLong(request.getPath().split("\\?")[1].split("=")[1]);
                    request.setPath(request.getPath().split("\\?")[0]);

                    Method method = methodMap.get(request.getPath());
                    if (method == null) {
                        throw new FrameworkException("GET method not found for path: " + request.getPath());
                    }
                    if (method.getParameterTypes().length == 1){
                        method.setAccessible(true);
                        return (Response) method.invoke(controllerMap.get(request.getPath()), id);
                    }
                    else {
                        throw new FrameworkException("GET method must have only one parameter");
                    }
                }
                else {
                    Method method = methodMap.get(request.getPath());
                    if (method == null) {
                        throw new FrameworkException("GET method not found for path: " + request.getPath());
                    }
                    if (method.getParameterTypes().length == 0) {
                        method.setAccessible(true);
                        return (Response) method.invoke(controllerMap.get(request.getPath()));
                    }
                    else {
                        throw new FrameworkException("GET method must have no parameters");
                    }
                }
            }

            case POST -> {
                Method method = methodMap.get(request.getPath());
                if (method == null) {
                    throw new FrameworkException("POST method not found for path: " + request.getPath());
                }

                method.setAccessible(true);
                if (method.getParameterTypes().length == 1) {
                    Object obj = DatabaseEngine.getInstance().createEntity(method.getParameterTypes()[0].getName(), request.getJsonBody());
                    method.invoke(controllerMap.get(request.getPath()), obj);
                    return new SuccessfulResponse();
                }
                else {
                    throw new FrameworkException("POST method must have only one parameter");
                }
            }

            case PUT -> {
                Method method = methodMap.get(request.getPath());
                if (method == null) {
                    throw new FrameworkException("PUT method not found for path: " + request.getPath());
                }
            }

            case DELETE -> {
                Long id = Long.parseLong(request.getPath().split("\\?")[1].split("=")[1]);

                request.setPath(request.getPath().split("\\?")[0]);
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
        }

        return new ErrorResponse();
    }
}
