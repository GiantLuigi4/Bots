package bots.sheets;

import java.util.Objects;

public class Sheet {
	String channel;
	String id;
	
	public Sheet(String channel, String id) {
		this.channel = channel;
		this.id = id;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Sheet sheet = (Sheet) o;
		return Objects.equals(channel, sheet.channel);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(channel);
	}
	
	@Override
	public String toString() {
		return "Sheet{" +
				"channel='" + channel + '\'' +
				", id='" + id + '\'' +
				'}';
	}
}
