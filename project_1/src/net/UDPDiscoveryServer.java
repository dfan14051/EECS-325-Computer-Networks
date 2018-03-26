package net;

import misc.Messages;
import misc.Ports;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class UDPDiscoveryServer implements Runnable {

    Set<Socket> neighbors;
    Set<String> pings;

    public UDPDiscoveryServer(Set<Socket> neighbors) {
        this.neighbors = neighbors;
        pings = new HashSet<>();
    }

    @Override
    public void run() {
        try {
            DatagramSocket serverSocket = new DatagramSocket(Ports.UDPDiscoveryPort);
            byte[] receiveData = new byte[1024];
            byte[] sendData;
            String ping;
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                ping = new String(receivePacket.getData());
                sendData = ping.getBytes();

                for(Socket neighbor : neighbors){
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, neighbor.getInetAddress(), Ports.UDPDiscoveryPort);
                    serverSocket.send(sendPacket);
                }
                if (!pings.contains(ping)) {
                    pings.add(ping);
                    String pong = String.format(Messages.peerDiscoveryResponse, Utils.getLocalIP(), Ports.welcomePort);
                    sendData = pong.getBytes();
                    String[] pingData = ping.split("[:;]");
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(pingData[1]), Integer.parseInt(pingData[2].trim()));
                    serverSocket.send(sendPacket);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
