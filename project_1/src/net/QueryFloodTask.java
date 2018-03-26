package net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.concurrent.Callable;

import misc.Messages;

public class QueryFloodTask implements Callable {

    Socket neighbor;
    String fileName;
    int query;

    public QueryFloodTask(Socket neighbor, String fileName, int query) {
        this.neighbor = neighbor;
        this.fileName = fileName;
        this.query = query;
    }

    @Override
    public String call() {
        synchronized (neighbor) {
            try {
                System.out.println(String.format(Messages.querySend, neighbor.getInetAddress().getHostAddress(), neighbor.getPort()));
                DataOutputStream outToServer = new DataOutputStream(neighbor.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(neighbor.getInputStream()));

                outToServer.writeBytes(String.format(Messages.query, query, fileName));
                String response = inFromServer.readLine();

                return response;
            } catch (IOException e) {
                System.out.println("Timeout.");
            }
        }
        return null;
    }
}
