package bots.convo;

import com.tfc.openAI.lang.AIInterpreter;
import groovy.lang.GroovyClassLoader;
import utils.Files;
import utils.PropertyReader;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

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
	
	private static AtomicInteger aiInstance = new AtomicInteger(1);
	
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
		if (true) {
			for (File f : Objects.requireNonNull(Files.listAllFolders(Files.get("bots\\convo\\simple")))) {
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
						if (check.startsWith(in)) {
							Random rng = new Random();
							String[] out = Files.readArray(outputs);
							msg = out[rng.nextInt(out.length)];
							break;
						}
					} else {
						if (check.startsWith(in)) {
							Random rng = new Random();
							String[] out = Files.readArray(outputs);
							msg = out[rng.nextInt(out.length)];
							break;
						}
						for (char c : ends.toCharArray()) {
							if (check.equals(in + c)) {
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
					Random rng = new Random();
					switch (sentenceNumber) {
						case 0:
							String[] out1 = Files.readArray("bots\\convo\\complex\\outputs\\ask_doing.grammar");
							msg += "\n> " + out1[rng.nextInt(out1.length)].replace("[", "").replace("]", "");
							break;
						case 1:
							String[] out2 = Files.readArray("bots\\convo\\complex\\outputs\\ask_up_to.grammar");
							msg += "\n> " + out2[rng.nextInt(out2.length)].replace("[", "").replace("]", "");
							break;
						default:
							break;
					}
					return msg;
				}
			}
		}
		for (File f : Objects.requireNonNull(Files.get("bots\\convo\\complex\\inputs").listFiles())) {
			String debug = "";
			try {
				String s = Files.read(f);
//				for (String s : Files.readArray(f)) {
				String parsing = "";
				try {
					parsing = (parseGrammar(input, s));
				} catch (Throwable err) {
					err.printStackTrace();
				}
				if (parsing.length() >= 1) {
					String message = "";
					if (!parsing.substring(0, parsing.length() - 1).equals("")) {
						StringBuilder parsing1 = new StringBuilder(parsing);
						if (((input.replace(".", "").replace("!", "").replace("?", "")).toLowerCase() + ' ').equals(parsing1.toString())) {
							File output = Files.get("bots\\convo\\complex\\response\\" + f.getName());
							if (output.exists()) {
								Random rng = new Random();
								String[] out = Files.readArray(output);
								message = out[rng.nextInt(out.length)].replace("[", "").replace("]", "");
								if (sentenceNumber == 1) {
									String[] out2 = Files.readArray("bots\\convo\\complex\\outputs\\ask_up_to.grammar");
									message += "\n> " + out2[rng.nextInt(out2.length)].replace("[", "").replace("]", "");
								}
								return message;
							}
							message = parsing1.toString();
						}
					}
				}
//				}
			} catch (Throwable err) {
				err.printStackTrace();
				System.out.println(debug);
			}
		}
		for (File f : Objects.requireNonNull(Files.get("bots\\convo\\programmed").listFiles())) {
			try {
				File f2 = new File(f.getPath() + "\\syntax.txt");
				String grammar = Files.read(f2);
				int percentIndex = grammar.indexOf("%");
				grammar = grammar.replace("\n", "");
				if (input.length() > percentIndex) {
					boolean matches = (percentIndex == -1 && input.startsWith(grammar)) || (percentIndex > 0 && grammar.startsWith(input.substring(0, percentIndex)));
					if (input.length() >= grammar.length() && matches) {
						if (percentIndex == -1) percentIndex = 0;
						File f3 = new File(f.getPath() + "\\program.ai");
						String code1 = Files.read(f3);
						String compiled = interpreter.interpret(code1);
//						System.out.println(compiled);
						StringBuilder builder = new StringBuilder();
						aiInstance.getAndIncrement();
						int[][] ints = new int[1][input.length() - percentIndex];
						for (int i = percentIndex; i < input.length(); i++) ints[0][i - percentIndex] = input.charAt(i);
						try {
							interpreter.exec(compiled, (out) -> builder.append(out.substring("key:".length())), aiInstance.get(), ints);
						} catch (Throwable ignored) {
						}
						aiInstance.getAndDecrement();
						if (!builder.toString().equals("")) {
							return builder.toString();
						}
					}
				}
			} catch (Throwable err) {
				err.printStackTrace();
			}
		}
		int[][] ints = new int[1][input.length() + 1];
		for (int i = 0; i < input.length(); i++) ints[0][i] = input.charAt(i);
		StringBuilder builder = new StringBuilder();
		try {
			interpreter.exec(code, (out) -> builder.append(out.substring("key:".length())), 0, ints);
		} catch (Throwable err) {
			System.out.println(code);
			try {
				Thread.sleep(1000);
			} catch (Throwable ignored) {
			}
			err.printStackTrace();
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
	
	public static String parseGrammar(String input, String s) {
		s = s.replace("\n", "");
		s = s.toLowerCase();
		input = input.replace(".", "").replace(",", "").replace("!", "").replace("?", "").toLowerCase();
		StringBuilder parsing = new StringBuilder();
		for (String s1 : s.split("\\[")) {
			if (!s1.equals("")) {
				String substring = s1.substring(1, s1.length() - 2);
				String substring1 = input;
				try {
					substring1 = input.substring(parsing.length());
				} catch (Throwable ignored) {
				}
				try {
					substring1 = input.substring(Math.abs(parsing.length()));
				} catch (Throwable ignored) {
				}
				String substring2 = substring1;
				if (substring1.indexOf(' ') != -1)
					substring2 = substring1.substring(0, substring1.indexOf(' '));
				if (s1.startsWith("-") && s1.endsWith("-]")) {
					if (substring.equals(substring2))
						parsing.append(substring2).append(" ");
				} else if (s1.startsWith("%") && s1.endsWith("%]")) {
					for (File file : Files.listAllFiles(Files.get("bots\\convo\\grammar\\" + substring.replace(".", "\\")))) {
						for (String s2 : Files.readArray(file)) {
							if (s2.equals(substring2))
								parsing.append(s2).append(' ');
						}
					}
				} else {
					String substring3 = input;
					try {
						substring3 = input.substring(parsing.length());
					} catch (Throwable ignored) {
					}
					if (substring3.startsWith(s1.replace("[", "").replace("]", "").replace("\n", ""))) {
						parsing.append(s1, 0, s1.length() - 1).append(' ');
					}
				}
			}
		}
		return parsing.toString().replace("]", "");
	}
}
