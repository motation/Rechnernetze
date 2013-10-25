package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
// Server Klasse
public class Server implements Runnable{

	private ServerSocket serverSocket;
	
	public Server() throws IOException{
		this.serverSocket = new ServerSocket(1025);
	}
	
	@Override
	public void run() {
		while(true){
			try {
				Socket socket = serverSocket.accept();
				(new Thread(new ServerConnection(socket))).start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}