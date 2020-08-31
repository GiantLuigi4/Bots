package bots.convo;

import com.tfc.openAI.lang.AIInterpreter;
import groovy.lang.GroovyClassLoader;
import utils.Files;
import utils.PropertyReader;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;
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
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}
	
	public static void testing(String[] args) {
		String input = "";
		InputStream stream = System.in;
		String code = (interpreter.interpretFromFile("bots/convo/convo.ai"));
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
					System.out.println(builder.toString());
				} catch (Throwable err) {
					err.printStackTrace();
				}
			} catch (Throwable err) {
				err.printStackTrace();
			}
		}
	}
	
	public static String respond(String code, String input, int sentenceNumber) {
		if (input.toLowerCase().equals("what are you doing?")) {
			switch (ConvoBot.activeConvos.size()) {
				case 1:
					return "Having a conversation.";
				case 2:
					return "Having two conversations.";
				case 3:
					return "Having three conversations.";
				case 4:
					return "Having four conversations.";
				case 5:
					return "Having five conversations.";
				case 6:
					return "Having six conversations.";
				case 7:
					return "Having seven conversations.";
				case 8:
					return "Having eight conversations.";
				case 9:
					return "Having nine conversations.";
				case 10:
					return "Having ten conversations.";
				default:
					return "Having " + ConvoBot.activeConvos.size() + " conversations at once";
			}
		}
		for (File f : Objects.requireNonNull(Files.get("bots\\convo\\simple").listFiles())) {
			File inputs = new File(f.getPath() + "\\in.list");
			File outputs = new File(f.getPath() + "\\out.list");
			File info = new File(f.getPath() + "\\info.properties");
			String[] inputsArray = Files.readArray(inputs);
			boolean caseSensitive = Boolean.parseBoolean(PropertyReader.read(info, "caseSensitive"));
			String ends = PropertyReader.read(info, "validEnds");
			String check = caseSensitive ? input : input.toLowerCase();
			String msg = "";
			for (String in : inputsArray) {
				if (ends.equals("")) {
					if (in.startsWith(check)) {
						Random rng = new Random();
						String[] out = Files.readArray(outputs);
						msg = out[rng.nextInt(out.length)];
						break;
					}
				} else {
					if (in.startsWith(check)) {
						Random rng = new Random();
						String[] out = Files.readArray(outputs);
						msg = out[rng.nextInt(out.length)];
						break;
					}
					for (char c : ends.toCharArray()) {
						if ((in + c).equals(check)) {
							Random rng = new Random();
							String[] out = Files.readArray(outputs);
							msg = out[rng.nextInt(out.length)];
							break;
						}
					}
				}
				if (!msg.equals("")) break;
			}
			if (msg != null && !msg.equals("")) {
				if (sentenceNumber == 0) {
					Random rng = new Random();
					String[] out = Files.readArray("bots\\convo\\grammar\\outputs\\ask_doing.grammar");
					msg += "\n> " + out[rng.nextInt(out.length)];
				}
				return msg;
			}
		}
		int[][] ints = new int[1][input.length() + 1];
		for (int i = 0; i < input.length(); i++) ints[0][i] = input.charAt(i);
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
