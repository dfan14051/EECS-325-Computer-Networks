package net;

import misc.Constants;
import misc.Messages;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class UDPDiscoveryClient implements Callable {

    String ipAddress;
    int port;
    List<String[]> peers;

    public UDPDiscoveryClient(String ipAddress, String port) {
        this.ipAddress = ipAddress;
        this.port = Integer.parseInt(port);
        this.peers = new LinkedList<>();
    }

    @Override
    public List<String[]> call() {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(Constants.UDPtimeout);
            byte[] receiveData = new byte[1024];
            byte[] sendData;
            String ping = String.format(Messages.peerDiscoveryRequest, Utils.getLocalIP(), socket.getLocalPort());
            sendData = ping.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipAddress), port);
            socket.send(sendPacket);
            while (true) {
                if(peers.size() >= 2){
                    return peers;
                }
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                String pong = new String(receivePacket.getData());
                peers.add(pong.split("[;:]"));
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e){
            return peers;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
