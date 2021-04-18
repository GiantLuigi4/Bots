package com.github.lorenzopapi.discord;

import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.lorenzopapi.discord.utils.YoutubeVideoInfo;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.managers.AudioManager;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SendingHandler implements AudioSendHandler {

	ByteBuffer buf;
	boolean canPlay;
	int counter;
	byte[] audio;
	ArrayList<YoutubeVideoInfo> queue;
	int loops;
	YoutubeVideoInfo info;
	AudioManager manager;
	/**
	 * This magic number is calculated like this:
	 * So we have an audio file that has a sample rate of 48000 sample per second
	 * Which means that there are 48 sounds per millisecond
	 * Now, the audio file has 2 channels (stereo) and the audio is 16bit signed
	 * So for each channel we have 2 bytes, 4 bytes in total
	 * In a millisecond we thus have 48 * 4 bytes of audio, which equals 192
	 * 192 * 20 = 3840, and that's exactly our magic number
	 */
	int packetSize;

	public SendingHandler(ArrayList<YoutubeVideoInfo> queue, AudioManager manager) {
		this.queue = queue;
		YoutubeVideoInfo info = queue.get(0);
		queue.remove(0);
		loops = info.loopCount;
		packetSize = 3840 * info.speed;
		setup(info.audio);
		counter = getCounterIndex(info.startTimestamp, packetSize);
		this.manager = manager;
		this.info = info;
	}
	
	public static int getCounterIndex(String timeStamp, int packetSize) {
		Date start = new Date(0);
		try {
			DateFormat format;
			if (timeStamp.split(":").length == 3) {
				format = new SimpleDateFormat("hh:mm:ss");
			} else {
				format = new SimpleDateFormat("mm:ss");
			}
			start = format.parse(timeStamp);
		} catch (Throwable ignored) {
		}
		System.out.println(start.getSeconds() + ((start.getMinutes() + (start.getHours() * 60)) * 60) * packetSize * 48);
		return (int) ((start.getSeconds() + ((start.getMinutes() + (start.getHours() * 60)) * 60)) * packetSize * (50.25));
	}
	
	public void setup(byte[] bytes) {
		audio = bytes;
		canPlay = true;
		counter = 0;
		buf = ByteBuffer.allocate(packetSize);
	}
	
	@Override
	public boolean canProvide() {
		return canPlay;
	}
	
	@Override
	public ByteBuffer provide20MsAudio() {
		buf.clear();
		byte[] sent = new byte[packetSize];
		System.arraycopy(audio, counter, sent, 0, Math.min(packetSize, audio.length - counter - 1));
		buf.put(sent);
		buf.position(0);
		counter += packetSize;
		int end = audio.length;
		if (!info.endTimestamp.equals("-1")) end = Math.min(getCounterIndex(info.endTimestamp, packetSize), end);
		if (counter >= end) canPlay = false;
		if (!canPlay) {
			loops -= 1;
			if (loops <= 0) {
				if (!queue.isEmpty()) {
					YoutubeVideoInfo info = queue.get(0);
					queue.remove(0);
					packetSize = 3840 * info.speed;
					setup(info.audio);
					this.info = info;
				} else {
					manager.closeAudioConnection();
				}
			}
			canPlay = true;
			counter = 0;
		}
		return buf;
	}
}
