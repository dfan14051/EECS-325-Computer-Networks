package net;

import misc.Messages;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class Heartbeat extends TimerTask {

    Socket clientSocket;
    Set<Socket> neighbors;
    Timer timer;

    public Heartbeat(Socket clientSocket, Set<Socket> neighbors, Timer timer) {
        this.clientSocket = clientSocket;
        this.neighbors = neighbors;
        this.timer = timer;
    }

    @Override
    public void run() {
        synchronized (clientSocket) {
            try {
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                System.out.println(String.format(Messages.sendingHeartbeat,
                        clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()));
                outToServer.writeBytes(Messages.heartbeat);
                String response = inFromServer.readLine();
                if (response.equals(Messages.heartbeat.trim())) {
                    System.out.println(Messages.heartbeatReplyReceived);
                } else {
                    throw new IOException();
                }

            } catch (SocketTimeoutException e) {
                try {
                    System.out.println(String.format(Messages.heartbeatNoReturn, clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()));
                    clientSocket.close();
                    neighbors.remove(clientSocket);
                    timer.cancel();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                System.out.println("\nClient Closed.");
            }
        }
    }

}
