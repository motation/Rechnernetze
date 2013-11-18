package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import server.Mail;

public class Pop3Client implements Runnable {
	private List<User> users;
	private Socket socket;
	private File sessionProtocol;
	private Properties prop = new Properties();
	private FileWriter sessionWriter;
	private BufferedWriter writer;
	BufferedReader reader;
	private final String DIR = "/home/sof/mails/mails/gmx/";

	public Pop3Client(List<User> users) {
		this.users = users;
		try {
			// load a properties file and absolute dir
			prop.load(new FileInputStream(System.getProperty("user.dir")
					+ "/src/config/auth.properties"));
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
	}

	public void fetchMails() throws UnknownHostException, IOException {
		for (User user : users) {
			this.socket = new Socket(user.getServer(), user.getPort());

			// log dir

			this.sessionProtocol = new File(prop.getProperty("windowsDir")
					+ "logs/" + user.getUser());

			// write in exist file
			sessionWriter = new FileWriter(sessionProtocol, true);

			// Single point of control
			String username = "USER " + user.getUser();
			String password = "PASS " + user.getPass();

			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));

			// login and protocol
			writeSession(readFromServer());

			writeSession(username);

			writeToServer(username);

			writeSession(readFromServer());

			writeSession(password);

			writeToServer(password);

			// parse count of mails
			String numberOfMails = readFromServer();
			Matcher match = Pattern.compile("\\w*\\s(\\d*)\\s").matcher(
					numberOfMails);
			int count = 0;
			while (match.find()) {
				count = Integer.parseInt(match.group(1));
			}

			writeSession(numberOfMails);

			// fetch mails
			for (int i = 1; i <= count; i++) {

				writeToServer("UIDL " + i);
				String search = readFromServer();
				String regex = "\\s\\d*\\s(.*?)$";
				Matcher matcher = Pattern.compile(regex).matcher(search);
				String result = null;
				if (matcher.find()) {
					result = matcher.group(1);
				}

				boolean exits = false;
				
				for (Mail mail : getAllMessages()) {
					if (mail.getUidl().equals(result)) {
						exits = true;
						System.out.println("vorhanden");
					}
				}
				if (!exits) {
					System.out.println("neew");
					writeToServer("RETR " + i);

					// create dir and file in own filesystem
					File mail = new File(prop.getProperty("windowsDir")
							+ "mails/" + user.getService());
					mail.setExecutable(true);
					mail.setWritable(true);
					mail.mkdir();

					mail = new File(prop.getProperty("windowsDir") + "mails/"
							+ user.getService() + "/" + result + "--"
							+ System.nanoTime());

					// looping to get mail content till single "."
					FileWriter fileWriter = new FileWriter(mail);
					String string = readFromServer();
					while ((string = readFromServer()) != null
							&& !(string.contains(".") && string.length() == 1)) {

						fileWriter.write(string + System.lineSeparator());
						fileWriter.flush();
					}
					fileWriter.write(".");
					fileWriter.flush();
					fileWriter.close();
				}
			}

			// quit connection to mail service
			writeSession("QUIT ");
			writeToServer("QUIT ");
			writeSession(readFromServer());
			writeSession("----------------------------------------------------");

			// Connection close
			sessionWriter.close();
			writer.close();
			reader.close();
			socket.close();
		}

	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				fetchMails();
				Thread.sleep(30000);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				Thread.currentThread().interrupt();
			}

		}

	}

	private String getCurrentTime() {
		Calendar cal = Calendar.getInstance();
		return "[" + cal.getTime().toString() + "] ";
	}

	private void writeSession(String request) throws IOException {
		sessionWriter
				.write(getCurrentTime() + request + System.lineSeparator());
		sessionWriter.flush();
	}

	private void writeToServer(String request) throws IOException {
		writer.write(request + System.lineSeparator());
		writer.flush();
	}

	private String readFromServer() throws IOException {
		return reader.readLine();
	}

	private List<Mail> getAllMessages() throws IOException {
		List<Mail> allMessages = new ArrayList<>();
		try {
			for (File file : getFiles(DIR)) {
				allMessages.add(new Mail(file));
			}
		} catch (Exception e) {
			allMessages.clear();
			return allMessages;
		}
		// Collections.sort(allMessages);
		return allMessages;
	}

	private List<File> getFiles(String dir) throws IOException {
		List<File> files = new ArrayList<>();
		try {
			for (String fileName : new File(dir).list()) {
				files.add(new File(dir + fileName));
			}
		} catch (Exception e) {
			files.clear();
			return files;
		}
		return files;
	}

}
