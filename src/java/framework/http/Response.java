package framework.http;

public abstract class Response {
    protected Header headers;

    public Response() {
        headers = new Header();
    }

    public abstract String render();

    public Header getHeader() {
        return headers;
    }
}
