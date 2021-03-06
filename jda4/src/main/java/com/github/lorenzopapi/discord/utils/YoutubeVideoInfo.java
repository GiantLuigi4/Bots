package com.github.lorenzopapi.discord.utils;

import com.github.lorenzopapi.discord.MusicBot;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.Objects;

public class YoutubeVideoInfo {
	public String name;
	public long viewCount;
	public byte[] audio;
	public String link;
	public int loopCount = 1;
	public float speed = 1;
	public byte bassBoost = 0;

	public String startTimestamp = "0:00";
	public String endTimestamp = "-1";
	
	public YoutubeVideoInfo(String name, long viewCount, byte[] audio, String link) {
		this.name = name;
		this.viewCount = viewCount;
		this.audio = audio;
		this.link = link;
	}
	
	public static YoutubeVideoInfo deserialize(JsonObject object) {
		try {
			//TODO Luigi I don't think that downloading the video each time the playlist is deserialized is a good idea....
			/*Yes, I know
			* We've been over this already lorenzo
			* I'm too lazy to make it store the video info to the playlist, plus download count exists*/
			YoutubeVideoInfo info = MusicBot.doYoutubeDLRequest(object.get("url").getAsString());
			info.speed = object.get("speed").getAsInt();
			info.loopCount = object.get("loop").getAsInt();
			info.endTimestamp = object.get("end").getAsString();
			info.startTimestamp = object.get("start").getAsString();
			return info;
		} catch (Throwable err) {
			err.printStackTrace();
			return null;
		}
	}
	
	@Override
	public boolean equals(Object object) {
		return false;
	}
	
	public JsonObject serialize() {
		JsonObject object = new JsonObject();
		object.addProperty("url", link.replace("https://www.youtube.com/watch?v=", "https://youtu.be/"));
		object.addProperty("start", startTimestamp);
		object.addProperty("end", endTimestamp);
		object.addProperty("loop", loopCount);
		object.addProperty("speed", speed);
		return object;
	}
	
	@Override
	public String toString() {
		return "YoutubeVideoInfo{" +
				"name='" + name + '\'' +
				", viewCount=" + viewCount +
				", bassBoost=" + bassBoost +
//				", audio=" + Arrays.toString(audio) +
				", link='" + link + '\'' +
				", loopCount=" + loopCount +
				", speed=" + speed +
				", startTimestamp='" + startTimestamp + '\'' +
				", endTimestamp='" + endTimestamp + '\'' +
				'}';
	}
	
	@Override
	public int hashCode() {
		int result = Objects.hash(name, viewCount, link, loopCount, speed, startTimestamp, endTimestamp);
		result = 31 * result + Arrays.hashCode(audio);
		return result;
	}
}
