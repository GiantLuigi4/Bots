package com.github.lorenzopapi.discord;

import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SendingHandler implements AudioSendHandler {

	ByteBuffer buf;
	boolean canPlay;
	int counter;
	byte[] audio;

	public SendingHandler(byte[] bytes) {
		audio = bytes;
		canPlay = true;
		counter = 0;
		System.out.println("E");
		System.out.println(canPlay);
		buf = ByteBuffer.allocate(3840);
	}
	
	@Override
	public boolean canProvide() {
		return canPlay;
	}
	
	@Override
	public ByteBuffer provide20MsAudio() {
		buf.clear();
		byte[] sent = new byte[3840];
		System.out.println(counter);
		System.arraycopy(audio, counter, sent, 0, Math.min(3840, audio.length - counter - 1));
		counter += 3840;
		if (counter >= audio.length) {
			canPlay = false;
		}
		buf.put(sent);
		buf.position(0);
		return buf;
	}
}
