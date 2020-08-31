package utils;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
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
	
	public static File get(String file) {
		File f = new File(dir + "\\" + file);
		return f;
	}
	
	public static String[] readArray(String file) {
		File f = new File(dir + "\\" + file);
		try {
			Scanner sc = new Scanner(f);
			ArrayList<String> strings = new ArrayList<>();
			while (sc.hasNextLine()) {
				strings.add(sc.nextLine());
			}
			sc.close();
			String[] strings1 = new String[strings.size()];
			for (int i = 0; i < strings.size(); i++) {
				strings1[i] = strings.get(i);
			}
			return strings1;
		} catch (Throwable ignored) {
		}
		return new String[0];
	}
	
	public static String[] readArray(File file) {
		try {
			Scanner sc = new Scanner(file);
			ArrayList<String> strings = new ArrayList<>();
			while (sc.hasNextLine()) {
				strings.add(sc.nextLine());
			}
			sc.close();
			String[] strings1 = new String[strings.size()];
			for (int i = 0; i < strings.size(); i++) {
				strings1[i] = strings.get(i);
			}
			return strings1;
		} catch (Throwable ignored) {
		}
		return new String[0];
	}
}
