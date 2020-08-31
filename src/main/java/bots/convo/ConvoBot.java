package bots.convo;

import com.tfc.openAI.lang.AIInterpreter;
import groovy.lang.GroovyClassLoader;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import utils.PropertyReader;

import java.util.HashMap;

public class ConvoBot extends ListenerAdapter {
	private static final GroovyClassLoader cl = new GroovyClassLoader();
	public static ConvoBot bot;
	public static JDA botBuilt;
	private static String id = "749811547659829258";
	private static Class<?> ai;
	private static AIInterpreter interpreter = AI.interpreter;
	private static String code = (interpreter.interpretFromFile("bots/convo/AI/convo.ai"));
	
	public static void main(String[] args) {
		try {
			cl.loadClass("bots.convo.ConvoBot")
					.getMethod("run", String[].class)
					.invoke(null, (Object) args);
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}
	
	public static void run(String[] args) {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		String token = PropertyReader.read("bots.properties", "convo");
		builder.setToken(token);
		builder.setStatus(OnlineStatus.ONLINE);
		builder.setGame(Game.watching("for luigi or lorenzo to make me"));
		bot = new ConvoBot();
		builder.addEventListener(bot);
		try {
			botBuilt = builder.buildAsync();
			Thread.sleep(1000);
			id = botBuilt.getSelfUser().getId();
		} catch (Throwable ignored) {
		}
	}
	
	protected static final HashMap<String, ConvoStats> activeConvos = new HashMap<>();
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getJDA().getSelfUser().getId().equals(id)) {
			if (event.getChannel().getName().contains("bot")) {
				if (event.getMessage().getContentRaw().equals("-convo:start"))
					activeConvos.put(event.getAuthor().getId(), new ConvoStats(0, event.getChannel().getIdLong()));
				else if (event.getMessage().getContentRaw().equals("-convo:end"))
					activeConvos.remove(event.getAuthor().getId());
				else if (event.getMessage().getContentRaw().startsWith("-convo:ignore")) ;
				else if (activeConvos.containsKey(event.getAuthor().getId())) {
					if (event.getChannel().getIdLong() == activeConvos.get(event.getAuthor().getId()).channel) {
						event.getChannel().sendMessage(AI.respond(code, event.getMessage().getContentRaw(), activeConvos.get(event.getAuthor().getId()).sentence)).complete();
						activeConvos.get(event.getAuthor().getId()).sentence++;
					}
				}
			}
		}
		super.onMessageReceived(event);
	}
}
