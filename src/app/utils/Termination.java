package app.utils;
import java.net.ServerSocket;


public class Termination extends Thread {
    private static ServerSocket servSock;

    public Termination(ServerSocket servSock)
    {
        Termination.servSock = servSock;
    }

    public void run() {
        try {
            System.out.println("Closing ServerSocket on port " + servSock.getLocalPort());
            servSock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}