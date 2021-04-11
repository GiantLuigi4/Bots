package com.github.lorenzopapi.discord;

import net.dv8tion.jda.api.audio.AudioSendHandler;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SendingHandler implements AudioSendHandler {
	
	AudioInputStream stream;
	int frame = 0;
	byte[] bytes;
	boolean isDone = false;
	public SendingHandler(AudioInputStream stream) {
		this.stream = stream;
		try {
			//I hate buffered input streams - luigi
			Field field = stream.getClass().getDeclaredField("stream");
			field.setAccessible(true);
			InputStream stream1 = (InputStream) field.get(stream);
			field = stream1.getClass().getDeclaredField("buf");
			field.setAccessible(true);
			bytes = (byte[]) field.get(stream1);
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	@Override
	public boolean canProvide() {
		return true;
	}
	
	@Override
	public ByteBuffer provide20MsAudio() {
		ByteBuffer buf = null;
		try {
			buf = ByteBuffer.allocate(stream.getFormat().getFrameSize() * 20 * stream.getFormat().getSampleSizeInBits());
//			int start = frame * (stream.getFormat().getFrameSize() * 20 * stream.getFormat().getSampleSizeInBits());
//			int end = start + (stream.getFormat().getFrameSize() * 20 * stream.getFormat().getSampleSizeInBits());
//			byte[] bytes1 = new byte[end - start];
//			if (end - start >= 0) System.arraycopy(bytes, start, bytes1, 0, end - start);
//			buf.put(bytes1);
//			frame++;
		} catch (Throwable ignored) {
			ignored.printStackTrace();
		}
		return buf;
	}
	
	@Override
	public boolean isOpus() {
		return false;
	}
}
