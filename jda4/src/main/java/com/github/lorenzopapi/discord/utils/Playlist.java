package com.github.lorenzopapi.discord.utils;

import com.github.kiulian.downloader.model.YoutubeVideo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

public class Playlist {
	private final ArrayList<YoutubeVideoInfo> videos;
	
	public Playlist(ArrayList<YoutubeVideoInfo> videos) {
		this.videos = videos;
	}
	
	public void addVideo(YoutubeVideoInfo info) {
		videos.add(info);
	}
	public void addVideo(int index, YoutubeVideoInfo info) {
		videos.add(index, info);
	}
	
	public YoutubeVideoInfo removeVideo(int index) {
		YoutubeVideoInfo info = videos.get(index);
		videos.remove(index);
		return info;
	}
	
	public JsonObject serialize() {
		JsonObject object = new JsonObject();
		for (int index = 0; index < videos.size(); index++) object.add("" + index, videos.get(index).serialize());
		return object;
	}
	
	public static Playlist deserialize(JsonObject object) {
		YoutubeVideoInfo[] infos = new YoutubeVideoInfo[object.entrySet().size()];
		for (Map.Entry<String, JsonElement> entry : object.entrySet()) infos[Integer.parseInt(entry.getKey())] = YoutubeVideoInfo.deserialize((JsonObject) entry.getValue());
		ArrayList<YoutubeVideoInfo> infos1 = new ArrayList<>(Arrays.asList(infos));
		return new Playlist(infos1);
	}
	
	public List<YoutubeVideoInfo> getVideos() {
		return new ArrayList<>(videos);
	}
	
	@Override
	public String toString() {
		return "Playlist{" +
				"videos=" + videos +
				'}';
	}
	
	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		Playlist playlist = (Playlist) object;
		return Objects.equals(videos, playlist.videos);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(videos);
	}
}
