package main.java.com.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

public class UDPServer implements Runnable {
	private static final int serverPort = 1025;
	private DatagramSocket serverDatagram;
	
	public UDPServer() {
	}
	
	@Override
	public void run() {
		try {
			this.serverDatagram = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		while (true) {			
			try {
				// waiting for new connection
				System.out.println("UDP: Waiting for someone to connect");
				byte[] content = new byte[4];
				DatagramPacket received = new DatagramPacket(content, 4);
				this.serverDatagram.receive(received);
				
				// connecting to the TCP server
				int TCPServerPort = Integer.parseInt(new String(content));
				System.out.println("UDP: going to connect to the TCP server on port " + TCPServerPort);
				Socket socket = new Socket(received.getAddress(), TCPServerPort);
				
				// connecting to the new TCP server
				DataInputStream in = new DataInputStream(socket.getInputStream());
				int newTcpPort = in.readInt();
				socket.close();
				socket = new Socket(received.getAddress(), newTcpPort);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws SocketException {
		DatagramSocket ds = new DatagramSocket(1024);
	}
}