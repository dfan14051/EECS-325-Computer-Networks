package net;

import misc.Constants;
import misc.Messages;
import misc.Ports;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

public class NeighborSocket implements Runnable {

    Socket connectionSocket;
    Set<Socket> neighbors;
    Set<String> fileNames;
    HashSet<QueryLog> queryIDs;
    BufferedReader inFromClient;
    DataOutputStream outToClient;

    NeighborSocket(Socket connectionSocket, Set<Socket> neighbors, Set<String> fileNames) {
        this.connectionSocket = connectionSocket;
        this.neighbors = neighbors;
        this.fileNames = fileNames;
        this.queryIDs = new HashSet<>();
    }

    @Override
    public void run() {
        String in;
        String[] query;
        try {
            this.inFromClient = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
            this.outToClient = new DataOutputStream(this.connectionSocket.getOutputStream());
            while (true) {
                in = inFromClient.readLine();
                if (in == null) {
                    System.out.println("\nClient Closed.");
                    return;
                }
                query = in.split("[:;]");
                switch (query[0]) {
                    case "Q":
                        queried(Integer.parseInt(query[1]), query[2], outToClient);
                        break;
                    case "H":
                        heartbeat(outToClient);
                        break;
                    case "T":
                        fileRequested(query[1], outToClient);
                        break;
                    default:
                        System.out.println("\nUnknown input");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client closed.");
        }

    }

    private void queried(int queryID, String shortString, DataOutputStream outToClient) throws IOException {
        if (!queryIDs.contains(new QueryLog(queryID, connectionSocket.getInetAddress().getHostAddress()))) {
            System.out.println(String.format(Messages.queryReceived, connectionSocket.getInetAddress().getHostAddress(),
                    connectionSocket.getPort()));
            queryIDs.add(new QueryLog(queryID, connectionSocket.getInetAddress().getHostAddress()));
            String peerIP = "";
            int peerPort = Ports.welcomePort;
            if (fileNames.contains(shortString)) {
                System.out.println(String.format(Messages.queryResponsePositive, shortString));
                try {
                    peerIP = Utils.getLocalIP();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String response = QueryFlood.fetchFileLocation(shortString, neighbors, queryID);
                if (response == null) {
                    System.out.println(String.format(Messages.queryResponseNegative, shortString));
                    return;
                }
                String[] responses = response.split("[(;:)]");
                peerIP = responses[3];
            }
            respond(outToClient, queryID, peerIP, peerPort, shortString);
        }
    }

    private void heartbeat(DataOutputStream outToClient) {
        System.out.println(String.format(Messages.heartbeatReceived, connectionSocket.getInetAddress().getHostAddress(), connectionSocket.getPort()));
        System.out.println(Messages.replyingHeartbeat);
        try {
            outToClient.writeBytes(Messages.heartbeat);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void respond(DataOutputStream outToClient, Integer queryID, String peerIP, int peerPort, String fileName) throws IOException {
        outToClient.writeBytes(String.format(Messages.response, queryID, peerIP, peerPort, fileName));
    }

    private void fileRequested(String fileName, DataOutputStream outToClient) {
        System.out.println(String.format(Messages.fileTransferRequestReceived, connectionSocket.getInetAddress().getHostAddress(), connectionSocket.getPort(), fileName));
        File file = new File(Constants.sharedDirectoryPath + fileName);
        try {
            FileInputStream in = new FileInputStream(file);
            BufferedReader inFromFile = new BufferedReader(new InputStreamReader(in, "UTF8"));
            String data;
            boolean firstLine = true;
            while ((data = inFromFile.readLine()) != null) {
                if (firstLine) {
                    data = Utils.removeUTF8BOM(data);
                    firstLine = false;
                }
                outToClient.writeBytes(data + System.lineSeparator());
                outToClient.flush();
            }
            System.out.println(String.format(Messages.fileTransmissionSuccessful, fileName, connectionSocket.getInetAddress().getHostAddress(), connectionSocket.getPort()));
            outToClient.flush();
            in.close();
            connectionSocket.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("\nFile transmission failed.");
            e.printStackTrace();
        }
    }

}
