package tfc.bots.quote;

import com.github.lorenzopapi.discord.utils.Files;
import com.github.lorenzopapi.discord.utils.PropertyReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class QuoteBot extends ListenerAdapter {
	private static JDA bot;
	
	private static final Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
	private static final JsonObject quotesJson;
	private static final File quotes = new File("bots/quotes.json");
	
	static {
		try {
			if (!quotes.exists()) {
				quotes.getParentFile().mkdirs();
				quotes.createNewFile();
				quotesJson = new JsonObject();
				writeQuotes();
			} else {
				FileInputStream inputStream = new FileInputStream(quotes);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				int b; while ((b = inputStream.read()) != -1) outputStream.write(b);
				inputStream.close();
				String text = outputStream.toString();
				outputStream.close();
				outputStream.flush();
				quotesJson = gson.fromJson(text, JsonObject.class);
			}
		}catch (Throwable err) {
			if (err instanceof RuntimeException) throw (RuntimeException) err;
			throw new RuntimeException(err);
		}
	}
	
	public static void main(String[] args) throws LoginException, InterruptedException {
		Files.create("Settings.properties", "drive:C");
		if (!Files.create("bots.properties")) {
			if (PropertyReader.contains("bots.properties", "quoteBot")) {
				JDABuilder builder = JDABuilder.createLight(PropertyReader.read("bots.properties", "quoteBot"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
//						.enableIntents(GatewayIntent.GUILD_MEMBERS)
//						.setMemberCachePolicy(MemberCachePolicy.ALL)
						.addEventListeners(new QuoteBot())
						.setActivity(Activity.watching("for !quote"));
				bot = builder.build();
//				Files.create(prefixesFile);
//				String content = Files.read(prefixesFile);
//				for (String line : content.split("\n")) {
//					if (line.contains(":")) {
//						line = line.trim().replace("\n", "");
//						String id = line.substring(0, line.indexOf(":"));
//						userToPrefix.put(Long.parseLong(id), PropertyReader.read(prefixesFile, id));
//					}
//				}
			} else {
				return;
			}
		} else {
			return;
		}
		
		bot.awaitReady();
	}
	
	@Override
	public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
		Message message = event.getMessage();
		String content = message.getContentRaw();
		if (content.startsWith("!quote")) {
			if (content.startsWith("!quote -a")) {
				Message referenced = event.getMessage().getReferencedMessage();
				if (referenced == null) {
					event.getChannel()
							.sendMessage("You need to reply to someone in order to quote them.")
							.reference(event.getMessage())
							.mentionRepliedUser(false)
							.complete();
					return;
				}
				String guildId = event.getGuild().getId();
				if (!quotesJson.has(guildId)) quotesJson.add(guildId, new JsonObject());
				JsonObject object = quotesJson.getAsJsonObject(guildId);
				String msgId = event.getMessage().getReferencedMessage().getId();
				ArrayList<Integer> ids = new ArrayList<>();
				for (String key : object.keySet()) {
					String quoteId = key;
					if (msgId.equals(object.getAsJsonObject(quoteId).getAsJsonPrimitive("id").getAsString())) {
						event.getChannel()
								.sendMessage("A quote with that message id already exists.")
								.reference(event.getMessage())
								.mentionRepliedUser(false)
								.complete();
						return;
					}
					ids.add(Integer.parseInt(quoteId));
				}
				ids.sort(Integer::compareTo);
				int lastKey = 0;
				for (int key : ids) {
					int newKey = key;
					if (newKey != lastKey + 1) {
						break;
					}
					lastKey = newKey;
				}
				lastKey = lastKey + 1;
				if (lastKey == 0) lastKey = 1;
				JsonObject quoteObject = new JsonObject();
				quoteObject.addProperty("id", referenced.getId());
				quoteObject.addProperty("text", referenced.getContentRaw());
				quoteObject.addProperty("quoter", message.getAuthor().getId());
				quoteObject.addProperty("quotee", referenced.getAuthor().getId());
				event.getChannel()
						.sendMessage("Added quote with id " + lastKey)
						.reference(event.getMessage().getReferencedMessage())
						.mentionRepliedUser(false)
						.complete();
				object.add(lastKey + "", quoteObject);
//				System.out.println(event.getMessage().getReferencedMessage().getContentRaw());
				writeQuotes();
			} else if (content.startsWith("!quote -r")) {
				boolean isAdmin = event.getMember().hasPermission(Permission.ADMINISTRATOR);
				String authorId = event.getAuthor().getId();
				String guildId = event.getMessage().getGuild().getId();
				String msg = content.replace("!quote -r ", "");
				for (char c : msg.toCharArray()) {
					if (!Character.isDigit(c)) {
						event.getChannel()
								.sendMessage("This command asks for an integer, for example, `!quote -r 42`.")
								.reference(event.getMessage())
								.mentionRepliedUser(false)
								.complete();
						return;
					}
				}
				JsonObject quotes = quotesJson.getAsJsonObject(guildId);
				if (!quotes.has(msg)) {
					event.getChannel()
							.sendMessage("The quote which you are trying to remove does not exist.")
							.reference(event.getMessage())
							.mentionRepliedUser(false)
							.complete();
					return;
				}
				JsonObject quoteObj = quotes.getAsJsonObject(msg);
				if (
						isAdmin ||
						quoteObj.getAsJsonPrimitive("quoter").getAsString().equals(authorId) ||
						quoteObj.getAsJsonPrimitive("quotee").getAsString().equals(authorId)
				) {
					String msgId = quoteObj.getAsJsonPrimitive("id").getAsString();
					quotes.remove(msg);
					event.getChannel()
							.sendMessage("Removed quote " + msg)
							.referenceById(msgId)
							.mentionRepliedUser(false)
							.complete();
					writeQuotes();
				} else {
					event.getChannel()
							.sendMessage("You must be the quoter, quotee, or an admin in order to remove a quote.")
							.reference(event.getMessage())
							.mentionRepliedUser(false)
							.complete();
				}
				return;
			} else {
				String guildId = event.getMessage().getGuild().getId();
				String msg = content.replace("!quote ", "");
				boolean lol = false;
				if (content.startsWith("!quote !quote")) {
					lol = true;
				}
				if (!msg.startsWith("!quote")) {
					for (char c : msg.toCharArray()) {
						if (!Character.isDigit(c)) {
							event.getChannel()
									.sendMessage("This command asks for an integer, for example, `!quote -r 42`.")
									.reference(event.getMessage())
									.mentionRepliedUser(false)
									.complete();
							return;
						}
					}
				} else {
					ArrayList<Integer> ids = new ArrayList<>();
					JsonObject quotes = quotesJson.getAsJsonObject(guildId);
					for (String key : quotes.keySet()) {
						String quoteId = key;
						ids.add(Integer.parseInt(quoteId));
					}
					msg = ids.get(new Random().nextInt(ids.size())) + "";
				}
				JsonObject quotes = quotesJson.getAsJsonObject(guildId);
				if (!quotes.has(msg)) {
					event.getChannel()
							.sendMessage("The quote which you are requesting does not exist.")
							.reference(event.getMessage())
							.mentionRepliedUser(false)
							.complete();
					return;
				}
				JsonObject quoteObj = quotes.getAsJsonObject(msg);
				{
					String msgId = quoteObj.getAsJsonPrimitive("id").getAsString();
					event.getChannel()
							.sendMessage((lol ? "Ah yes, `!quote !quote`. \n> " : "> ") + quoteObj.getAsJsonPrimitive("text").getAsString().replace("\n", "\n> "))
							.referenceById(msgId)
							.mentionRepliedUser(false)
							.complete();
				}
			}
		}
		super.onGuildMessageReceived(event);
	}
	
	private static void writeQuotes() {
		try {
			FileOutputStream outputStream = new FileOutputStream(quotes);
			outputStream.write(gson.toJson(quotesJson).getBytes());
			outputStream.close();
			outputStream.flush();
		} catch (Throwable ignored) {
		}
	}
}
