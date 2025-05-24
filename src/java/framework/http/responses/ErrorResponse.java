package framework.http.responses;

public class ErrorResponse extends Response {

    @Override
    public String render() {
        StringBuilder responseContext = new StringBuilder();

        responseContext.append("HTTP/1.1 500 Internal Server Error\n");
        for (String key : this.headers.getKeys()) {
            responseContext.append(key).append(":").append(this.headers.get(key)).append("\n");
        }

        return responseContext.toString();
    }
}
