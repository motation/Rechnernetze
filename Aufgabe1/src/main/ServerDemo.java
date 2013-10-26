package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import client.Pop3Client;
import client.User;

public class ServerDemo {
	public static void main(String[] args) throws IOException, InterruptedException {

		Properties prop = new Properties();

		try {
			// load a properties file and absolute dir
			prop.load(new FileInputStream(System.getProperty("user.dir")
					+ "/src/config/auth.properties"));
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}

		User user = new User("pop3.web.de", 110, prop.getProperty("userweb"),
				prop.getProperty("passweb"), "web");

		List<User> users = new ArrayList<>();
		users.add(user);
		Pop3Client pop3 = new Pop3Client(users);
//		new Thread(pop3).start();
		pop3.fetchMails();
		
	}
}
