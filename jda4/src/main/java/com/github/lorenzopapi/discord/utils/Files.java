package com.github.lorenzopapi.discord.utils;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Files {
	public static final String dir = System.getProperty("user.dir");

	public static boolean create(String file) {
		return create(file, "");
	}
	
	public static boolean create(String file, String text) {
		File f = get(file);
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

	public static String read(String file) {
		return read(get(file));
	}

	public static String read(File file) {
		try {
			Scanner sc = new Scanner(file);
			StringBuilder builder = new StringBuilder();
			while (sc.hasNextLine()) {
				builder.append(sc.nextLine()).append("\n");
			}
			sc.close();
			return builder.toString();
		} catch (Throwable ignored) {
		}
		return "";
	}

	public static boolean write(String file, String text) {
		return write(get(file), text);
	}

	public static boolean write(File file, String text) {
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(text);
			writer.close();
			return true;
		} catch (Throwable ignored) {
		}
		return false;
	}

	public static File get(String file) {
		return new File(dir + File.separatorChar + file);
	}
}
