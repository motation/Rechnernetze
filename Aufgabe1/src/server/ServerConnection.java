package server;

import java.net.Socket;
// ArbeitsThread
public class ServerConnection implements Runnable {

	private Socket socket;

	public ServerConnection(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		System.out.println("Client is connected");
		while (!Thread.currentThread().isInterrupted()) {
			try {
				System.out.println("Fetch Data");
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				Thread.currentThread().interrupt();
			}

		}

	}

}
