package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import client.Pop3Client;
import client.User;

public class ProxyPopServer {
	public static void main(String[] args) throws IOException {
		User user = new User("s-o.fedders@gmx.de", "***", "pop.gmx.net",
				110);
		List<User> users = new ArrayList<>();
		users.add(user);
		Pop3Client client = new Pop3Client(users);
		// new Thread(client).start();
		client.fetchMails();
	}
}
