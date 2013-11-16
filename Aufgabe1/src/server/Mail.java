package server;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mail implements Comparable{
	private String uidl;
	private File file;
	private long createTime;
	private boolean marked;
	private String fileSrc;
	private String fileName;

	public Mail(File file) {
		this.file = file;
		this.marked = false;
		this.fileSrc = file.getPath();
		this.fileName = file.getName();
		Matcher match = Pattern.compile("(.*?)[-]{2}([^\\s]*)").matcher(
				fileName);
		try {
			uidl = match.group(1);
			createTime = Long.parseLong(match.group(2));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public String getFileSrc() {
		return fileSrc;
	}

	public String getUidl() {
		return uidl;
	}

	public File getFile() {
		return file;
	}

	public long getCreateTime() {
		return createTime;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked() {
		this.marked = true;
	}

	public void unsetMarked() {
		this.marked = false;
	}

	public String getFileName() {
		return fileName;
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}