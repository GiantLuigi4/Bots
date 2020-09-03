package bots.convo;

import utils.Files;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Learning {
	public static String[] convoTest = new String[]{
			"when did you start learning",
			"Since <https://github.com/GiantLuigi4/Bots/commit/aece72ae19d95b175e19c4e3e083a62ea0955d76>.",
			"when did you start learning",
			"Before 0.1.",
			"when did you start learning",
			"Before the betas.",
			"how do you learn",
			"I watch conversations, and try to find patterns.",
			"how does your learning work",
			"I watch conversations, and try to find patterns.",
			"how do you learn things",
			"I watch conversations, and try to find patterns.",
	};
	
	public static void main(String[] args) {
		String prompt = "";
		HashMap<String, ArrayList<String>> responseMap = new HashMap<>();
		for (String s : convoTest) {
			if (prompt.equals("")) {
				prompt = s;
			} else {
				s = s.replace("[", ":").replace(";", "]");
				if (responseMap.containsKey(s)) {
					if (!responseMap.get(s).contains(prompt)) responseMap.get(s).add(prompt);
				} else {
					responseMap.put(s, new ArrayList<>());
					responseMap.get(s).add(prompt);
				}
				prompt = "";
			}
		}
		System.out.println(responseMap.toString());
		HashMap<String, String> outs = new HashMap<>();
		responseMap.forEach((key, value) -> {
			ArrayList<ArrayList<String>> strings = new ArrayList<>();
			for (String s1 : value) {
				String[] strings1 = s1.split(" ");
				for (int i = 0; i < strings1.length; i++) {
					if (strings.size() <= i) strings.add(new ArrayList<>());
					strings.get(i).add(strings1[i]);
				}
			}
			StringBuilder builder = new StringBuilder();
			for (ArrayList<String> stringsList : strings) {
				if (stringsList.size() == 1) {
					builder.append("[").append(stringsList.get(0)).append("]");
				} else {
					String lastString = stringsList.get(0);
					boolean allSame = true;
					for (String s : stringsList) {
						if (!lastString.equals(s)) {
							allSame = false;
							break;
						}
						lastString = s;
					}
					if (allSame) {
						builder.append("[").append(lastString).append("]");
					} else {
						lastString = stringsList.get(0);
						for (String s : stringsList) {
							if (!lastString.equals(s)) {
								builder.append("[-").append(s).append("-]");
								lastString = s;
							}
						}
					}
				}
			}
			outs.put(key, builder.toString());
		});
		System.out.println(outs);
		HashMap<String, String> ins = new HashMap<>();
		outs.forEach((out, in) -> {
			if (!ins.containsKey(in)) {
				ins.put(in, "[" + out + "]");
			} else {
				String text = ins.get(in);
				ins.replace(in, text + "\n[" + out + "]");
			}
		});
		System.out.println(ins);
		ins.forEach((in, out) -> {
			String replace = out.replace("?", "").replace("[", "").replace("]", "").replace(" ", "_").split("\n")[0];
			File inF = Files.get("bots\\convo\\memory\\" + replace + ".in.grammar");
			File outF = Files.get("bots\\convo\\memory\\" + replace + ".out.grammar");
			try {
				if (!inF.exists()) {
					inF.getParentFile().mkdirs();
					inF.createNewFile();
				}
				if (!outF.exists()) {
					outF.getParentFile().mkdirs();
					outF.createNewFile();
				}
			} catch (Throwable ignored) {
				ignored.printStackTrace();
			}
			Files.write(inF, in);
			Files.write(outF, out);
		});
	}
}
