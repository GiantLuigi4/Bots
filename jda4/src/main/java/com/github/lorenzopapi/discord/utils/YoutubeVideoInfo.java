package com.github.lorenzopapi.discord.utils;

public class YoutubeVideoInfo {
	public String name;
	public long viewCount;
	public byte[] audio;
	public String link;
	public int loopCount = 1;
	public int speed = 1;
	
	public YoutubeVideoInfo(String name, long viewCount, byte[] audio, String link) {
		this.name = name;
		this.viewCount = viewCount;
		this.audio = audio;
		this.link = link;
	}
}
