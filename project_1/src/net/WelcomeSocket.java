package net;

import misc.Messages;
import misc.Ports;
import misc.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class WelcomeSocket implements Runnable {

    private Set<Socket> neighbors;

    public WelcomeSocket(Set<Socket> neighbors) {
        this.neighbors = neighbors;
    }

    @Override
    public void run() {
        try {
            HashSet<String> fileNames = getFileNames();

            ServerSocket welcomeSocket = new ServerSocket(Ports.welcomePort);
            while (true) {
                Socket connectionSocket = welcomeSocket.accept();
                System.out.println(String.format(Messages.connectionAccepted,
                        connectionSocket.getInetAddress().getHostAddress(), connectionSocket.getPort()));
                (new Thread(new NeighborSocket(connectionSocket, neighbors, fileNames))).start(); // Open TCP connection with neighbor
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashSet<String> getFileNames() {
        HashSet<String> fileNames = new HashSet<>();
		File fileIndex = new File(Constants.fileIndexPath);
		BufferedReader fromFile;
		try {
			fromFile = new BufferedReader(new FileReader(fileIndex));
			String file;
			while((file = fromFile.readLine()) != null) {
				fileNames.add(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
//			System.exit(-1);
		}


        return fileNames;
    }

}
