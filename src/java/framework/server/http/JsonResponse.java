package framework.server.http;

import com.google.gson.Gson;

public class JsonResponse extends Response{
    private Gson gson;
    private Object jsonObject;

    public JsonResponse(Object jsonObject) {
        this.gson = new Gson();
        this.jsonObject = jsonObject;
    }

    @Override
    public String render() {
        StringBuilder responseContent = new StringBuilder();

        responseContent.append("HTTP/1.1 200 OK\n");
        for (String key : this.headers.getKeys()) {
            responseContent.append(key).append(":").append(this.headers.get(key)).append("\n");
        }
        responseContent.append("\n");

        responseContent.append(this.gson.toJson(this.jsonObject));

        return responseContent.toString();
    }
}
