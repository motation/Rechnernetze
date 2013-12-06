package client;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mail implements Comparable<Mail> {
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

	public void setMarked(boolean value) {
		this.marked = value;
	}

	public boolean isMarked() {
		return this.marked;
	}

	@Override
	public int compareTo(Mail o) {
		return Long.compare(this.getCreateTime(), o.getCreateTime());
	}

	public long getOctets() {
		return this.getFile().length();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (createTime ^ (createTime >>> 32));
		result = prime * result + ((uidl == null) ? 0 : uidl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mail other = (Mail) obj;
		if (createTime != other.createTime)
			return false;
		if (uidl == null) {
			if (other.uidl != null)
				return false;
		} else if (!uidl.equals(other.uidl))
			return false;
		return true;
	}

}
