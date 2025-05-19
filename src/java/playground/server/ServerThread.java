package playground.server;

import java.io.*;
import java.net.Socket;

public class ServerThread implements Runnable{
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String firstLine;

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
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            in.lines().forEach(System.out::println);

            /// ovde treba da se ubaci da na osnovu putanje poziva odredjenu metodu iz kontroler klasa

            out.println("Hello World");
            in.close();
            out.close();
            socket.close();
        }catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
