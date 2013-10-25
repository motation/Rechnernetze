package client;

public class User {
	private String server;
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

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	private int port;
	private String user;
	private String pass;

	public User(String server, int port, String user, String pass) {
		this.pass = pass;
		this.port = port;
		this.server = server;
		this.user = user;
	}
}
