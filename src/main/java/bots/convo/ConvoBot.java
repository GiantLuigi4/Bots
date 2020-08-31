package bots.convo;

import com.tfc.openAI.lang.AIInterpreter;
import groovy.lang.GroovyClassLoader;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import utils.PropertyReader;

import java.awt.*;
import java.util.HashMap;

public class ConvoBot extends ListenerAdapter {
	private static final GroovyClassLoader cl = new GroovyClassLoader();
	public static ConvoBot bot;
	public static JDA botBuilt;
	private static String id = "749811547659829258";
	private static Class<?> ai;
	private static AIInterpreter interpreter = AI.interpreter;
	private static String code = (interpreter.interpretFromFile("bots/convo/convo.ai"));
	
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
				if (event.getMessage().getContentRaw().equals("-convo:start") || event.getMessage().getContentRaw().equals("-convo:begin"))
					activeConvos.put(event.getAuthor().getId(), new ConvoStats(0, event.getChannel().getIdLong()));
				else if (event.getMessage().getContentRaw().equals("-convo:end") || event.getMessage().getContentRaw().equals("-convo:stop"))
					activeConvos.remove(event.getAuthor().getId());
				else if (event.getMessage().getContentRaw().startsWith("-convo:ignore")) ;
				else if (event.getMessage().getContentRaw().equals("-convo:help")) {
					EmbedBuilder builder = new EmbedBuilder();
					builder.setTitle("Help");
					builder.setAuthor(event.getAuthor().getName());
					builder.setColor(new Color(255, 255, 0));
					builder.addField("**-convo:help**", "Display the help message.", false);
					builder.addField("**-convo:start**/**convo:begin**", "Start a conversation.", false);
					builder.addField("**-convo:stop**/**-convo:end**", "End a conversation.", false);
					builder.addField("**-convo:ignore [text]**", "Use this to talk to people without having me speak to you.", false);
					builder.setFooter("Bot by: GiantLuigi4", "https://cdn.discordapp.com/avatars/380845972441530368/27de0e038db60752d1e8b7b4fced0f4e.png?size=128");
					event.getChannel().sendMessage(" ").embed(builder.build()).complete();
				} else if (activeConvos.containsKey(event.getAuthor().getId())) {
					if (event.getChannel().getIdLong() == activeConvos.get(event.getAuthor().getId()).channel) {
						StringBuilder message = new StringBuilder();
						for (String s : event.getMessage().getContentRaw().split("\n")) {
							for (String input : s.split("\\. ")) {
								message.append("> " + AI.respond(code, input, activeConvos.get(event.getAuthor().getId()).sentence)).append("\n");
								activeConvos.get(event.getAuthor().getId()).sentence++;
							}
						}
						message.append("In response to: ").append(event.getAuthor().getAsMention());
						event.getChannel().sendMessage(message.toString()).complete();
					}
				}
			}
		}
		super.onMessageReceived(event);
	}
}
