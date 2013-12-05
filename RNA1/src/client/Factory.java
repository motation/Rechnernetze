package client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Factory {
	public static List<Mail> getAllMails(String dir) {
		List<Mail> mails = new ArrayList<>();
		for (File file : getAllFiles(dir)) {
			mails.add(new Mail(file));
		}
		return mails;
	}

	public static List<File> getAllFiles(String dir) {
		List<File> files = new ArrayList<>();
		for (String path : new File(dir).list()) {
			files.add(new File(dir + "/" + path));
		}
		return files;
	}
}
