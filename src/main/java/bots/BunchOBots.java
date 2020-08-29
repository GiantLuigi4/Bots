package bots;

import bots.idle_maker.IdleMkr;
import bots.rater.RaterBot;
import bots.role_reaction.RoleReactionBot;

import javax.security.auth.login.LoginException;

public class BunchOBots {
	public static void main(String[] args) throws LoginException {
		RoleReactionBot.main(args);
		RaterBot.main(args);
		IdleMkr.main(args);
	}
}
