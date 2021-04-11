package com.github.lorenzopapi.discord;

import net.dv8tion.jda.api.audio.AudioSendHandler;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SendingHandler implements AudioSendHandler {

	AudioInputStream stream;
	public SendingHandler(AudioInputStream stream) {
		this.stream = stream;
	}

	@Override
	public boolean canProvide() {
		return true;
	}

	@Override
	public ByteBuffer provide20MsAudio() {
		ByteBuffer buf = null;
		try {
			buf = ByteBuffer.allocate(stream.getFormat().getFrameSize() * 20);
			byte[] bytes = new byte[stream.getFormat().getFrameSize() * 20];
			stream.read(bytes);
			buf.put(bytes);
		} catch (IOException ignored) {}
		return buf;
	}
}
