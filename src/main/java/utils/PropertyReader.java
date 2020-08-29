package utils;

import java.io.File;
import java.util.Scanner;

public class PropertyReader {
	private static final String dir = System.getProperty("user.dir");
	
	public static String read(String file, String property) {
		File f = new File(dir + "\\" + file);
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
			return line;
		} catch (Throwable ignored) {
		}
		return "";
	}
}
