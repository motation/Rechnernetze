package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class User {
	private String name;
	private String pass;
	private String mailFolder;
	private String server;
	private int port;
	private boolean logged = false;
	private boolean nameSet = false;

	public User(String name, String pass, String server, int port) {
		this.name = name;
		this.pass = pass;
		this.port = port;
		this.server = server;
	}

	public User(String name, String pass) {
		this.name = name;
		this.pass = pass;
	}

	public User(String name) {
		this.name = name;

		// homeDir

		Properties prop = new Properties();

		try {
			// load a properties file and absolute dir
			prop.load(new FileInputStream(System.getProperty("user.dir")
					+ "/src/client/auth.properties"));
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}

		this.mailFolder = prop.getProperty("homeDir") + "/mails/" + name;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public void setMailFolder(String mailFolder) {
		this.mailFolder = mailFolder;
	}

	public String getMailFolder() {
		return this.mailFolder;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isLogged() {
		return logged;
	}

	public void setLogged(boolean logged) {
		this.logged = logged;
	}

	public void setName(boolean set) {
		this.nameSet = set;
	}

	public boolean isNameSet() {
		return this.nameSet;
	}

}
