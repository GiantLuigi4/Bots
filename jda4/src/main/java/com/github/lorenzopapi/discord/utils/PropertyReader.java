package com.github.lorenzopapi.discord.utils;

import java.io.File;
import java.util.Scanner;

public class PropertyReader {
	public static String read(String file, String property) {
		return read(Files.get(file), property);
	}
	
	public static String read(File file, String property) {
		try {
			Scanner sc = new Scanner(file);
			String line = "";
			while (sc.hasNextLine()) {
				String read = sc.nextLine();
				if (read.startsWith(property + ":")) {
					line = read.substring((property + ":").length());
				}
			}
			sc.close();
			return line;
		} catch (Throwable ignored) {
		}
		return "";
	}
	
	public static boolean contains(String file, String property) {
		File f = Files.get(file);
		try {
			Scanner sc = new Scanner(f);
			String line = "";
			while (sc.hasNextLine()) {
				String read = sc.nextLine();
				if (read.startsWith(property + ":")) {
					line = read.substring((property + ":").length());
				}
			}
			sc.close();
			if (!line.equals("")) {
				return true;
			}
		} catch (Throwable ignored) {
		}
		return false;
	}
}
