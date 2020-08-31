package bots.convo;

import com.tfc.openAI.lang.AIInterpreter;
import groovy.lang.GroovyClassLoader;

import java.io.InputStream;

public class AI {
	private static final AIInterpreter interpreter = new AIInterpreter();
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
		String code = (interpreter.interpretFromFile("convo.ai"));
		System.out.println(code);
		while (!(
				input.equals("good bye")||
						input.equals("bye")||
						input.equals("go away")||
						input.equals("leave")||
						input.equals("goodbye")
		)) {
			try {
				while (stream.available() == 0);
				byte[] bytes = new byte[stream.available()];
				stream.read(bytes);
				char[] chars = new char[bytes.length];
				for (int i=0;i<bytes.length;i++) {
					chars[i] = (char)bytes[i];
				}
				input = new String(chars).replace("\n","");
				System.out.println(input);
				int[][] ints = new int[1][input.length()+1];
				for (int i=0;i<bytes.length;i++) {
					ints[0][i] = bytes[i];
				}
				try {
					interpreter.exec(code, (out)->{}, 0, ints);
				} catch (Throwable err) {
					err.printStackTrace();
				}
			} catch (Throwable err) {
				err.printStackTrace();
			}
		}
	}
}
