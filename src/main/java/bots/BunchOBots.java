package bots;

import bots.convo.ConvoBot;
import bots.idle_maker.IdleMkr;
import bots.rater.PublicRaterBot;
import bots.rater.RaterBot;
import bots.role_reaction.RoleReactionBot;
import utils.Files;
import utils.PropertyReader;

import javax.security.auth.login.LoginException;

public class BunchOBots {
	public static final String drive = PropertyReader.read("settings.properties", "drive");
	
	public static void main(String[] args) throws LoginException {
		Files.create("Settings.properties", "drive:C");
		if (!Files.create("bots.properties")) {
			if (PropertyReader.contains("bots.properties", "roleReaction")) {
				RoleReactionBot.main(args);
			}
			if (PropertyReader.contains("bots.properties", "rbPrivate")) {
				try {
					RaterBot.main(args);
				} catch (Throwable err) {
					err.printStackTrace();
				}
			} else {
				if (PropertyReader.contains("bots.properties", "rbPublic")) {
					try {
						PublicRaterBot.main(args);
					} catch (Throwable err) {
						err.printStackTrace();
					}
				}
			}
			if (PropertyReader.contains("bots.properties", "idleMaker")) {
				IdleMkr.main(args);
			}
			if (PropertyReader.contains("bots.properties", "convo")) {
				ConvoBot.main(args);
			}
		}
	}
}
