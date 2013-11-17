package server;

/*
 * UDPServer.java
 *
 * Version 2.0
 * Vorlesung Rechnernetze HAW Hamburg
 * Autor: M. Hübner (nach Kurose/Ross)
 * Zweck: UDP-Server Beispielcode:
 *        Warte auf UDP-Pakete. Nach Empfang eines Pakets als String auspacken,
 *        in Großbuchstaben konvertieren und zurücksenden
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class UDPServer {
	final static int SERVER_PORT = 50000;
	final static int BUFFER_SIZE = 1024;
	private static final String SERVERNAME = "ChatServer";

	private List<User> users = new ArrayList<>();
	private DatagramSocket serverSocket; // UDP-Socketklasse
	private boolean serviceRequested = true;

	/* Messages */
	private static final String SUCCESS = "OK";
	private static final String QUIT = "Sie wurden abgemeldet.";

	/* Error-Messages */
	private static final String SENDING_ERROR = "ERROR Couldn't send Data to Client:\n";
	private static final String COMMAND_NOT_FOUND = "ERROR Command not found.";

	public void startService() {
		try {
			/*
			 * UDP-Socket erzeugen (kein Verbindungsaufbau!) Socket wird an den
			 * ServerPort gebunden
			 */
			serverSocket = new DatagramSocket(SERVER_PORT);
			System.out.println(SERVERNAME
					+ ": Waiting for connection - listening UDP port "
					+ SERVER_PORT);
			while (serviceRequested) {
				readFromClient();
			}
			
			/* Socket schließen (freigeben) */
			serverSocket.close();
			System.out.println("Server shut down!");
		} catch (SocketException e) {
			System.err.println("Connection aborted by client!");
		} catch (IOException e) {
			System.err.println("Connection aborted by client!");
		}

		System.out.println(SERVERNAME + " stopped!");
	}

	/*
	 * Liest den nächsten Auftrag vom Input
	 */
	private void readFromClient() throws IOException {
		String receiveString = "";

		/* Paket für den Empfang erzeugen */
		byte[] receiveData = new byte[BUFFER_SIZE];
		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				BUFFER_SIZE);

		/* Warte auf Empfang eines Pakets auf dem eigenen Server-Port */
		serverSocket.receive(receivePacket);

		/* Paket erhalten --> auspacken und analysieren */
		receiveString = new String(receivePacket.getData(), 0,
				receivePacket.getLength());
		InetAddress receivedIPAddress = receivePacket.getAddress();
		int receivedPort = receivePacket.getPort();
		SocketAddress receivedSocketAddress = receivePacket.getSocketAddress();
		System.out.println(SERVERNAME + " got: " + receiveString);

		

		if (receiveString.equals("BYE")) {
			quit(receivedSocketAddress, receivedIPAddress, receivedPort);
		} else if (receiveString.startsWith("NEW ")) {
			newUser(
					receiveString.substring(3, receiveString.length()),
					receivedSocketAddress, receivedIPAddress, receivedPort);
		} else if (receiveString.equals("INFO")) {
			writeToClient(receivedIPAddress, receivedPort, getAllUser());
		} else {
			error(receivedSocketAddress, receivedIPAddress, receivedPort, COMMAND_NOT_FOUND);
		}

	}

	/*
	 * Sendet eine Nachricht an einen bestimmten User
	 */
	private void writeToClient(InetAddress ipAddress, int port,
			String sendString) {
		/* Sende den String als UDP-Paket zum Client */

		/* String in Byte-Array umwandeln */
		byte[] sendData = sendString.getBytes();

		/* Antwort-Paket erzeugen */
		DatagramPacket sendPacket = new DatagramPacket(sendData,
				sendData.length, ipAddress, port);
		/* Senden des Pakets */
		try {
			serverSocket.send(sendPacket);
		} catch (IOException e) {
			System.err.println(SENDING_ERROR + e);
			return;
		}

		System.out.println(SERVERNAME + " has sent the message: " + sendString);
	}
	
	/*
	 * Error-Handling
	 */
	private void error(SocketAddress socketAddress,InetAddress ipAddress,int port, String errorMessage) {
		writeToClient(ipAddress, port, errorMessage);
		quit(socketAddress, ipAddress, port);
	}

	/*
	 * Legt einen neuen User an und bestätigt
	 */
	private void newUser(String userName,SocketAddress socketAddress,InetAddress ipAddress,int port) {
		if (isUserUnknown(socketAddress)) {
			User user = new User(userName, socketAddress, ipAddress, port);
			users.add(user);
			
		} else {
			User user = users.get(getIndexOfUser(socketAddress));
			user.setUserName(userName);
		}
		writeToClient(ipAddress, port, SUCCESS);
	}

	/*
	 * Löscht einen User aus der User-Map.
	 */
	private void quit(SocketAddress socketAddress,InetAddress ipAddress,int port) {
		if (!isUserUnknown(socketAddress)) {
			users.remove(getIndexOfUser(socketAddress));
		}		
		writeToClient(ipAddress, port, QUIT);
		serverSocket.disconnect();
	}

	/*
	 * Gibt den Index eines User in users zurück. Im Fehlerfall gibt die Methode
	 * -1 zurück.
	 */
	private int getIndexOfUser(SocketAddress socketAddress) {
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getSocketAddress().equals(socketAddress)) {
				return i;
			}
		}
		return -1;
	}
	
	/*
	 * Gibt eine Liste aller Namen der angemeldeten User als String zurück.
	 */
	private String getAllUser() {
		String resultString = "LIST " + users.size();
		for (User user : users) {
			resultString = resultString + " " + user.getSocketAddress() + " "
					+ user.getUserName();
		}
		return resultString;
	}

	/*
	 * Prüft ob der User nicht in der User-Liste ist.
	 */
	private boolean isUserUnknown(SocketAddress socketAddress) {
		for (User user : users) {
			if (user.getSocketAddress().equals(socketAddress)) {
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		UDPServer myServer = new UDPServer();
		myServer.startService();
	}
}
