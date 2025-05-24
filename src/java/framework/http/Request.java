package framework.http;

import java.util.HashMap;

public class Request {
    private Method method;
    private String path;
    private Header headers;
    private HashMap<String, String> jsonBody;

    public Request(Method method, String path, Header headers, HashMap<String, String> jsonBody) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.jsonBody = jsonBody;
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

    public HashMap<String, String> getJsonBody() {
        return jsonBody;
    }

    public void setJsonBody(HashMap<String, String> jsonBody) {
        this.jsonBody = jsonBody;
    }
}
