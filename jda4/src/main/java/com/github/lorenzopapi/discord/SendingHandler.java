package com.github.lorenzopapi.discord;

import com.github.lorenzopapi.discord.utils.YoutubeVideoInfo;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.managers.AudioManager;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SendingHandler implements AudioSendHandler {

	ByteBuffer buf;
	boolean canPlay;
	int counter;
	byte[] audio;
	ArrayList<YoutubeVideoInfo> queue;
	int loops;
	int bassBoost;
	YoutubeVideoInfo info;
	AudioManager manager;
	boolean isForTheWorstApplied = false;
	float volume = 100;
	int byteSwap = 1;
	int pseudoRetro = 1;
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
		packetSize = Math.round(3840f * info.speed);
		if (info.bassBoost != 0) {
			for (int i = 0; i < info.audio.length; i++) {
				info.audio[i] += info.bassBoost;
			}
		}
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
		return (start.getSeconds() + ((start.getMinutes() + (start.getHours() * 60)) * 60)) * packetSize * (50);
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
	
	public String getEndTimestamp() {
		DateFormat format;
		String timeStamp = info.endTimestamp;
		Date time;
		if (timeStamp.split(":").length == 3) {
			format = new SimpleDateFormat("hh:mm:ss");
		} else {
			format = new SimpleDateFormat("mm:ss");
		}
		try {
			time = format.parse(timeStamp);
		} catch (Throwable err) {
			int time1 = (audio.length / packetSize) / (50);
			int hours = time1 / (60 * 60);
			int minutes = time1 / 60 - (hours * 60);
			int seconds = time1 - (minutes + (hours * 60)) * 60;
			return toStr(hours) + ":" + toStr(minutes) + ":" + toStr(seconds);
		}
		return toStr(time.getHours()) + ":" + toStr(time.getMinutes()) + ":" + toStr(time.getSeconds());
	}
	
	@Override
	public ByteBuffer provide20MsAudio() {
		buf.clear();
		byte[] sent = new byte[packetSize];
//		counter = packetSize*41;
		System.arraycopy(audio, counter, sent, 0, Math.min(packetSize, audio.length - counter - 1));
		for (int index = 0; index < sent.length; index++) {
			if (isForTheWorstApplied) {
				sent[index] = (byte) (((byte) (sent[index] % 32) / 20) * 20);
				if (index > 1 || counter > 1) audio[counter + index - 1] += audio[counter + index - 1] / 32;
			}
			sent[index] += bassBoost;
			sent[index] *= Math.min(Math.max(volume, -100) / 100f, 1);
			sent[index] = sent[(index / pseudoRetro) * pseudoRetro];
//			sent[index] = (byte) Math.sqrt(sent[index] * sent[index]);
		}
		byte[] srcSwap = Arrays.copyOf(sent, sent.length);
		for (int index = 0; index < sent.length; index++) {
			int num = byteSwap;
			if (index % num * 2 < num) {
				sent[index] = srcSwap[index + index % num];
			} else {
				sent[index] = srcSwap[index - index % num];
			}
		}
//		System.out.println(Arrays.equals(sent, srcSwap));
		buf.put(sent);
		buf.position(0);
		counter += packetSize;
		int end = audio.length;
//		if (!info.endTimestamp.equals("-1")) end = Math.min(getCounterIndex(info.endTimestamp, packetSize), end);
		if (counter >= end || getEndTimestamp().equals(getTimestamp())) canPlay = false;
		if (!canPlay) {
			loops -= 1;
			if (loops <= 0) {
				if (!queue.isEmpty()) {
					YoutubeVideoInfo info = queue.get(0);
					queue.remove(0);
					packetSize = Math.round(3840f * info.speed);
					counter = getCounterIndex(info.startTimestamp, packetSize);
					if (info.bassBoost != 0) {
						for (int i = 0; i < info.audio.length; i++) {
							info.audio[i] += info.bassBoost;
						}
					}
					loops = info.loopCount;
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
	
	private static String toStr(int time) {
		if (("" + time).length() == 1) {
			return "0" + time;
		}
		return time + "";
	}
	
	public String getTimestamp() {
		int time = (counter / packetSize) / (50);
		int hours = time / (60 * 60);
		int minutes = time / 60 - (hours * 60);
		int seconds = time - (minutes + (hours * 60)) * 60;
		return toStr(hours) + ":" + toStr(minutes) + ":" + toStr(seconds);
	}
	
	public String getStartTimestamp() {
		DateFormat format;
		String timeStamp = info.startTimestamp;
		Date time;
		if (timeStamp.split(":").length == 3) {
			format = new SimpleDateFormat("hh:mm:ss");
		} else {
			format = new SimpleDateFormat("mm:ss");
		}
		try {
			time = format.parse(timeStamp);
		} catch (Throwable err) {
			return "00:00:00";
		}
		return toStr(time.getHours()) + ":" + toStr(time.getMinutes()) + ":" + toStr(time.getSeconds());
	}
}
