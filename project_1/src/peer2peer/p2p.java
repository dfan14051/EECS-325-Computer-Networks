package peer2peer;

import java.io.*;
import java.net.*;

public class p2p {
	
	public static void main(String[] args){
		System.out.println("Peer Started");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			
			try {
				String command = reader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
