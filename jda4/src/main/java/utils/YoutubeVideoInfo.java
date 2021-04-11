package utils;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;

public class YoutubeVideoInfo {
	public String name;
	public long viewCount;
	public AudioInputStream stream;
	public String link;
	public AudioFileFormat format;
	
	public YoutubeVideoInfo(String name, long viewCount, AudioInputStream stream, String link, AudioFileFormat format) {
		this.name = name;
		this.viewCount = viewCount;
		this.stream = stream;
		this.link = link;
		this.format = format;
	}
}
