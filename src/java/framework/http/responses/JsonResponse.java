package framework.http.responses;

import com.google.gson.Gson;

public class JsonResponse extends Response {
    private Gson gson;
    private Object jsonObject;

    public JsonResponse(Object jsonObject) {
        this.gson = new Gson();
        this.jsonObject = jsonObject;
    }

    @Override
    public String render() {
        StringBuilder responseContext = new StringBuilder();

        responseContext.append("HTTP/1.1 200 OK\n");
        for (String key : this.headers.getKeys()) {
            responseContext.append(key).append(":").append(this.headers.get(key)).append("\n");
        }
        responseContext.append("\n");
        responseContext.append(this.gson.toJson(this.jsonObject));

        return responseContext.toString();
    }
}
