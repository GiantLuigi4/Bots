package bots;

import bots.idle_maker.IdleMkr;
import bots.rater.RaterBot;
import bots.role_reaction.RoleReactionBot;
import utils.PropertyReader;

import javax.security.auth.login.LoginException;

public class BunchOBots {
	public static final String drive = PropertyReader.read("settings.properties","drive");
	
	public static void main(String[] args) throws LoginException {
		RoleReactionBot.main(args);
		RaterBot.main(args);
		IdleMkr.main(args);
	}
}
