package bots.convo;

import java.io.InputStream;

public class AI {
	public static void main(String[] args) {
		String input = "";
		InputStream stream = System.in;
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
			} catch (Throwable ignored) {}
		}
	}
}
