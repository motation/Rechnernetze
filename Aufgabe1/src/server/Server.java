package server;

import java.io.*;
import java.net.*;
// Server Klasse
//public class Server implements Runnable{
//	
//	public static final int SERVER_PORT = 11000;
//
//	private ServerSocket serverSocket;
//	
//	public Server() throws IOException{
//		this.serverSocket = new ServerSocket(1025);
//	}
//	
//	@Override
//	public void run() {
//		while(true){
//			try {
//				Socket socket = serverSocket.accept();
//				(new Thread(new ServerConnection(socket))).start();
//				
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//}

public class Server {
	/* Server, der Verbindungsanfragen entgegennimmt */
	public static final int SERVER_PORT = 11000;

	public static void main(String[] args) {
		ServerSocket welcomeSocket; // TCP-Server-Socketklasse
		Socket connectionSocket; // TCP-Standard-Socketklasse

		int counter = 0; // Zählt die erzeugten Bearbeitungs-Threads

		try {
			/* Server-Socket erzeugen */
			welcomeSocket = new ServerSocket(SERVER_PORT);

			while (true) { // Server laufen IMMER
				System.out
						.println("TCP Server: Waiting for connection - listening TCP port "
								+ SERVER_PORT);
				/*
				 * Blockiert auf Verbindungsanfrage warten --> nach
				 * Verbindungsaufbau Standard-Socket erzeugen und
				 * connectionSocket zuweisen
				 */
				connectionSocket = welcomeSocket.accept();

				/* Neuen Arbeits-Thread erzeugen und den Socket übergeben */
				new Thread(new ServerConnection(++counter, connectionSocket)).start();
				
			}
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
}