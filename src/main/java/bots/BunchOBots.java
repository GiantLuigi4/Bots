package bots;

import bots.convo.ConvoBot;
import bots.idle_maker.IdleMkr;
import bots.rater.PublicRaterBot;
import bots.rater.RaterBot;
import bots.role_reaction.RoleReactionBot;
import utils.Files;
import utils.PropertyReader;

public class BunchOBots {
	public static final String drive = PropertyReader.read("settings.properties", "drive");
	
	public static void main(String[] args) {
		Files.create("Settings.properties", "drive:C");
		if (!Files.create("bots.properties")) {
			if (PropertyReader.contains("bots.properties", "roleReaction")) {
				new Thread(() -> RoleReactionBot.main(args)).start();
			}
			if (PropertyReader.contains("bots.properties", "rbPrivate")) {
				new Thread(() -> {
					try {
						RaterBot.main(args);
					} catch (Throwable err) {
						err.printStackTrace();
					}
				}).start();
			} else {
				if (PropertyReader.contains("bots.properties", "rbPublic")) {
					new Thread(() -> {
						try {
							PublicRaterBot.main(args);
						} catch (Throwable err) {
							err.printStackTrace();
						}
					}).start();
				}
			}
			if (PropertyReader.contains("bots.properties", "idleMaker")) {
				new Thread(() -> IdleMkr.main(args)).start();
			}
			if (PropertyReader.contains("bots.properties", "convo")) {
				new Thread(() -> ConvoBot.main(args)).start();
			}
		}
	}
}
