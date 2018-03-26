package net;

import misc.Constants;
import misc.Messages;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class AdHocTCP implements Runnable{

    private String fileIP;
    private int filePort;
    private String fileName;

    public AdHocTCP(String fileIP, int filePort, String fileName) {
        this.fileIP = fileIP;
        this.filePort = filePort;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        System.out.println(String.format(Messages.fileTransferRequestSend, fileIP, filePort, fileName));
        try {
            Socket fileSocket = new Socket(fileIP, filePort);
            fileSocket.setSoTimeout(Constants.fileTimeout);
            DataOutputStream out = new DataOutputStream(fileSocket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(fileSocket.getInputStream()));
            out.writeBytes(String.format(Messages.fileTransferRequest, fileName));
            saveFile(in, fileName);
            fileSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void saveFile(BufferedReader in, String fileName){
        try {
            File file = new File(Constants.ObtainedDirectoryPath + fileName);
            BufferedWriter toFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            String data;
            while ((data = in.readLine()) != null) {
                toFile.write(data + System.lineSeparator());
                toFile.flush();
            }
            toFile.close();
            System.out.println("\nFile transfer successful.");
        } catch(IOException e){
            System.out.println("\nFile transfer failed.");
            e.printStackTrace();
        }
    }

}
