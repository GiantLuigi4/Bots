package utils;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Objects;
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
	
	public static String read(String file) {
		File f = new File(dir + "\\" + file);
		try {
			Scanner sc = new Scanner(f);
			StringBuilder text = new StringBuilder();
			while (sc.hasNextLine()) {
				text.append(sc.nextLine()).append("\n");
			}
			sc.close();
			return text.toString();
		} catch (Throwable ignored) {
		}
		return "";
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
	
	public static ArrayList<String> listAll(String name) {
		ArrayList<String> files = new ArrayList<>();
		ArrayList<String> dirs = new ArrayList<>();
		dirs.add(dir + "\\" + name);
		while (!dirs.isEmpty()) {
			ArrayList<String> tempDirs = new ArrayList<>();
			for (String s : dirs) {
				for (File f : Objects.requireNonNull(new File(s).listFiles())) {
					if (f.isDirectory()) {
						tempDirs.add(f.toString());
					} else {
						files.add(f.toString());
					}
				}
			}
			dirs.clear();
			dirs.addAll(tempDirs);
		}
		return files;
	}
}
