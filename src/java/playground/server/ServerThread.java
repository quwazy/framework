package playground.server;

import framework.exceptions.FrameworkException;

import java.io.*;
import java.net.Socket;

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
