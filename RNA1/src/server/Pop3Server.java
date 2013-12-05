package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class Pop3Server implements Runnable {

	private ServerSocket serverSocket;
	private boolean run;

	public Pop3Server() throws IOException {
		this.serverSocket = new ServerSocket(11000);
		this.run = true;
	}

	public void startServer() throws IOException {
		// server function
		while (run) {
			new Thread(new ServerConnection(serverSocket.accept())).start();
		}
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				this.startServer();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				Thread.currentThread().interrupt();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		Pop3Server s3 = new Pop3Server();
		s3.startServer();
		
		
	}
}
