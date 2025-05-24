package framework.http.responses;

public class SuccessfulResponse extends Response{

    @Override
    public String render() {
        StringBuilder responseContext = new StringBuilder();

        responseContext.append("HTTP/1.1 200 OK\n");
        for (String key : this.headers.getKeys()) {
            responseContext.append(key).append(":").append(this.headers.get(key)).append("\n");
        }

        return responseContext.toString();
    }
}
