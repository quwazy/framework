package framework.server;

import framework.exceptions.FrameworkException;
import framework.server.http.Header;
import framework.server.http.Method;
import framework.server.http.Request;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class ServerThread implements Runnable{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ServerThread(Socket socket){
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
            throw new FrameworkException("Failed to create BufferedReader/PrintWriter");
        }
    }

    @Override
    public void run() {
        try {
            Request request = generateRequest();

            if (request == null){
                closeSocket();
            }
            /// na osnovu rute odbradjujemo zahtev
            /// Response response = ServerEngine.makeResponse(request.getRoute);
            /// out.println(response)

            sendResponse(request);
        }catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            closeSocket();
        }
    }

    private Request generateRequest() throws IOException {
        String line = in.readLine();
        if (line == null){
            return null;
        }

        String[] actionRow = line.split(" ");
        Method method = Method.valueOf(actionRow[0]);
        String route = method + " " + actionRow[1];
        Header header = new Header();
        HashMap<String, String> parameters = Request.getParametersFromRoute(route);

        do {
            line = in.readLine();
            String[] headerRow = line.split(": ");
            if(headerRow.length == 2) {
                header.add(headerRow[0], headerRow[1]);
            }
        } while(!line.trim().isEmpty());

        return new Request(method, route, header, parameters);
    }

    private void sendResponse(Request request){
        StringBuilder responseContent = new StringBuilder();
        responseContent.append("HTTP/1.1 200 OK\r\n");
        responseContent.append("Content-Type: text/html\r\n");
        responseContent.append("\r\n");
        responseContent.append("<h1>Hello World</h1>");

        out.println(responseContent.toString());
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
