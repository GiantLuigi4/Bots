package bots.convo;

import com.tfc.openAI.lang.AIInterpreter;
import groovy.lang.GroovyClassLoader;
import idek.DiscordTTT;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import utils.Files;
import utils.PropertyReader;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
		builder.setGame(Game.listening("-convo:help, GiantLuigi4, 10MLD downloads"));
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
	protected static final HashMap<String, DiscordTTT.Stats> activeGames = new HashMap<>();
	protected static final List<User> senders = new ArrayList<>();

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getJDA().getSelfUser().getId().equals(id)) {
			MessageChannel channel = event.getChannel();
			if (channel.getName().contains("bot")) {
				User author = event.getAuthor();
				String authorId = author.getId();

				String content = event.getMessage().getContentRaw();
				if (content.equals("-convo:start") || content.equals("-convo:begin")) {
					if (!activeConvos.containsKey(authorId)) {
						activeConvos.put(authorId, new ConvoStats(0, channel.getIdLong()));
						senders.add(author);
					} else if (senders.contains(author)) {
						channel.sendMessage(author.getAsMention() + ", this conversation was started from you lmao").complete();
					}
				} else if (content.equals("-convo:end") || content.equals("-convo:stop")) {
					if (activeConvos.containsKey(authorId)) {
						activeConvos.remove(authorId);
						senders.remove(author);
						channel.sendMessage(author.getAsMention()).append(" ended the conversation!").complete();
					} else {
						channel.sendMessage(author.getAsMention()).append(", you haven't started any conversation! Do -convo:start.").complete();
					}
				} else if (content.equals("-convo:brain_size"))
					channel.sendMessage("Brain size:" + Files.listAll("bots\\convo").size()).complete();
				else if (content.startsWith("-convo:train-start")) {
					channel.sendMessage("Bot training start").complete();
					channel.sendMessage("Not working yet").complete();
				} else if (content.startsWith("-convo:train-stop")) {
					channel.sendMessage("Bot training ended").complete();
				} else if (content.startsWith("-convo:sayCode")) {
					String name = content.substring("-convo:sayCode ".length());
					String code = Files.read("bots\\convo\\programmed\\" + name + "\\program.ai");
					byte[] bytes = new byte[code.length()];
					for (int i = 0; i < code.length(); i++) {
						bytes[i] = (byte) code.charAt(i);
					}
					channel.sendFile(bytes, name + ".ai").complete();
				} else if (content.equals("-convo:game")) {
					DiscordTTT ttt = new DiscordTTT();
					ttt.initialize(event);
					activeGames.put(authorId, new DiscordTTT.Stats(0, true));
				} else if (content.equals("-convo:game-end")) {
					channel.sendMessage("Game cancelled!").complete();
					activeGames.remove(authorId);
				} else if (content.startsWith("-convo:sayPy")) {
					String name = content.substring("-convo:sayPy ".length());
					String code = Files.read("bots\\convo\\programmed\\" + name + "\\program.py");
					code = interpreter.interpret(code);
					byte[] bytes = new byte[code.length()];
					for (int i = 0; i < code.length(); i++) {
						bytes[i] = (byte) code.charAt(i);
					}
					channel.sendFile(bytes, name + ".py").complete();
				} else if (content.equals("-convo:help")) {
					Random rand = new Random();
					EmbedBuilder builder = new EmbedBuilder();
					builder.setTitle("Help");
					builder.setAuthor(author.getName());
					builder.setColor(new Color(rand.nextInt(256), rand.nextInt(256), 0));
					builder.addField("**-convo:help**", "Display the help message.", false);
					builder.addField("**-convo:start**/**convo:begin**", "Start a conversation.", false);
					builder.addField("**-convo:stop**/**-convo:end**", "End a conversation.", false);
					builder.addField("**-convo:ignore [text]**", "Use this to talk to people without having me speak to you.", false);
					builder.addField("**-convo:sayCode [text]**", "I'll tell you the code (in aithon) for a specific programmed response.", false);
					builder.addField("**-convo:sayPy [text]**", "I'll tell you the code (in python) for a specific programmed response.", false);
					builder.addField("**-convo:train-start**", "Start Bot training.", false);
					builder.addField("**-convo:train-stop**", "Stops Bot training.", false);
					builder.setFooter("Bot by: GiantLuigi4", "https://cdn.discordapp.com/avatars/380845972441530368/27de0e038db60752d1e8b7b4fced0f4e.png?size=128");
					channel.sendMessage(" ").embed(builder.build()).complete();
				} else if (!authorId.equals(id) && !content.startsWith("-convo:ignore") && !activeGames.containsKey(authorId)) {
					for (User sender : senders) {
						ConvoStats currentStats = activeConvos.get(sender.getId());
						if (channel.getIdLong() == currentStats.channel && author.equals(sender)) {
							StringBuilder message = new StringBuilder();
								for (String s : content.split("\n")) {
									for (String input : s.split("\\. ")) {
									message.append("> " + AI.respond(code, input, activeConvos.get(sender.getId()).sentence)).append("\n");
									activeConvos.get(sender.getId()).sentence++;
								}
							}
							message.append("In response to: ").append(author.getAsMention());
							channel.sendMessage(message.toString()).complete();
						}
					}
				} else if (activeGames.containsKey(authorId)) {
					if (activeGames.get(authorId).started) {
						int phase = activeGames.get(authorId).phase;
						if (phase == 0) {
							if (!DiscordTTT.isBoolean(content)) {
								event.getChannel().sendMessage("I've said TRUE or FALSE, not " + content + "!!").complete();
								return;
							} else {
								DiscordTTT.twoP = Boolean.parseBoolean(content);
								activeGames.get(authorId).phase++;
							}
							if (!DiscordTTT.twoP)
								channel.sendMessage("It's YOU (X) against COMPUTER (0)").complete();
							//else
								//channel.sendMessage("It's YOU (X) against A FRIEND (0), write the name of your friend.").complete();

						//} else if (!DiscordTTT.twoP) {
							//DiscordTTT.turn1P(event, content, activeGames.get(authorId));
						/*} else if (activeGames.get(authorId).secondPlayer == null) {
							activeGames.get(authorId).secondPlayer = DiscordTTT.getMemberFromUsername(event, content);
							if (activeGames.get(authorId).secondPlayer == null) {
								channel.sendMessage("Write a valid username!!").complete();
							}
						} else {
*/
						}
						if (activeGames.get(authorId).started && !DiscordTTT.twoP)
							channel.sendMessage("Write a number from 1 (left-upper corner) to 9 (right-down corner)").complete();
					} else {
						activeGames.remove(authorId);
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
								channel.sendMessage("Joining conversation...").complete();
								activeConvos.get(lastSenderID).users.add(authorId);
								channel.sendMessage("Joined!").complete();
							} else {
								channel.sendMessage("No conversation active! Use '-convo:start' to start one").complete();
							}
						} else {
							EmbedBuilder builder = new EmbedBuilder();
							builder.setTitle("Choose Conversation");
							builder.setAuthor(author.getName());
							builder.setColor(new Color(255, 255, 0));
							builder.addField("", "There are " + activeConvos.size() + " active. (> 1)", true);
							builder.addField("", "Choose conversation to Join by writing '-convo:join conversationName'", true);
							for (User sender : senders) {
								count++;
								builder.addField("**Conversation number " + count + "**", "Started by " + sender.getAsMention(), true);
							}
							builder.setFooter("Bot by: GiantLuigi4", "https://cdn.discordapp.com/avatars/380845972441530368/27de0e038db60752d1e8b7b4fced0f4e.png?size=128");
							channel.sendMessage(" ").embed(builder.build()).complete();
						}
					} else {
						channel.sendMessage("You are already in a conversation!!").complete();
					}
				} else {
					channel.sendMessage("test").complete();
				}*/
