package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
					+ "\\src\\config\\auth.properties"));
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}

		User user = new User("pop.gmx.net", 110, prop.getProperty("usergmx"),
				prop.getProperty("passgmx"));

		List<User> users = new ArrayList<>();
		users.add(user);
		Pop3Client pop3 = new Pop3Client(users);
//		new Thread(pop3).start();
		pop3.fetchMails();
		
	}
}
