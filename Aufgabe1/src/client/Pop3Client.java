package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pop3Client implements Runnable {
	private List<User> users;
	private Socket socket;
	private File sessionProtocol;

	public Pop3Client(List<User> users) {
		this.users = users;
	}

	public void fetchMails() throws UnknownHostException, IOException {
		for (User user : users) {
			// timestamp
			this.socket = new Socket(user.getServer(), user.getPort());
			Calendar cal = Calendar.getInstance();
			// log dir
			this.sessionProtocol = new File("D:/Mail/logs/" + user.getUser());

			FileWriter fw = new FileWriter(sessionProtocol, true);

			// Singlepoint of control
			String username = "USER " + user.getUser() + System.lineSeparator();
			String password = "PASS " + user.getPass() + System.lineSeparator();
			String time = "[" + cal.getTime().toString() + "] ";

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));

			// login and protocol
			fw.write(time + reader.readLine() + System.lineSeparator());
			fw.flush();
			fw.write(time + username);
			fw.flush();
			writer.write(username);
			writer.flush();
			fw.write(time + reader.readLine() + System.lineSeparator());
			fw.flush();
			fw.write(time + password);
			fw.flush();
			writer.write(password);
			writer.flush();

			// parse count of mails
			String mails = reader.readLine();
			Matcher match = Pattern.compile("\\w*\\s(\\d*)\\s").matcher(mails);
			int count = 0;
			while (match.find()) {
				count = Integer.parseInt(match.group(1));
			}

			fw.write(time + mails + System.lineSeparator());
			fw.flush();

			// TODO fetch mails

			for (int i = 1; i <= count; i++) {
				String string = null;
				fw.write(time + "RETR " + i + System.lineSeparator());
				fw.flush();
				writer.write("RETR " + i + System.lineSeparator());
				writer.flush();
				while ((string = reader.readLine()).startsWith(".")) {
					System.out.println(string);
				}
			}

			fw.write(time + "QUIT " + System.lineSeparator());
			fw.flush();
			writer.write("QUIT " + System.lineSeparator());
			writer.flush();
			fw.write(time + reader.readLine() + System.lineSeparator());
			fw.write("----------------------------------------------------"
					+ System.lineSeparator());
			fw.flush();

			// Connection close
			fw.close();
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
}
