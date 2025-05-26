package framework.engines;

import com.google.gson.Gson;
import framework.exceptions.FrameworkException;
import framework.http.*;
import framework.http.responses.ErrorResponse;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Accepts a request from a client and processes it.
 * Sends a response to the client.
 */
public class ThreadEngine implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    protected ThreadEngine(Socket socket) {
        this.socket = socket;

        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())), true);
        }
        catch (IOException e) {
            closeSocket();
            throw new FrameworkException("Failed to create BufferedReader/PrintWriter");
        }
    }

    @Override
    public void run() {
        try {
            Request request = generateRequest();
            if (request == null) {
                return;
            }

            out.println(ServerEngine.getInstance().makeResponse(request).render());
        }
        catch (Exception e) {
            out.println(new ErrorResponse().render());
            throw new RuntimeException(e);
        }
        finally {
            closeSocket();
        }
    }

    /**
     * Generates a request object from the input stream.
     * @return Request object or null if the input stream is empty.
     */
    private Request generateRequest() throws IOException {
        String line = in.readLine();
        if (line == null) {
            return null;
        }

        String[] actionRow = line.split(" ");
        Method method = Method.valueOf(actionRow[0]);
        String route = method + " " + actionRow[1];
        Header headers = new Header();
        HashMap<String, String> jsonBody = new HashMap<>();

        /// Reading headers from a request
        do {
            line = in.readLine();
            String[] headerRow = line.split(": ");
            if (headerRow.length == 2) {
                headers.add(headerRow[0].toLowerCase(), headerRow[1]);
            }
        } while (!line.trim().isEmpty());

        if (method.equals(Method.POST) || method.equals(Method.PUT)) {
            int contentLength = Integer.parseInt(headers.get("content-length"));
            char[] buff = new char[contentLength];
            in.read(buff, 0, contentLength);
            String body = new String(buff);

            String contentType = headers.get("content-type");

            /// request with JSON body
            if (contentType != null && contentType.contains("application/json")) {
                Gson gson = new Gson();
                HashMap<String, Object> jsonMap = gson.fromJson(body, HashMap.class);   //Parse the JSON string into a HashMap
                for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                    jsonBody.put(entry.getKey(), entry.getValue().toString());
                }
            }
            else {
                throw new FrameworkException("Unsupported content type: " + contentType);
            }
        }

        return new Request(method, route, headers, jsonBody);
    }

    private void closeSocket(){
        try {
            in.close();
            out.close();
            socket.close();
        }
        catch (IOException e) {
            throw new FrameworkException("Failed to close socket");
        }
    }
}
