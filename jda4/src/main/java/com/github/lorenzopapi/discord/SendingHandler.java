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
	public SendingHandler(AudioInputStream stream) {
		this.stream = stream;
	}
	
	@Override
	public boolean canProvide() {
		return true;
	}
	
	@Override
	public ByteBuffer provide20MsAudio() {
		return null;
	}
	
	@Override
	public boolean isOpus() {
		return false;
	}
}
