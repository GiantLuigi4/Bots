package bots.convo;

import com.tfc.openAI.lang.AIInterpreter;
import groovy.lang.GroovyClassLoader;

import java.io.InputStream;
import java.util.Random;

public class AI {
	//Idk why this doesn't error, but ok?
	protected static final AIInterpreter interpreter = new AIInterpreter();
	//AI Lang is written in groovy, and thus does not work unless loaded on a groovy classloader
	private static final GroovyClassLoader cl = new GroovyClassLoader();
	
	public static void main(String[] args) {
		try {
			Class<?> clazz = cl.loadClass("bots.convo.AI");
			clazz.getMethod("testing", String[].class).invoke(null, (Object) args);
		} catch (Throwable ignored) {
			ignored.printStackTrace();
		}
	}
	
	public static void testing(String[] args) {
		String input = "";
		InputStream stream = System.in;
		String code = (interpreter.interpretFromFile("bots/convo/AI/convo.ai"));
		System.out.println(code);
		while (!(
				input.equals("good bye") ||
						input.equals("bye") ||
						input.equals("go away") ||
						input.equals("leave") ||
						input.equals("goodbye") ||
						input.equals("-convo:end")
		)) {
			try {
				while (stream.available() == 0) ;
				byte[] bytes = new byte[stream.available()];
				stream.read(bytes);
				char[] chars = new char[bytes.length];
				for (int i = 0; i < bytes.length; i++) {
					chars[i] = (char) bytes[i];
				}
				input = new String(chars).replace("\n", "");
				System.out.println(input);
				int[][] ints = new int[1][input.length() + 1];
				for (int i = 0; i < bytes.length; i++) {
					ints[0][i] = bytes[i];
				}
				try {
					StringBuilder builder = new StringBuilder();
					interpreter.exec(code, (out) -> {
						builder.append(out.substring("key:".length()));
					}, 0, ints);
				} catch (Throwable err) {
					err.printStackTrace();
				}
			} catch (Throwable err) {
				err.printStackTrace();
			}
		}
	}
	
	public static String respond(String code, String input) {
		int[][] ints = new int[1][input.length() + 1];
		for (int i = 0; i < input.length(); i++) {
			ints[0][i] = (int) (input.charAt(i));
		}
		StringBuilder builder = new StringBuilder();
		try {
			interpreter.exec(code, (out) -> {
				builder.append(out.substring("key:".length()));
			}, 0, ints);
		} catch (Throwable ignored) {
		}
		if (builder.toString().equals("")) {
			double response = new Random().nextDouble();
			if (response <= 0.3f) {
				return "I do not know how to respond to that.";
			} else if (response <= 0.6f) {
				return "`" + input + "`?";
			} else {
				return "What does `" + input + "` mean?";
			}
		}
		return builder.toString();
	}
}
