package utils;

import java.io.File;
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
}
