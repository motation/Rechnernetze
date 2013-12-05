package client;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mail {
	private String uidl;
	private long createTime;
	private String fileName;
	private File file;
	private boolean marked = false;

	public Mail(File file) {
		this.file = file;
		this.fileName = this.file.getName();
		String regex = "(.*?)--(.*?)$";
		Matcher matcher = Pattern.compile(regex).matcher(this.getFileName());
		if (matcher.find()) {
			this.uidl = matcher.group(1);
			this.createTime = Long.parseLong(matcher.group(2));
		}
	}

	public String getUidl() {
		return uidl;
	}

	public void setUidl(String uidl) {
		this.uidl = uidl;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	public void setMarked(boolean value){
		this.marked = value;
	}
	
	public boolean isMarked(){
		return this.marked;
	}

}
