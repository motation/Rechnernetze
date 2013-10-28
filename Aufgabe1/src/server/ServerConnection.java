package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

// ArbeitsThread
public class ServerConnection implements Runnable {

	private BufferedReader inFromClient;
	private DataOutputStream outToClient;
	private Socket socket;
	private String user;
	private Properties prop = new Properties();
	boolean serviceRequested = true; // Arbeitsthread beenden?
	private final char WHITESPACE = ' ';
	private boolean loggedIn;

	private final int COMMAND_LENGTH = 4;
	private final int WHITESPACE_POSITION = 4;
	private final String SERVERNAME = "placeholder";
	private final String DIR;

	public ServerConnection(Socket sock) {
		/* Konstruktor */
		this.socket = sock;
		this.user = "";
		this.loggedIn = false;
		try {
			// load a properties file and absolute dir
			this.prop.load(new FileInputStream(System.getProperty("user.dir")
					+ "/src/config/auth.properties"));
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
		
		DIR = prop.getProperty("windowsDir")+"mails";
	}

	@Override
	public void run() {
		String inputString;
		String command;
		String parameter;

		System.out.println("TCP Server Thread " + SERVERNAME
				+ " is running until QUIT is received!");

		try {
			/* Socket-Basisstreams durch spezielle Streams filtern */
			inFromClient = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			outToClient = new DataOutputStream(socket.getOutputStream());

			while (serviceRequested) {
				/* String vom Client empfangen */
				inputString = readFromClient();

				/* Test, ob Arbeitsthread beendet werden soll */
				if (inputString.substring(0, 4).indexOf("QUIT") > -1) {
					serviceRequested = false;

					/* Pr�ft ob ein Befehl im Input vorliegt */
				} else if (inputString.length() == COMMAND_LENGTH) {
					command = inputString.substring(0, 4).toLowerCase();
					/* Ruft eine Methode mit dem Namen des Befehls auf */
					try {
						callMethod(command);
					} catch (Exception e) {
						writeToClient("Command not found.");
					}

					/* Pr�ft ob ein Befehl mit Parameter im Input vorliegt */
				} else if (inputString.length() > COMMAND_LENGTH
						&& inputString.charAt(WHITESPACE_POSITION) == WHITESPACE) {
					command = inputString.substring(0, 4).toLowerCase();
					parameter = inputString.substring(WHITESPACE_POSITION + 1,
							inputString.length());
					/*
					 * Ruft eine Methode mit dem Namen des Befehls und den
					 * Parameter auf
					 */
					try {
						callMethod(command, parameter);
					} catch (Exception e) {
						writeToClient("Command not found.");
					}
				} else {
					writeToClient("Something went wrong.");
				}
			}

			/* Socket-Streams schlie�en --> Verbindungsabbau */
			socket.close();
		} catch (Exception e) {
			System.err.println("Connection aborted by client!");
		}

		System.out.println("TCP Server Thread " + SERVERNAME + " stopped!");

	}

	private String readFromClient() throws IOException {
		/* Lies die n�chste Anfrage-Zeile (request) vom Client */
		String request = inFromClient.readLine();
		System.out.println("TCP Server Thread detected job: " + request);
		return request;
	}

	private void writeToClient(String reply) throws IOException {
		/* Sende den String als Antwortzeile (mit newline) zum Client */
		outToClient.writeBytes(reply + '\n');
		System.out.println("TCP Server Thread " + SERVERNAME
				+ " has written the message: " + reply);
	}

	/*
	 * Findet eine Methode in dieser Klasse mit dem Namen "command" und ruft
	 * diese auf und schreibt das Ergebnis zum Client
	 */
	private void callMethod(String command) throws Exception {
		Method command_method = getClass().getDeclaredMethod(command);
		command_method.invoke(this);
	}

	/*
	 * Findet eine Methode in dieser Klasse mit dem Namen "command" und ruft
	 * diese mit den Parametern "parameter" auf und schreibt das Ergebnis zum
	 * Client
	 */
	private void callMethod(String command, String parameter) throws Exception {
		Method command_method = getClass().getDeclaredMethod(command,
				String.class);
		command_method.invoke(this, parameter);
	}

	// TODO: Implementierung aller Methoden
	private void user(String name) throws IOException {
		if (user.isEmpty()) {
			user = name;
			writeToClient("+OK password required for user \"" + user + "\"");
		} else {
			writeToClient("-ERR Too many USER commands");
		}

	}

	private void pass(String password) throws IOException {
		if (user.isEmpty()) {
			writeToClient("-ERR please supply USER first");
		} else if (user.equals(prop.getProperty("userweb"))
				&& password.equals(prop.getProperty("passweb"))) {
			loggedIn = true;
			writeToClient("+OK mailbox \"" + user + "\" has "
					+ getAllMessages().size() + " messages ("
					+ getOctets(getFiles(DIR)) + " octets) " + SERVERNAME);
		} else {
			writeToClient("-ERR authentication failed");
		}
	}

	private void stat() throws IOException {
		if (loggedIn) {
			writeToClient("+OK " + getAllMessages().size() + " "
					+ getOctets(getFiles(DIR)));
		} else {
			notLoggedInMessage();
		}
	}

	private void list() throws IOException {
		if (loggedIn) {
			writeToClient("+OK scan listing follows");
			listHelper(numberOfMessages(getAllMessages()));
		} else {
			notLoggedInMessage();
		}
	}

	private void list(String params) throws IOException {
		if (loggedIn) {
			int count;
			try {
				count = Integer.parseInt(params);
			} catch (Exception e) {
				writeToClient("-ERR no such message");
				return;
			}
			int numberOfMessages = getAllMessages().size();
			if (count > numberOfMessages) {
				writeToClient("-ERR no such message, only " + numberOfMessages
						+ " messages in maildrop");
			} else {
				listHelper(count);
			}

		} else {
			notLoggedInMessage();
		}
	}

	private void retr(String params) throws IOException {
		if (loggedIn) {
			int count;
			try {
				count = Integer.parseInt(params);
			} catch (Exception e) {
				writeToClient("-ERR no such message");
				return;
			}
			File message = getAllMessages().get(count).getFile();
			writeToClient("+OK " + message.length() + " octets");
			FileReader input = new FileReader(message);
			BufferedReader reader = new BufferedReader(input);
			String line = null;
			while ((line = reader.readLine()) != null) {
				writeToClient(line);
			}
			reader.close();
			input.close();
		} else {
			notLoggedInMessage();
		}
	}

	private void dele(String params) throws IOException {
		if (loggedIn) {
			int count;
			try {
				count = Integer.parseInt(params);
			} catch (Exception e) {
				writeToClient("-ERR no such message");
				return;
			}
			List<Mail> mails = new ArrayList<>();
			mails = getAllMessages();
			if (mails.size() < count || mails.get(count).isMarked()) {
				writeToClient("-ERR message " + count + " already deleted");
			} else {
				mails.get(count).setMarked();
				writeToClient("+OK message " + count + " deleted");
			}
		} else {
			notLoggedInMessage();
		}
	}

	private void noop() throws IOException {
		if (loggedIn) {
			writeToClient("+OK");
		} else {
			notLoggedInMessage();
		}
	}

	private void rset() throws IOException {
		if (loggedIn) {
			for (Mail message : getAllMessages()) {
				if (message.isMarked()) {
					message.unsetMarked();
				}
			}
			stat();
		} else {
			notLoggedInMessage();
		}

	}

	private void uidl() throws IOException {
		if (loggedIn) {
			List<Mail> mails = new ArrayList<>();
			mails = getAllMessages();
			for (int i = 0; i < mails.size(); i++) {
				writeToClient("+OK");
				writeToClient(i + " " + mails.get(i).getUidl());
			}
		} else {
			notLoggedInMessage();
		}
	}

	private void uidl(String params) throws IOException {
		if (loggedIn) {
			int count;
			try {
				count = Integer.parseInt(params);
			} catch (Exception e) {
				writeToClient("-ERR no such message");
				return;
			}
			List<Mail> mails = new ArrayList<>();
			mails = getAllMessages();
			int size = mails.size();
			if (count > size) {
				writeToClient("-ERR no such message, only " + size
						+ " messages in maildrop");
			} else {
				writeToClient("+OK " + count + " " + mails.get(count).getUidl());
			}
		} else {
			notLoggedInMessage();
		}
	}

	// Helpers

	private void notLoggedInMessage() throws IOException {
		writeToClient("-ERR command not allowed in this state");
	}

	private List<Mail> getAllMessages() throws IOException {
		List<Mail> allMessages = new ArrayList<>();
		for (File file : getFiles(DIR)) {
			allMessages.add(new Mail(file));
		}

		Collections.sort(allMessages);
		return allMessages;
	}

	private void listHelper(int numberOfMessages) throws IOException {
		writeToClient("+OK scan listing follows");

		writeToClient("+OK " + numberOfMessages + " messages ("
				+ getOctets(getFiles(DIR)) + " octets)");
		for (int i = 0; i < numberOfMessages; i++) {
			writeToClient(i + " " + getAllMessages().get(i).getFile().length());
		}
		writeToClient(".");
	}

	private int numberOfMessages(List<Mail> mails) {
		int numberOfMessages = 0;

		for (Mail mail : mails) {

			if (!mail.isMarked())
				numberOfMessages++;
		}
		return numberOfMessages;
	}

	private List<File> getFiles(String dir) throws IOException {
		List<File> files = new ArrayList<>();
		for (String fileName : new File(dir).list()) {
			files.add(new File(dir + fileName));
		}
		return files;
	}

	private int getOctets(List<File> files) throws IOException {
		int octets = 0;
		for (File file : files) {
			octets += file.length();
		}

		return octets;
	}
}
