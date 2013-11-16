package server;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;

public class User {
	private String userName;
	private SocketAddress socketAddress;
	private InetAddress receivedIPAddress; // IP-Adresse des Clients
	private int receivedPort; // Port auf dem Client
	
	
	public User(String userName, SocketAddress socketAddress, InetAddress receivedIPAddress, int receivedPort) {
		this.userName = userName;
		this.socketAddress = socketAddress;
		this.receivedIPAddress = receivedIPAddress;
		this.receivedPort = receivedPort;
	}

	/*
	 * Getters
	 */
	public String getUserName() {
		return userName;
	}
	
	public SocketAddress getSocketAddress() {
		return socketAddress;
	}
	
	public InetAddress getReceivedIPAddress() {
		return receivedIPAddress;
	}
	
	public int getReceivedPort() {
		return receivedPort;
	}
	
	/*
	 * Setters
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
