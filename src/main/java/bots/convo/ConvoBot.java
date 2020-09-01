package bots.convo;

import com.tfc.openAI.lang.AIInterpreter;
import groovy.lang.GroovyClassLoader;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import utils.Files;
import utils.PropertyReader;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConvoBot extends ListenerAdapter {
	private static final GroovyClassLoader cl = new GroovyClassLoader();
	public static ConvoBot bot;
	public static JDA botBuilt;
	private static String id = "749811547659829258";
	private static Class<?> ai;
	private static final AIInterpreter interpreter = AI.interpreter;
	private static final String code = (interpreter.interpretFromFile("bots/convo/convo.ai"));

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
		builder.setGame(Game.watching("for -convo:help"));
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
	//TODO make a list of senderIDs for multiple conversations
	protected static final List<User> senders = new ArrayList<>();

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getJDA().getSelfUser().getId().equals(id)) {
			if (event.getChannel().getName().contains("bot")) {
				User author = event.getAuthor();
				String authorId = author.getId();
				String content = event.getMessage().getContentRaw();
				if (content.equals("-convo:start") || content.equals("-convo:begin")) {
					if (!activeConvos.containsKey(authorId)) {
						activeConvos.put(authorId, new ConvoStats(0, event.getChannel().getIdLong()));
						senders.add(author);
					} else if (senders.contains(author)) {
						event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", this conversation was started from you lmao").complete();
					}
				} else if (content.equals("-convo:end") || content.equals("-convo:stop")) {
					if (activeConvos.containsKey(authorId)) {
						activeConvos.remove(authorId);
						senders.remove(author);
						event.getChannel().sendMessage(event.getAuthor().getAsMention()).append(" ended the conversation!").complete();
					} else {
						event.getChannel().sendMessage(event.getAuthor().getAsMention()).append(", you haven't started any conversation! Do -convo:start.").complete();
					}
				} else if (content.equals("-convo:brain_size"))
					event.getChannel().sendMessage("Brain size:" + Files.listAll("bots\\convo").size()).complete();
				else if (content.startsWith("-convo:ignore")) ;
				else if (content.startsWith("-convo:train-start")) {
					event.getChannel().sendMessage("Bot training start").complete();
					event.getChannel().sendMessage("Not working yet").complete();
				} else if (content.startsWith("-convo:train-stop")) {
					event.getChannel().sendMessage("Bot training ended").complete();
				} else if (content.startsWith("-convo:sayCode")) {
					String name = content.substring("-convo:sayCode ".length());
					String code = Files.read("bots\\convo\\programmed\\" + name + "\\program.ai");
					byte[] bytes = new byte[code.length()];
					for (int i = 0; i < code.length(); i++) {
						bytes[i] = (byte) code.charAt(i);
					}
					event.getChannel().sendFile(bytes, name + ".ai").complete();
				} else if (content.startsWith("-convo:sayPy")) {
					String name = content.substring("-convo:sayPy ".length());
					String code = Files.read("bots\\convo\\programmed\\" + name + "\\program.ai");
					code = interpreter.interpret(code);
					byte[] bytes = new byte[code.length()];
					for (int i = 0; i < code.length(); i++) {
						bytes[i] = (byte) code.charAt(i);
					}
					event.getChannel().sendFile(bytes, name + ".ai").complete();
				} else if (content.equals("-convo:help")) {
					EmbedBuilder builder = new EmbedBuilder();
					builder.setTitle("Help");
					builder.setAuthor(event.getAuthor().getName());
					builder.setColor(new Color(255, 255, 0));
					builder.addField("**-convo:help**", "Display the help message.", false);
					builder.addField("**-convo:start**/**convo:begin**", "Start a conversation.", false);
					builder.addField("**-convo:stop**/**-convo:end**", "End a conversation.", false);
					builder.addField("**-convo:ignore [text]**", "Use this to talk to people without having me speak to you.", false);
					builder.addField("**-convo:sayCode [text]**", "I'll tell you the code (in aithon) for a specific programmed response.", false);
					builder.addField("**-convo:sayPy [text]**", "I'll tell you the code (in python) for a specific programmed response.", false);
					builder.addField("**-convo:train-start**", "Start Bot training.", false);
					builder.addField("**-convo:train-stop**", "Stops Bot training.", false);
					builder.setFooter("Bot by: GiantLuigi4", "https://cdn.discordapp.com/avatars/380845972441530368/27de0e038db60752d1e8b7b4fced0f4e.png?size=128");
					event.getChannel().sendMessage(" ").embed(builder.build()).complete();
				} else if (!authorId.equals(id)) {
					for (User sender : senders) {
						ConvoStats currentStats = activeConvos.get(sender.getId());
						if (event.getChannel().getIdLong() == currentStats.channel && author.equals(sender)) {
							StringBuilder message = new StringBuilder();
								for (String s : content.split("\n")) {
									for (String input : s.split("\\. ")) {
									message.append("> " + AI.respond(code, input, activeConvos.get(sender.getId()).sentence)).append("\n");
									activeConvos.get(sender.getId()).sentence++;
								}
							}
							message.append("In response to: ").append(event.getAuthor().getAsMention());
							event.getChannel().sendMessage(message.toString()).complete();

						}
					}
				}
			}
		}
		super.onMessageReceived(event);
	}
}


			/*
			} else if (content.startsWith("-convo:join")) {
				if (content.length() == 11) {
					if (!authorId.equals(lastSenderID)) {
						if (activeConvos.size() == 1) {
							if (activeConvos.containsKey(lastSenderID)) {
								event.getChannel().sendMessage("Joining conversation...").complete();
								activeConvos.get(lastSenderID).users.add(authorId);
								event.getChannel().sendMessage("Joined!").complete();
							} else {
								event.getChannel().sendMessage("No conversation active! Use '-convo:start' to start one").complete();
							}
						} else {
							EmbedBuilder builder = new EmbedBuilder();
							builder.setTitle("Choose Conversation");
							builder.setAuthor(event.getAuthor().getName());
							builder.setColor(new Color(255, 255, 0));
							builder.addField("", "There are " + activeConvos.size() + " active. (> 1)", true);
							builder.addField("", "Choose conversation to Join by writing '-convo:join conversationName'", true);
							for (User sender : senders) {
								count++;
								builder.addField("**Conversation number " + count + "**", "Started by " + sender.getAsMention(), true);
							}
							builder.setFooter("Bot by: GiantLuigi4", "https://cdn.discordapp.com/avatars/380845972441530368/27de0e038db60752d1e8b7b4fced0f4e.png?size=128");
							event.getChannel().sendMessage(" ").embed(builder.build()).complete();
						}
					} else {
						event.getChannel().sendMessage("You are already in a conversation!!").complete();
					}
				} else {
					event.getChannel().sendMessage("test").complete();
				}*/
