package server;

import java.io.*;
import java.net.*;

public class Server {
	/* Server, der Verbindungsanfragen entgegennimmt */
	public static final int SERVER_PORT = 11000;

	public static void main(String[] args) {
		ServerSocket welcomeSocket; // TCP-Server-Socketklasse
		Socket connectionSocket; // TCP-Standard-Socketklasse

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

				/* Neuen Arbeits-Thread erzeugen und den Socket �bergeben */
				new Thread(new ServerConnection(connectionSocket)).start();
				
			}
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
}