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
	final static int SERVER_PORT = 9876;
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
	private static final String USER_NOT_FOUND = "ERROR User doesn't exist.";
	private static final String COMMAND_NOT_FOUND = "ERROR Command not found.";
	private User user;

	public void startService() {
		String capitalizedSentence;

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

		System.out.println(SERVERNAME + " got from: " + receiveString);
		if (isUserNotConnected(receivedSocketAddress) && !receiveString.startsWith("NEW ")) {
			writeToClient(receivedIPAddress, receivedPort, "Please register first.");
		} else if (receiveString.startsWith("NEW ")) { 
			newUser(new User(receiveString.substring(3, receiveString.length()), receivedSocketAddress, receivedIPAddress, receivedPort));
		} else {
			
			if (receiveString == "SHUTDOWN") {
				serviceRequested = false;
			} else if (receiveString == "QUIT"){
				quitUser(getUser(receivedSocketAddress));
			} else if (receiveString.startsWith("NEW ")) {
				/* Nur noch Umbennenung, da schon angemeldet! */
				getUser(receivedSocketAddress).setUserName(receiveString.substring(3, receiveString.length()));
			} else {
				error(getUser(receivedSocketAddress), COMMAND_NOT_FOUND);
			}
			
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
				sendData.length, ipAddress,
				port);
		/* Senden des Pakets */
		try {
			serverSocket.send(sendPacket);
		} catch (IOException e) {
			System.err.println(SENDING_ERROR + e);
			return;
		}

		System.out.println(SERVERNAME + " has sent the message: " + sendString);
	}

	private void writeToClient(User user, String sendString) {
		writeToClient(user.getReceivedIPAddress(), user.getReceivedPort(),
				sendString);
	}

	/*
	 * Sendet eine Nachricht an alle User
	 */
	private void writeToAllClients(String sendString) {
		/* String in Byte-Array umwandeln */
		byte[] sendData = sendString.getBytes();

		/* Zu jedem User senden */
		for (User user : users) {
			writeToClient(user, sendString);
		}

		System.out.println(SERVERNAME + " has sent to all User the message: "
				+ sendString);
	}

	/*
	 * Error-Handling
	 */
	private void error(User user, String errorMessage) {
		writeToClient(user, errorMessage);
		quitUser(user);
	}

	/*
	 * Legt einen neuen User an und bestätigt
	 */
	private void newUser(User user) {
		users.add(user);
		writeToClient(user, SUCCESS);
	}

	/*
	 * Löscht einen User aus der User-Map.
	 */
	private void quitUser(User user) {
		// int userIndex = getUser(socketAddress);
		// user = users.get(userIndex);
		// user = null;
		// users.remove(userIndex);
		users.remove(getIndexOfUser(user.getSocketAddress()));
		writeToClient(user, QUIT);
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
	
	private User getUser(SocketAddress socketAddress) {
		return users.get(getIndexOfUser(socketAddress));
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

	private boolean isUserNotConnected(SocketAddress socketAddress) {
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
