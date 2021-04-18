package com.github.lorenzopapi.discord;

import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

public class SendingHandler implements AudioSendHandler {

	ByteBuffer buf;
	boolean canPlay;
	int counter;
	byte[] audio;
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

	public SendingHandler(byte[] bytes) {
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
		return buf;
	}
}
