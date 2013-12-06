package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import client.Factory;
import client.Log;
import client.Mail;
import client.User;

public class ServerConnection implements Runnable {

	private static final String QUIT_MESSAGE = "+OK POP3 Server signing off";
	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private Log log;
	private User user;
	private List<User> users;
	private String homeDir;
	private Properties prop = new Properties();
	private final char WHITESPACE = ' ';
	private final int POSITON_OF_WHITESPACE = 4;
	private final int COMMAND_LENGTH = 4;
	private Set<Mail> mails = new HashSet<>();

	public ServerConnection(Socket socket) throws IOException {
		this.socket = socket;
		this.writer = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream()));
		this.reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

		try {
			// load a properties file and absolute dir
			prop.load(new FileInputStream(System.getProperty("user.dir")
					+ "/src/client/auth.properties"));
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
		homeDir = prop.getProperty("homedir");
		this.log = new Log(true, new File(homeDir + "/serverLogs"));
		this.users = new ArrayList<>();
		File file = new File(homeDir + "/mails");
		for (String user : file.list()) {
			this.users.add(new User(user));
			this.users.get(users.size() - 1).setMailFolder(
					homeDir + "/mails/" + user);
		}
		this.user = new User("");

	}

	private String readFromClient() throws IOException {
		String message = this.reader.readLine();
		this.log.writeSession(message);
		return message;
	}

	private void writeToClient(String request) throws IOException {
		log.writeSession(request);
		this.writer.write(request + System.lineSeparator());
		this.writer.flush();
	}

	@Override
	public void run() {
		try {
			writeToClient("+OK connected");
		} catch (IOException e1) {
			System.out.println(e1.getMessage());
			Thread.currentThread().interrupt();
		}
		while (!Thread.currentThread().isInterrupted()) {

			try {
				String request = readFromClient();

				switch (this.getCommandAndParam(request).get(0)) {
				case "pass":
					this.pass(this.getCommandAndParam(request));
					break;
				case "user":
					this.user(this.getCommandAndParam(request));
					break;
				case "stat":
					this.stat(this.getCommandAndParam(request));
					break;
				case "uidl":
					this.uidl(this.getCommandAndParam(request));
					break;
				case "quit":
					this.quit(this.getCommandAndParam(request));
					break;
				case "dele":
					this.dele(this.getCommandAndParam(request));
					break;
				case "retr":
					this.retr(this.getCommandAndParam(request));
					break;
				case "noop":
					this.noop(this.getCommandAndParam(request));
					break;
				case "rset":
					this.rset(this.getCommandAndParam(request));
					break;
				case "list":
					this.list(this.getCommandAndParam(request));
					break;
				default:
					writeToClient("-ERR Command not found");
					break;
				}

			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private void pass(List<String> request) throws IOException {
		if (request.size() == 1) {
			this.argumentMissing();
		} else if (!user.isNameSet()) {
			writeToClient("-ERR user first!");
		} else if (user.isNameSet() && request.size() > 1 && !user.isLogged()) {
			if (user.getName().equals(prop.getProperty("user"))
					&& request.get(1).equals(prop.getProperty("pw"))) {
				writeToClient("+OK success");
				user.setLogged(true);
			} else {
				user.setName(false);
				this.socket.close();
				Thread.currentThread().interrupt();
			}
		} else if (user.isLogged()) {
			this.notAllowedInThisState();
		}
	}

	private void user(List<String> request) throws IOException {
		if (request.size() == 1) {
			this.argumentMissing();
		} else if (user.isNameSet() && request.size() > 1) {
			writeToClient("-ERR too many user");
		} else if (!user.isNameSet()) {
			user.setName(request.get(1));
			writeToClient("+OK password required for user \"" + user.getName()
					+ "\"");
			user.setName(true);
		}

	}

	private void stat(List<String> request) throws IOException {
		if (request.size() > 1) {
			this.tooManyArguments();
		} else if (!user.isLogged()) {
			this.notAllowedInThisState();
		} else if (user.isLogged()) {

			this.mails.addAll(this.getAddedMails(this.users));

			writeToClient("+OK " + this.numberOfMails(this.mails) + " "
					+ this.getOctets());
		}

	}

	private void uidl(List<String> request) throws IOException {
		if (request.size() > 1) {
			int num = 0;
			try {
				num = Integer.parseInt(request.get(1));
			} catch (Exception e) {
				invalidSequence(request.get(1));
				return;
			}
			if (user.isLogged()) {
				this.mails.addAll(this.getAddedMails(this.users));
				List<Mail> unmarked = this.getUnmarkedMails(this.mails);
				Collections.sort(unmarked);
				if (num <= unmarked.size()) {
					writeToClient("+OK " + num + " "
							+ unmarked.get(num - 1).getUidl());
				} else {
					writeToClient("-ERR no such uidl");
				}
			} else {
				this.notAllowedInThisState();
			}
		} else if (request.size() == 1 && user.isLogged()) {
			this.mails.addAll(this.getAddedMails(this.users));
			List<Mail> unmarked = this.getUnmarkedMails(this.mails);
			Collections.sort(unmarked);
			writeToClient("+OK printing uidls");
			int count = 1;
			for (Mail mail : unmarked) {
				writeToClient(count + " " + mail.getUidl());
				count++;
			}
			writeToClient(".");
		} else {
			this.notAllowedInThisState();
		}
	}

	private void quit(List<String> request) throws IOException {
		if (request.size() == 1) {
			writeToClient(QUIT_MESSAGE);
			this.socket.close();
			Thread.currentThread().interrupt();
		} else {
			this.tooManyArguments();
		}
	}

	private void noop(List<String> request) throws IOException {
		writeToClient("+OK");
	}

	private void rset(List<String> request) throws IOException {
		if (request.size() == 1) {
			if (user.isLogged()) {
				int num = 0;
				long octets = 0L;
				for (Mail mail : this.mails) {
					if (mail.isMarked()) {
						mail.setMarked(false);
						num++;
						octets += mail.getOctets();
					}
				}
				writeToClient("+OK maildrop has " + num + " " + octets);
			} else {
				this.notAllowedInThisState();
			}
		} else if (request.size() > 1) {
			this.tooManyArguments();
		}
	}

	private void list(List<String> request) throws IOException {
		if (request.size() > 1) {
			int num = 0;
			try {
				num = Integer.parseInt(request.get(1));
			} catch (Exception e) {
				invalidSequence(request.get(1));
				return;
			}
			if (!user.isLogged()) {
				this.notAllowedInThisState();
			} else {

				this.mails.addAll(this.getAddedMails(this.users));

				List<Mail> mailList = this.getUnmarkedMails(this.mails);
				Collections.sort(mailList);
				if (num <= mailList.size()) {
					writeToClient("+OK " + num + " "
							+ mailList.get(num - 1).getOctets());
				} else {
					writeToClient("-ERR message not found");
				}

			}
		} else if (!user.isLogged()) {
			this.notAllowedInThisState();
		} else if (request.size() == 1 && user.isLogged()) {

			this.mails.addAll(this.getAddedMails(this.users));

			List<Mail> mailList = this.getUnmarkedMails(this.mails);
			Collections.sort(mailList);
			writeToClient("+OK " + mailList.size() + " " + this.getOctets());
			int i = 1;
			for (Mail mail : mailList) {
				writeToClient(i + " " + mail.getOctets());
				i++;
			}
			writeToClient(".");
		}
	}

	private void dele(List<String> request) throws IOException {
		if (request.size() > 1) {

			int num = 0;
			try {
				num = Integer.parseInt(request.get(1));
			} catch (Exception e) {
				invalidSequence(request.get(1));
				return;
			}
			if (!user.isLogged()) {
				this.notAllowedInThisState();
			} else {

				this.mails.addAll(this.getAddedMails(this.users));

				List<Mail> mailList = this.getUnmarkedMails(this.mails);
				Collections.sort(mailList);

				if (num <= mailList.size()) {
					mailList.get(num - 1).setMarked(true);
					writeToClient("+OK message marked");
				} else {
					writeToClient("-ERR noch such message");
				}

			}

		} else if (request.size() == 1) {
			this.argumentMissing();
		}
	}

	private void retr(List<String> request) throws IOException {
		if (request.size() == 1) {
			this.argumentMissing();
		} else if (request.size() > 1) {
			int num = 0;
			try {
				num = Integer.parseInt(request.get(1));
			} catch (Exception e) {
				invalidSequence(request.get(1));
				return;
			}
			if (user.isLogged()) {

				this.mails.addAll(this.getAddedMails(this.users));
				List<Mail> unmarked = this.getUnmarkedMails(this.mails);
				Collections.sort(unmarked);

				if (num <= unmarked.size()) {
					writeToClient("+OK " + num + " "
							+ unmarked.get(num - 1).getOctets());

					FileReader fis = new FileReader(unmarked.get(num - 1)
							.getFile());
					BufferedReader reader = new BufferedReader(fis);
					String line = null;
					while ((line = reader.readLine()) != null) {
						if (line.startsWith(".")) {
							writeToClient("." + line);
						} else {
							writeToClient(line);
						}
					}

					writeToClient(".");
					reader.close();
				}

			} else {
				this.notAllowedInThisState();
			}

		}

	}

	private List<String> getCommandAndParam(String request) {
		List<String> stringArr = new ArrayList<>();

		if (request.length() == COMMAND_LENGTH) {
			stringArr.add(request.substring(0, 4).toLowerCase());
		} else if (request.length() > COMMAND_LENGTH
				&& request.charAt(POSITON_OF_WHITESPACE) == (WHITESPACE)) {
			stringArr.add(request.substring(0, 4).toLowerCase());
			stringArr.add(request.substring(POSITON_OF_WHITESPACE + 1,
					request.length()));
		} else {
			stringArr.add(request);
		}

		return stringArr;
	}

	private void notAllowedInThisState() throws IOException {
		writeToClient("-ERR command not allowed in this state");
	}

	private void argumentMissing() throws IOException {
		writeToClient("-ERR argument missing");
	}

	private void tooManyArguments() throws IOException {
		writeToClient("-ERR too many arguments");
	}

	private long getOctets() {
		long octets = 0;
		for (Mail mail : mails) {
			if (!mail.isMarked()) {
				octets += mail.getFile().length();
			}
		}
		return octets;
	}

	private Set<Mail> getAddedMails(List<User> users) {
		Set<Mail> mails = new HashSet<>();

		// TODO check if mail all mails are available on the file system

		for (User user : users) {
			mails.addAll(Factory.getAllMails(user.getMailFolder()));
		}
		return mails;
	}

	private void invalidSequence(String request) throws IOException {
		writeToClient("-ERR invalid sequence number: " + request);
	}

	private int numberOfMails(Set<Mail> mails) {
		int num = 0;
		for (Mail mail : mails) {
			if (!mail.isMarked()) {
				num++;
			}
		}
		return num;
	}

	private List<Mail> getUnmarkedMails(Set<Mail> mails) {
		List<Mail> unmarked = new ArrayList<>();
		for (Mail mail : mails) {
			if (!mail.isMarked()) {
				unmarked.add(mail);
			}
		}
		return unmarked;
	}
}
