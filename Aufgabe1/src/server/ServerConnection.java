package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.Socket;



// ArbeitsThread
public class ServerConnection implements Runnable {

	private int name;
	private BufferedReader inFromClient;
	private DataOutputStream outToClient;
	private Socket socket;
	boolean serviceRequested = true; // Arbeitsthread beenden?
	
	private final String COMMAND_NOT_FOUND = "ERROR: Command not found.";
	private final char WHITESPACE = ' ';
	
	private final int COMMAND_LENGTH = 4;
	private final int WHITESPACE_POSITION = 4;
	
	public ServerConnection(int num, Socket sock) {
		/* Konstruktor */
		this.name = num;
		this.socket = sock;
	}


	@Override
	public void run() {
		String inputString;
		String command;
		String parameter;

		System.out.println("TCP Server Thread " + name
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
					
				/* Prüft ob ein Befehl im Input vorliegt */
				} else if (inputString.length() == COMMAND_LENGTH) {
					
					/* Ruft eine Methode mit dem Namen des Befehls auf */
					callMethod(inputString.substring(0, 4));
					
				/* Prüft ob ein Befehl mit Parameter im Input vorliegt */
				} else if (inputString.length() > COMMAND_LENGTH && inputString.charAt(WHITESPACE_POSITION) == WHITESPACE) {
					command = inputString.substring(0, 4);
					parameter = inputString.substring(WHITESPACE_POSITION, inputString.length());
					/* Ruft eine Methode mit dem Namen des Befehls und den Parameter auf */
					callMethod(command, parameter);
				}
			}

			/* Socket-Streams schließen --> Verbindungsabbau */
			socket.close();
		} catch (Exception e) {
			System.err.println("Connection aborted by client!");
		}

		System.out.println("TCP Server Thread " + name + " stopped!");

	}
	
	private String readFromClient() throws IOException {
		/* Lies die nächste Anfrage-Zeile (request) vom Client */
		String request = inFromClient.readLine();
		System.out.println("TCP Server Thread detected job: " + request);
		return request;
	}

	private void writeToClient(String reply) throws IOException {
		/* Sende den String als Antwortzeile (mit newline) zum Client */
		outToClient.writeBytes(reply + '\n');
		System.out.println("TCP Server Thread " + name
				+ " has written the message: " + reply);
	}
	
	/* Findet eine Methode in dieser Klasse mit dem Namen "command" und ruft diese auf 
	 * und schreibt das Ergebnis zum Client */
	private void callMethod(String command) throws Exception {
		Method command_method = getClass().getDeclaredMethod(command);
        writeToClient((String) command_method.invoke(this));
	}
	
	/* Findet eine Methode in dieser Klasse mit dem Namen "command" und ruft diese mit
	 *  den Parametern "parameter" auf und schreibt das Ergebnis zum Client */
	private void callMethod(String command, String parameter) throws Exception {
		Method command_method = getClass().getDeclaredMethod(command, String.class);
		writeToClient((String) command_method.invoke(this, parameter));
	}
	
	
	// TODO: Implementierung aller Methoden
	private String USER(String name) {
        return name;
    }
	
	private String PASS(String password) {
        return password;
    }
	
	private String STAT() {
        return "stat_command";
    }
	
	private String LIST() {
        return "list_command";
    }
	
	private String LIST(String params) {
        return "list_command_with_params: " + params;
    }
	
	private String RETR(String params) {
        return "retr_command_with_params: " + params;
    }
	
	private String DELE(String params) {
        return "dele_command_with_params: " + params;
    }
	
	private String NOOP() {
        return "noop_command";
    }
	
	private String RSET() {
        return "rset_command";
    }
	
	private String UIDL() {
        return "uidl_command";
    }
	
	private String UIDL(String params) {
        return "uidl_command_with_params: " + params;
    }


}


