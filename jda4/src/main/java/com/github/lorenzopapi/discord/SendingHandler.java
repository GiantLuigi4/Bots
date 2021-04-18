package com.github.lorenzopapi.discord;

import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.lorenzopapi.discord.utils.YoutubeVideoInfo;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class SendingHandler implements AudioSendHandler {

	ByteBuffer buf;
	boolean canPlay;
	int counter;
	byte[] audio;
	ArrayList<YoutubeVideoInfo> queue;
	/**
	 * This magic number is calculated like this:
	 * So we have an audio file that has a sample rate of 48000 sample per second
	 * Which means that there are 48 sounds per millisecond
	 * Now, the audio file has 2 channels (stereo) and the audio is 16bit signed
	 * So for each channel we have 2 bytes, 4 bytes in total
	 * In a millisecond we thus have 48 * 4 bytes of audio, which equals 192
	 * 192 * 20 = 3840, and that's exactly our magic number
	 */
	int packetSize = 3840;

	public SendingHandler(ArrayList<YoutubeVideoInfo> queue) {
		this.queue = queue;
		YoutubeVideoInfo info = queue.get(0);
		queue.remove(0);
		setup(info.audio);
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
		if (counter >= audio.length)
			canPlay = false;
		if (!canPlay) {
			YoutubeVideoInfo info = queue.get(0);
			queue.remove(0);
			setup(info.audio);
			canPlay = true;
			counter = 0;
		}
		return buf;
	}
}
