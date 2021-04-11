package utils;

import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import java.io.File;

//idk if this even does anything, I don't think it does, but idc enough to remove it
public class FFMPEGLocator extends DefaultFFMPEGLocator {
	File file = new File("bots/music/ffmpeg.exe");
	@Override
	public String getExecutablePath() {
		if (file.exists()) return file.getPath();
		else file.getParentFile().mkdirs();
		return super.getExecutablePath();
	}
}
