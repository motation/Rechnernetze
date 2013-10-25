package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import client.User;

public class ServerDemo {
	public static void main(String[] args) throws IOException {
//		new Thread(new Server()).start();
//		Socket socket = new Socket("localhost", 1025);
		
		User user = new User("mail.gmx.net", 25, "s-o.fedders@gmx.de", "p!vD87#60");
		
		Socket socket = new Socket("pop.gmx.net", 995);
		
		InputStreamReader in = new InputStreamReader(socket.getInputStream());
		
		BufferedReader reader = new BufferedReader(in);
		System.out.println(reader.readLine());
		
		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
		BufferedWriter writer = new BufferedWriter(out);
		
		writer.write("STAT");
		writer.flush();
		System.out.println(reader.readLine());
		
//		String string = null;
//	
//		while((string=reader.readLine()) != null){
//			System.out.println(string);
//		}
		
	}
}
