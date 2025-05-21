package framework.server;

import com.google.gson.Gson;
import framework.engines.DatabaseEngine;
import framework.engines.ServerEngine;
import framework.exceptions.FrameworkException;
import framework.server.http.Header;
import framework.server.http.Method;
import framework.server.http.Request;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerThread implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ServerThread(Socket socket) {
        this.socket = socket;

        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())), true);
        } catch (IOException e) {
            closeSocket();
            throw new FrameworkException("Failed to create BufferedReader/PrintWriter");
        }
    }

    @Override
    public void run() {
        try {
            Request request = generateRequest();

            if (request == null) {
                closeSocket();
            }
            /// na osnovu rute odbradjujemo zahtev
            /// Response response = ServerEngine.makeResponse(request.getRoute);
            /// out.println(response)

            sendResponse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeSocket();
        }
    }

    private Request generateRequest() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String line = in.readLine();
        if (line == null) {
            return null;
        }

        String[] actionRow = line.split(" ");
        Method method = Method.valueOf(actionRow[0]);
        String route = method + " " + actionRow[1];
        Header header = new Header();
        HashMap<String, String> parameters = new HashMap<>();

        do {
            line = in.readLine();
            String[] headerRow = line.split(": ");
            if (headerRow.length == 2) {
                System.out.println("Found header: " + headerRow[0] + " = " + headerRow[1]);
                header.add(headerRow[0].toLowerCase(), headerRow[1]);
            }
        } while (!line.trim().isEmpty());

        if (method.equals(Method.POST)) {
            int contentLength = Integer.parseInt(header.get("content-length"));
            char[] buff = new char[contentLength];
            in.read(buff, 0, contentLength);
            String body = new String(buff);

            String contentType = header.get("content-type");

            /// request with JSON
            if (contentType != null && contentType.contains("application/json")) {
                Gson gson = new Gson();
                // Parse the JSON string into a HashMap
                HashMap<String, Object> jsonMap = gson.fromJson(body, HashMap.class);

                for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                    parameters.put(entry.getKey(), entry.getValue().toString());
                    System.out.println("Found parameter: " + entry.getKey() + " = " + entry.getValue());

                }
                DatabaseEngine.getInstance().addEntity(parameters);
            } else {
                // Handle other formats if needed
                System.out.println("Unsupported content type: ");
            }
            /// request with FILE
        }
        return new Request(method, route, header, parameters);
    }

    private void sendResponse(){
        StringBuilder responseContent = new StringBuilder();
        responseContent.append("HTTP/1.1 200 OK\r\n");
        responseContent.append("Content-Type: text/html\r\n");
        responseContent.append("\r\n");
        responseContent.append("<h1>Hello World</h1>");

        out.println(responseContent);
    }

    private void closeSocket(){
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            throw new FrameworkException("Failed to close socket");
        }
    }
}
