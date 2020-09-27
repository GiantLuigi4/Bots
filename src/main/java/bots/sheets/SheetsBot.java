package bots.sheets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import utils.Files;
import utils.PropertyReader;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class SheetsBot extends ListenerAdapter {
	private static final String APPLICATION_NAME = "Sheets Bot";
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final ArrayList<Sheet> allSheets = new ArrayList<>();
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final NetHttpTransport HTTP_TRANSPORT;
	private static final Set<String> SCOPES = SheetsScopes.all();
	public static SheetsBot bot;
	public static JDA botBuilt;
	private static String id = "759556889163333673";
	private static Guild sheetCache;
	
	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) {
		JDABuilder builder = new JDABuilder(AccountType.BOT);
		String token = PropertyReader.read("bots.properties", "sheets");
		builder.setToken(token);
		builder.setStatus(OnlineStatus.ONLINE);
		builder.setGame(Game.listening("-ideas:setup"));
		bot = new SheetsBot();
		builder.addEventListener(bot);
		try {
			botBuilt = builder.buildBlocking();
			try {
				id = botBuilt.getSelfUser().getId();
			} catch (Throwable ignored) {
				id = botBuilt.getSelfUser().getId();
			}
			sheetCache = botBuilt.getGuildById(759572178600984586L);
			for (TextChannel chnl : sheetCache.getTextChannels()) {
				System.out.println("Channel: " + chnl.getName());
				for (Message msg : chnl.getHistory().retrievePast(21).complete()) {
					System.out.println("Message: " + msg.getContentRaw());
					String[] sheet = msg.getContentRaw().split("\n");
					allSheets.add(new Sheet(sheet[0], sheet[1]));
				}
			}
		} catch (Throwable ignored) {
		}
	}
	
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(Objects.requireNonNull(Files.getAsStream("credentials.json"))));
		
		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline")
				.setApprovalPrompt("You must approve this for this to work")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		super.onMessageReceived(event);
		boolean hasIdeasPin = false;
		for (Message msg : event.getTextChannel().getPinnedMessages().complete()) {
			if (msg.getAuthor().getId().equals(id) && msg.getContentRaw().equals("This is an ideas channel, if you want to add an idea, type in \"-idea:add\" and then write your idea.")) {
				hasIdeasPin = true;
			}
		}
		boolean topicCheck = event.getTextChannel().getTopic().startsWith("\u0001");
		boolean isIdeasChannel = hasIdeasPin || topicCheck;
		if (event.getMessage().getContentRaw().startsWith("-ideas:setup")) {
			if (!isIdeasChannel) {
				if (
						event.getGuild().getMember(event.getMessage().getAuthor()).hasPermission(Permission.MANAGE_CHANNEL) ||
								event.getGuild().getMember(event.getMessage().getAuthor()).hasPermission(Permission.ADMINISTRATOR)
				) {
					if (
							!event.getGuild().getMemberById(id).hasPermission(event.getTextChannel(), Permission.MESSAGE_MANAGE) ||
									!event.getGuild().getMemberById(id).hasPermission(event.getTextChannel(), Permission.MANAGE_CHANNEL)
					) {
						Message msg = event.getChannel().sendMessage("In order to setup the channel, I need to have the ability to manage the channel.").complete();
					} else {
						Message msg = event.getChannel().sendMessage("This is an ideas channel, if you want to add an idea, type in \"-idea:add\" and then write your idea.").complete();
						try {
							event.getMessage().getChannel().pinMessageById(msg.getId()).complete();
							event.getTextChannel().getManager().setTopic("\377Submits your ideas for " + event.getMessage().getContentRaw().substring("-ideas:setup ".length()).split(" ", 1)[1] + " here!").complete();
						} catch (Throwable ignored) {
						}
						for (TextChannel chnl : sheetCache.getTextChannels()) {
							if (chnl.getGuild().getMemberById(id).hasPermission(chnl, Permission.MANAGE_CHANNEL)) {
								if (chnl.getName().startsWith("open") || chnl.getName().equals(id)) {
									if (chnl.getHistory().retrievePast(20).complete().size() <= 20) {
										chnl.sendMessage(event.getTextChannel().getAsMention() + "\n" + event.getMessage().getContentRaw().replace("-ideas:setup ", "").split(" ", 2)[0]).complete();
										if (chnl.getName().startsWith("open")) {
											chnl.sendMessage(chnl.getGuild().getMemberById(380845972441530368L).getAsMention() + " LOCK THIS CHANNEL").complete();
										}
										chnl.getManager().setName(id).complete();
										allSheets.add(new Sheet(event.getTextChannel().getAsMention(), event.getMessage().getContentRaw().replace("-ideas:setup ", "").split(" ", 2)[0]));
										break;
									}
								}
							}
						}
					}
				}
			} else {
				Message msg = event.getChannel().sendMessage("This channel is already an ideas channel.").complete();
			}
		} else if (event.getMessage().getContentRaw().startsWith("-idea:add")) {
			try {
				Sheet sheet = null;
				System.out.println(allSheets.size());
				for (Sheet sheet1 : allSheets) {
					if (sheet1.channel.equals(event.getTextChannel().getAsMention())) {
						sheet = sheet1;
					}
				}
				Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
						.setApplicationName(APPLICATION_NAME)
						.build();
				ValueRange requestBody = new ValueRange();
				Sheets.Spreadsheets.Values.Append request =
						service.spreadsheets().values().append(sheet.id, "A1", requestBody);
				request.setValueInputOption("RAW");
				request.setInsertDataOption("OVERWRITE");
				request.execute();
				event.getTextChannel().sendMessage("Added your idea to the sheet.").complete();
			} catch (Throwable error) {
				error.printStackTrace();
				String err = error.getMessage();
				for (StackTraceElement element : error.getStackTrace()) {
					err += "\n" + element.toString();
				}
				try {
					EmbedBuilder builder = new EmbedBuilder();
					builder.setTitle("An error has occurred!");
					builder.setColor(Color.RED);
					builder.addField(sheetCache.getMemberById(759556889163333673L).getAsMention(), err, false);
					event.getTextChannel().sendMessage(" ").embed(builder.build()).complete();
				} catch (Throwable error1) {
					try {
						event.getTextChannel().sendMessage(err).complete();
					} catch (Throwable ignored) {
					
					}
				}
			}
		}
	}
}
