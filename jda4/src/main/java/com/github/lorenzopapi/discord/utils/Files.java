package com.github.lorenzopapi.discord.utils;

import java.io.File;
import java.io.FileWriter;

public class Files {
	public static final String dir = System.getProperty("user.dir");

	public static boolean create(String file) {
		return create(file, "");
	}
	
	public static boolean create(String file, String text) {
		File f = new File(dir + File.separatorChar + file);
		if (!f.exists()) {
			try {
				f.getParentFile().mkdirs();
				f.createNewFile();
				FileWriter writer = new FileWriter(file);
				writer.write(text);
				writer.close();
				return true;
			} catch (Throwable ignored) {
			}
		}
		return false;
	}
}
