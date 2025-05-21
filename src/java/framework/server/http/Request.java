package framework.server.http;

import java.util.HashMap;

public class Request {
    private Method method;
    private String path;
    private Header headers;
    private HashMap<String, String> parameters;

    public Request(Method method, String path, Header headers) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.parameters = new HashMap<>();
    }

    public Request(Method method, String path, Header headers, HashMap<String, String> parameters) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.parameters = parameters;
    }

    public static HashMap<String, String> getParametersFromRoute(String route) {
        String[] splittedRoute = route.split("\\?");

        if(splittedRoute.length == 1) {
            return new HashMap<String, String>();
        }

        return getParametersFromString(splittedRoute[1]);
    }

    public static HashMap<String, String> getParametersFromString(String parametersString) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        String[] pairs = parametersString.split("&");
        for (String pair:pairs) {
            String[] keyPair = pair.split("=");
            parameters.put(keyPair[0], keyPair[1]);
        }

        return parameters;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Header getHeaders() {
        return headers;
    }

    public void setHeaders(Header headers) {
        this.headers = headers;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }
}
