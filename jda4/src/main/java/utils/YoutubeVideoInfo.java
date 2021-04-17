package utils;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.InputStream;

public class YoutubeVideoInfo {
	public String name;
	public long viewCount;
	public byte[] stream;
	public String link;
	
	public YoutubeVideoInfo(String name, long viewCount, byte[] stream, String link) {
		this.name = name;
		this.viewCount = viewCount;
		this.stream = stream;
		this.link = link;
	}
}
