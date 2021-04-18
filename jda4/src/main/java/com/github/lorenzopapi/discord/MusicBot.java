package com.github.lorenzopapi.discord;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.Format;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.github.lorenzopapi.discord.utils.Files;
import com.github.lorenzopapi.discord.utils.PropertyReader;
import com.github.lorenzopapi.discord.utils.YoutubeVideoInfo;
import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import javax.security.auth.login.LoginException;
import javax.sound.sampled.AudioFormat;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MusicBot extends ListenerAdapter {
	public static String prefixesFile = "musicBotPrefixes.properties";
	public static String extension = ".raw";
	public static String separator = Long.toHexString(System.currentTimeMillis());
	public static Map<Long, String> userToPrefix = new HashMap<>();
	public static Map<Guild, ArrayList<YoutubeVideoInfo>> queue = new HashMap<>();
	private static final File downloadCache = new File("bot_cache");
	private static JDA bot;
	private static final HashMap<String, YoutubeVideoInfo> streamsByServer = new HashMap<>();
	
	public static void main(String[] args) throws LoginException, InterruptedException {
		Files.create("Settings.properties", "drive:C");
		if (!Files.create("bots.properties")) {
			if (PropertyReader.contains("bots.properties", "musicBot")) {
				downloadCache.mkdirs();
				for (File file : downloadCache.listFiles()) {
					file.delete();
				}
				bot = JDABuilder.createLight(PropertyReader.read("bots.properties", "musicBot"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
						.enableCache(CacheFlag.VOICE_STATE)
						.addEventListeners(new MusicBot())
						.setActivity(Activity.watching("-music:help"))
						.build();
				Files.create(prefixesFile);
				String content = Files.read(prefixesFile);
				for (String line : content.split("\n")) {
					if (line.contains(":")) {
						line = line.trim().replace("\n", "");
						String id = line.substring(0, line.indexOf(":"));
						userToPrefix.put(Long.parseLong(id), PropertyReader.read(prefixesFile, id));
					}
				}
			} else {
				return;
			}
		} else {
			return;
		}
		
		bot.awaitReady();
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		if (e.getAuthor().isBot()) {
			return;
		}
		long userId = e.getMember().getIdLong();
		Message m = e.getMessage();
		String message = m.getContentRaw().toLowerCase();
		if (!queue.containsKey(e.getGuild())) {
			queue.put(e.getGuild(), new ArrayList<>());
		}
		if (!userToPrefix.containsKey(userId)) {
			userToPrefix.put(userId, "-music:");
		}
		String prefix = userToPrefix.get(userId);
		SendingHandler handler = (SendingHandler) e.getGuild().getAudioManager().getSendingHandler();
		if (message.startsWith(prefix) || message.startsWith("-music:")) {
			if (message.startsWith(prefix + "play")) {
				playSong(e, e.getGuild());
			} else if (message.startsWith(prefix + "queue")) {
				e.getChannel().sendMessage(queueBuilder(e).build()).complete();
			} else if (message.startsWith(prefix + "clear")) {
				if (e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
					queue.get(e.getGuild()).clear();
					e.getChannel().sendMessage("Queue cleared!").complete();
				} else {
					e.getChannel().sendMessage("You need the Administration permission to do that!").complete();
				}
			} else if (message.startsWith(prefix + "help") || message.startsWith("-music:help")) {
				e.getChannel().sendMessage(helpMessageBuilder(e).build()).complete();
			} else if (message.startsWith(prefix + "leave")) {
				e.getGuild().getAudioManager().closeAudioConnection();
			} else if (message.startsWith(prefix + "cp")) {
				String[] args = message.split(" ");
				if (args.length > 0 && !args[1].isEmpty()) {
					userToPrefix.replace(userId, args[1]);
					String content = Files.read(prefixesFile);
					StringBuilder newContent = new StringBuilder();
					for (String line : content.split("\n")) {
						if (line.contains(":")) {
							long parsedId = Long.parseLong(line.substring(0, line.indexOf(":")));
							if (parsedId != userId) {
								newContent.append(line).append("\n");
							}
						}
					}
					newContent.append(userId).append(":").append(args[1]);
					Files.get(prefixesFile).delete();
					Files.create(prefixesFile, newContent.toString());
				} else {
					e.getChannel().sendMessage("Prefix cannot be empty").complete();
				}
			} else if (handler != null) {
				if (message.startsWith(prefix + "pause") && handler.canPlay) {
					handler.canPlay = false;
					e.getChannel().sendMessage("Paused!").complete();
				} else if (message.startsWith(prefix + "resume") && !handler.canPlay) {
					handler.canPlay = true;
					e.getChannel().sendMessage("Resumed!").complete();
				}
			}
		}
	}
	
	private static void playSong(GuildMessageReceivedEvent e, Guild guild) {
		String[] args = e.getMessage().getContentRaw().split(" ");
		AudioManager manager = guild.getAudioManager();
		if (args.length <= 1) {
			e.getChannel().sendMessage("Please provide a link to a valid youtube video").reference(e.getMessage()).mentionRepliedUser(false).complete();
			return;
		}
		HashMap<String, String> args1 = new HashMap<>();
		if (args.length > 2) {
			String text = "";
			for (String arg : args) {
				text += arg + " ";
			}
			args1 = parseArgs(text);
		} else {
			args1.put("video", args[1]);
		}
		String video = args1.get("video");
		if (video.startsWith("<") && video.endsWith(">")) {
			video = video.substring(1, video.length() - 1);
		}
		System.out.println(video);
		VoiceChannel vc = null;
		for (VoiceChannel c : guild.getVoiceChannels()) {
			if (c.getMembers().contains(e.getMember())) {
				vc = c;
				break;
			}
		}
		if (guild.getSelfMember().getVoiceState().inVoiceChannel()) {
			System.out.println("already connected");
			try {
				YoutubeVideoInfo info = doYoutubeDLRequest(video);
				if (streamsByServer.containsKey(e.getGuild().getId()))
					streamsByServer.replace(e.getGuild().getId(), info);
				else
					streamsByServer.put(e.getGuild().getId(), info);
				if (info.viewCount == -1)
					e.getChannel().sendMessage(info.name).reference(e.getMessage()).mentionRepliedUser(false).complete();
				else
					e.getChannel().sendMessage(playingMessageBuilder(info, e, true).build()).reference(e.getMessage()).mentionRepliedUser(false).complete();
				ArrayList<YoutubeVideoInfo> infos = getQueue(guild);
				if (args1.containsKey("speed")) info.speed = Integer.parseInt(args1.get("speed"));
				if (args1.containsKey("loop")) info.loopCount = Integer.parseInt(args1.get("loop"));
				infos.add(info);
			} catch (Throwable err) {
				e.getChannel().sendMessage(errorMessageBuilder(err).build()).reference(e.getMessage()).mentionRepliedUser(false).complete();
			}
			return;
		}
		if (vc != null) {
			if (!guild.getSelfMember().hasPermission(vc, Permission.VOICE_CONNECT) || !guild.getSelfMember().hasPermission(vc, Permission.VOICE_SPEAK)) {
				e.getChannel().sendMessage("Insufficient permissions!").reference(e.getMessage()).mentionRepliedUser(false).queue();
			} else {
				manager.openAudioConnection(vc);
				bot.getDirectAudioController().connect(vc);
				e.getChannel().sendMessage("Connected successfully!").reference(e.getMessage()).mentionRepliedUser(false).queue();
				try {
					YoutubeVideoInfo info = doYoutubeDLRequest(video);
					streamsByServer.put(e.getGuild().getId(), info);
					if (info.viewCount == -1)
						e.getChannel().sendMessage(info.name).reference(e.getMessage()).mentionRepliedUser(false).complete();
					else
						e.getChannel().sendMessage(playingMessageBuilder(info, e, false).build()).reference(e.getMessage()).mentionRepliedUser(false).complete();
					ArrayList<YoutubeVideoInfo> infos = getQueue(guild);
					if (args1.containsKey("speed")) info.speed = Integer.parseInt(args1.get("speed"));
					if (args1.containsKey("loop")) info.loopCount = Integer.parseInt(args1.get("loop"));
					infos.add(info);
					manager.setSendingHandler(new SendingHandler(infos, manager));
				} catch (Throwable err) {
					e.getChannel().sendMessage(errorMessageBuilder(err).build()).reference(e.getMessage()).mentionRepliedUser(false).complete();
				}
			}
		} else {
			e.getChannel().sendMessage("Please join a voice channel!").reference(e.getMessage()).mentionRepliedUser(false).queue();
		}
	}
	
	private static EmbedBuilder playingMessageBuilder(YoutubeVideoInfo info, GuildMessageReceivedEvent e, boolean isQueueing) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(new Color(((int) Math.abs(info.name.length() * 3732.12382f)) % 255, Math.abs(Objects.hash(info.name)) % 255, Math.abs(Objects.hash(info.name.toLowerCase())) % 255));
		if (isQueueing) builder.setTitle("Added to queue: ");
		else builder.setTitle("Now Playing:");
		String url = info.link;
		builder.setDescription(info.name);
		builder.addField("Link: ", url, false);
		builder.addField("Video has: ", info.viewCount + " views", false);
		String videoId = url.substring(url.indexOf("v=") + 2, url.indexOf("&") > 0 ? url.indexOf("&") : url.length());
		builder.setThumbnail("https://i.ytimg.com/vi/%id%/hqdefault.jpg".replace("%id%", videoId));
		builder.setAuthor("Requested by: " + e.getMember().getEffectiveName(), null, e.getAuthor().getAvatarUrl());
		builder.setFooter("Bot by: GiantLuigi4 and LorenzoPapi");
		return builder;
	}

	private static EmbedBuilder queueBuilder(GuildMessageReceivedEvent e) {
		HashMap<String, String> map =  parseArgs(e.getMessage().getContentRaw().toLowerCase());
		System.out.println(map);
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(new Color(((int) Math.abs(e.hashCode() * 3732.12382f)) % 255, Math.abs(Objects.hash("queue")) % 255, Math.abs(Objects.hash("QUEUE")) % 255));
		int index = 1;
		for (YoutubeVideoInfo info : queue.get(e.getGuild())) {
			builder.addField(index + ". " + info.name, "Looped " + info.loopCount + "times\n" + "Speed: " + info.loopCount, false);
			index++;
		}
		builder.setFooter("Bot by: GiantLuigi4 and LorenzoPapi");
		return builder;
	}
	
	private static ArrayList<YoutubeVideoInfo> getQueue(Guild guild) {
		if (!queue.containsKey(guild)) queue.put(guild, new ArrayList<>());
		return queue.get(guild);
	}
	
	public static HashMap<String, String> parseArgs(String input) {
		HashMap<String, String> args = new HashMap<>();
		String[] strings = input.split(" ");
		strings[0] = "";
		for (String string : strings) {
			if (!string.isEmpty() && string.contains(":")) {
				String[] text = string.split(":", 2);
				args.put(text[0], text[1]);
			}
		}
		return args;
	}
	
	private static EmbedBuilder helpMessageBuilder(GuildMessageReceivedEvent e) {
		EmbedBuilder builder = new EmbedBuilder();
		String prefix = userToPrefix.get(e.getMember().getIdLong());
		String name = e.getMember().getEffectiveName() + e.getMember().getAsMention() + e.getMember().getColor().toString() + e.getMember().getUser().getDiscriminator();
		builder.setColor(new Color(((int) Math.abs(name.length() * 3732.12382f)) % 255, Math.abs(Objects.hash(name)) % 255, Math.abs(Objects.hash(name.toLowerCase())) % 255));
		builder.setAuthor("Requested by: " + e.getMember().getEffectiveName(), null, e.getAuthor().getAvatarUrl());
		builder.setFooter("Bot by: GiantLuigi4 and LorenzoPapi");
		builder.setThumbnail(bot.getSelfUser().getAvatarUrl());
		builder.addField(prefix + "help", "Displays this embed", false);
		builder.addField(prefix + "play [link to youtube video]", "Plays a video's audio to the voice channel which you are currently in", false);
		builder.addField(prefix + "leave", "Makes the bot leave the vc it's in", false);
		builder.addField(prefix + "resume", "Resumes the song", false);
		builder.addField(prefix + "pause", "Pauses the song", false);
		builder.addField(prefix + "cp", "Changes the prefix of the bot for the current user", false);
		return builder;
	}
	
	private static EmbedBuilder errorMessageBuilder(Throwable throwable) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(new Color(255, 0, 0));
		builder.setTitle("An error occurred:");
		builder.setDescription(throwable.getLocalizedMessage());
		boolean hitBotLine = false;
		for (StackTraceElement element : throwable.getStackTrace()) {
			hitBotLine = hitBotLine || element.toString().startsWith("com.github.lorenzopapi");
			if (hitBotLine && !element.toString().startsWith("com.github.lorenzopapi")) break;
			builder.addField("", element.toString(), false);
		}
		return builder;
	}
	
	private static YoutubeVideoInfo doYoutubeDLRequest(String url) throws IOException, YoutubeException {
		String videoId = url.substring(url.indexOf("v=") + 2, url.indexOf("&") > 0 ? url.indexOf("&") : url.length()); //lorenzo's method of getting only the video id
		try {
			//Downloads the video with YoutubeDL
			//lorenzo's implementation
			YoutubeDLRequest req = new YoutubeDLRequest(url, downloadCache.getPath());
			req.setOption("ignore-errors");
			req.setOption("extract-audio");
			req.setOption("output", "%(title)s" + separator + "%(id)s" + separator + "%(view_count)d" + separator + ".audio");
			req.setOption("retries", 10);
			boolean cached = false;
			File audioOut = new File(downloadCache, videoId + extension);
			for (File file : downloadCache.listFiles()) {
				if (file.getName().contains(videoId)) {
					cached = true;
					break;
				}
			}
			if (!cached) {
				YoutubeDLResponse response = YoutubeDL.execute(req);
				System.out.println(response.getOut());
			}
			for (File sourceAudio : downloadCache.listFiles()) {
				if (sourceAudio.getName().contains(videoId)) {
					String title = sourceAudio.getName().split(separator)[0];
					long views = Long.parseLong(sourceAudio.getName().split(separator)[2]);
					convert(sourceAudio, audioOut);
					FileInputStream stream = new FileInputStream(audioOut);
					byte[] audio = new byte[stream.available()];
					stream.read(audio);
					stream.close();
					audioOut.delete();
					return new YoutubeVideoInfo(title, views, audio, url);
				}
			}
			throw new RuntimeException("video not found");
		} catch (Throwable ex) {
			//downloads the video with YoutubeDownloader
			//luigi's implementation
			//refactored by lorenzo
			try {
				YoutubeDownloader downloader = new YoutubeDownloader();
				YoutubeVideo video = downloader.getVideo(videoId);
				Format selectedFormat = video.findFormats((format) -> format.type().equals(Format.AUDIO)).get(0);
				File src = new File(downloadCache + "/" + videoId + "." + selectedFormat.extension().value());
				if (!src.exists()) {
					video.download(selectedFormat, new File(downloadCache.getPath()), videoId);
				}
				File audioOut = new File(downloadCache + "/" + videoId + extension);
				convert(src, audioOut);
				FileInputStream stream = new FileInputStream(audioOut);
				byte[] audio = new byte[stream.available()];
				stream.read(audio);
				stream.close();
				audioOut.delete();
				return new YoutubeVideoInfo(video.details().title(), video.details().viewCount(), audio, url);
			} catch (Throwable err) {
				if (ex.getLocalizedMessage().contains("Cannot run program \"youtube-dl\"")) {
					throw err;
				}
				err.printStackTrace();
				throw err;
			}
		}
	}
	
	private static void convert(File input, File output) {
		try {
			//converts to 16bit signed pcm big endian raw audio using FFmpeg
			//lorenzo's implementation
			FFmpeg.atPath()
					.addInput(UrlInput.fromPath(input.toPath()))
					.addArguments("-f", "s16be")
					.addArguments("-ar", "48000")
					.addArguments("-acodec", "pcm_s16be")
					.addArguments("-ac", "2")
					.addOutput(UrlOutput.toPath(output.toPath()))
					.setOutputListener(System.out::println)
					.execute();
		} catch (Throwable err) {
			//https://github.com/a-schild/jave2
			//converts to 16bit signed pcm big endian raw audio using jave, in case ffmpeg is not found
			//luigi's implementation
			//refactored and fixed by LorenzoPapi
			try {
				AudioAttributes audio = new AudioAttributes();
				AudioFormat format = AudioSendHandler.INPUT_FORMAT;
				audio.setCodec("pcm_s16be");
				audio.setSamplingRate(48000);
				audio.setChannels(format.getChannels());

				EncodingAttributes attributes = new EncodingAttributes();
				attributes.setOutputFormat("s16be");
				attributes.setAudioAttributes(audio);

				Encoder encoder = new Encoder();
				encoder.encode(new MultimediaObject(input), output, attributes);
				return;
			} catch (Throwable err1) {
				err1.printStackTrace();
			}
			throw err;
		}
	}
}

