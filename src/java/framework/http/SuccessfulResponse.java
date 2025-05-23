package framework.http;

public class SuccessfulResponse extends Response{

    @Override
    public String render() {
        StringBuilder responseContext = new StringBuilder();

        responseContext.append("HTTP/1.1 200 OK\n");
        for (String key : this.headers.getKeys()) {
            responseContext.append(key).append(":").append(this.headers.get(key)).append("\n");
        }
        responseContext.append("\r\n");
        responseContext.append("<h1>Hello World</h1>");
        responseContext.append("\n");
        return responseContext.toString();

//        StringBuilder responseContent = new StringBuilder();
//        responseContent.append("HTTP/1.1 200 OK\n");
//        responseContent.append("Server: MyServer\n");
//        responseContent.append("Date: Wed, 15 Nov 2015 07:28:00 GMT\n");
//        responseContent.append("Content-Length: 13\n");
//        responseContent.append("Content-Type: text/html\r\n");
//        responseContent.append("\r\n");
//        responseContent.append("<h1>Hello World</h1>");
//
//        return responseContent.toString();
    }
}
