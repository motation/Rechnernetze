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
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pop3Client implements Runnable {

	private List<User> users;
	private Socket socket;
	private Properties prop = new Properties();
	private BufferedWriter writer;
	private BufferedReader reader;
	private PathHandler pathHandler;
	private Log log;

	public Pop3Client(List<User> users) {
		this.users = users;

		try {
			// load a properties file and absolute dir
			prop.load(new FileInputStream(System.getProperty("user.dir")
					+ "/src/client/auth.properties"));
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
	}

	public void fetchMails() throws IOException {
		for (User user : users) {
			// init path and log file
			this.pathHandler = new PathHandler(prop.getProperty("homedir"),
					user);

			this.log = new Log(true, pathHandler.getSessionFile());

			// socket input/output stream
			socket = new Socket(user.getServer(), user.getPort());
			writer = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			// login
			System.out.println(readFromServer());
			writeToServer("USER " + user.getName());
			System.out.println(readFromServer());
			writeToServer("PASS " + user.getPass());
			System.out.println(readFromServer());
			writeToServer("STAT");
			String statString = readFromServer();
			System.out.println(statString);

			// Parse numberOfMails to integer
			String regexNumberOfMails = "\\w*\\s(\\d*)\\s";
			int numberOfMails = 0;
			Matcher matcherNumberOfMails = Pattern.compile(regexNumberOfMails)
					.matcher(statString);
			if (matcherNumberOfMails.find()) {
				numberOfMails = Integer.parseInt(matcherNumberOfMails.group(1));
			}

			for (int i = 1; i <= numberOfMails; i++) {
				writeToServer("UIDL " + i);
				String search = readFromServer();
				String regexUidl = "\\s\\d*\\s(.*?)$";
				Matcher matcherUidl = Pattern.compile(regexUidl)
						.matcher(search);
				String resultUidl = null;
				if (matcherUidl.find()) {
					resultUidl = matcherUidl.group(1);
				}

				boolean exists = false;
				for (Mail mail : Factory.getAllMails(pathHandler
						.getServiceDir().getAbsolutePath())) {
					if (mail.getUidl().equals(resultUidl)) {
						exists = true;
					}
				}

				if (!exists) {
					writeToServer("RETR " + i);
					readFromServer();

					File newMail = pathHandler.getNewMail(resultUidl + "--"
							+ System.nanoTime());

					// looping to get mail content till single "."
					FileWriter fileWriter = new FileWriter(newMail);
					String string = readFromServer();
					while ((string = readFromServer()) != null
							&& !(string.contains(".") && string.length() == 1)) {
						fileWriter.write(string);
						fileWriter.flush();
					}

					fileWriter.close();

				}

			}

		}

		writeToServer("QUIT");
		System.out.println(readFromServer());

		writer.close();
		reader.close();
		log.close();
		socket.close();

	}

	private void writeToServer(String request) throws IOException {
		this.log.writeSession(request);
		writer.write(request + System.lineSeparator());
		writer.flush();
	}

	private String readFromServer() throws IOException {
		String message = reader.readLine();
		this.log.writeSession(message);
		return message;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				fetchMails();
			} catch (IOException e1) {
				System.out.println(e1.getMessage());
			}
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
