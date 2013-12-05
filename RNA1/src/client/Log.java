package client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class Log {
	private boolean debug;
	private File logFile;
	private FileWriter writer;

	public Log(boolean debug, File logFile) throws IOException {
		this.debug = debug;
		this.logFile = logFile;
		this.writer = new FileWriter(logFile, true);
	}

	private String getCurrentTime() {
		Calendar cal = Calendar.getInstance();
		return "[" + cal.getTime().toString() + "] ";
	}

	public void writeSession(String request) throws IOException {
		if (debug) {
			String message = getCurrentTime() + request
					+ System.lineSeparator();
			writer.write(message);
			writer.flush();
		}

	}

	public void close() throws IOException {
		writer.close();
	}

	public boolean getDebugMode() {
		return this.debug;
	}

	public File getLogFile() {
		return logFile;
	}

}
