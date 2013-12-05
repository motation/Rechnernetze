package client;

import java.io.File;
import java.io.IOException;

public class PathHandler {
	private File homeDir;
	private File sessionFile;
	private File serviceDir;

	public PathHandler(String homeDir, User user) throws IOException {
		this.homeDir = new File(homeDir);

		if (!this.homeDir.exists()) {
			this.homeDir.setExecutable(true);
			this.homeDir.setReadable(true);
			this.homeDir.mkdirs();
		}

		String sessionPath = homeDir + File.separator + "logs" + File.separator
				+ File.separator;
		this.sessionFile = new File(sessionPath + user.getName());
		if (!sessionFile.exists()) {
			if (createDir(sessionPath)) {
				sessionFile.createNewFile();
			}
		}

		String serviceDir = homeDir + File.separator + "mails" + File.separator
				+ user.getName() + File.separator;
		this.serviceDir = new File(serviceDir);
		if (!this.serviceDir.exists()) {
			this.serviceDir.mkdirs();
		}

	}

	public File getHomeDir() {
		return this.homeDir;
	}

	private boolean createDir(String dir) {
		File dirs = new File(dir);
		dirs.setExecutable(true);
		dirs.setReadable(true);
		return dirs.mkdirs();

	}

	public File getSessionFile() {
		return sessionFile;
	}

	public File getServiceDir() {
		return serviceDir;
	}

	public File getNewMail(String fileName) {
		return new File(this.serviceDir + File.separator + fileName);
	}

	public static void main(String[] args) throws IOException {
		User user = new User("s-o.fedders@gmx.de", "p!vD87#60", "pop.gmx.net", 110);
		PathHandler pa = new PathHandler("J:/", user);

		System.out.println(pa.getNewMail("hallo"));

	}

}
