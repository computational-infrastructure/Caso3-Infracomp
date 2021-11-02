package app.utils;

import java.io.IOException;
import java.net.ServerSocket;

public class Termination extends Thread
{
    private static ServerSocket servSock;
    public Termination(ServerSocket servSock)
    {
        Termination.servSock = servSock;
    }

    public void run()
    {
        try 
        {
            System.out.println("Cerrando el ServerSocket");
            servSock.close();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
}