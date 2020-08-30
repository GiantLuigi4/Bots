package utils;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Files {
	public static final String dir = System.getProperty("user.dir");
	
	public static boolean create(String file) {
		File f = new File(dir + "\\" + file);
		if (!f.exists()) {
			try {
				f.getParentFile().mkdirs();
				f.createNewFile();
				return true;
			} catch (Throwable ignored) {
			}
		}
		return false;
	}
	
	public static boolean create(String file, String text) {
		File f = new File(dir + "\\" + file);
		if (!f.exists()) {
			try {
				f.getParentFile().mkdirs();
				f.createNewFile();
				FileWriter writer = new FileWriter(f);
				writer.write(text);
				writer.close();
				return true;
			} catch (Throwable ignored) {
			}
		}
		return false;
	}
}
