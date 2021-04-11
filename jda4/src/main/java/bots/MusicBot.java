// MADE BY PIZZA BOI

package bots;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.Format;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import utils.Files;
import utils.PropertyReader;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import javax.security.auth.login.LoginException;
import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MusicBot extends ListenerAdapter {
	private String prefix = "-music:";
	public static Map<Guild, ArrayList<?>> queue = new HashMap<>();
	private static final File downloadCache = new File("bot_cache");
	private static JDA bot;
	private static final HashMap<String, YoutubeVideoInfo> streamsByServer = new HashMap<>();
	
	public static void main(String[] args) throws LoginException, InterruptedException {
		Files.create("Settings.properties", "drive:C");
		if (!Files.create("bots.properties")) {
			if (PropertyReader.contains("bots.properties", "musicBot")) {
				downloadCache.mkdirs();
				bot = JDABuilder.createLight(PropertyReader.read("bots.properties", "musicBot"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
						.enableCache(CacheFlag.VOICE_STATE)
						.addEventListeners(new MusicBot())
						.setActivity(Activity.watching("-music:help"))
						.build();
			} else {
				return;
			}
		} else {
			return;
		}
		
		bot.awaitReady();
		while (true) {
			bot.getAudioManagers().forEach((manager) -> {
				manager.setSpeakingMode(SpeakingMode.SOUNDSHARE, SpeakingMode.PRIORITY);
				if (!streamsByServer.containsKey(manager.getGuild().getId())) {
//					manager.closeAudioConnection();
				} else {
					YoutubeVideoInfo info = streamsByServer.get(manager.getGuild().getId());
					byte[] bytes = new byte[info.format.getFrameLength() * 20];
					try {
						if (info != null) {
							info.stream.read(bytes);
							if (info.stream.available() <= 1) {
								streamsByServer.remove(manager.getGuild().getId());
							}
						}
						if (manager.getSendingHandler() != null) {
							manager.getSendingHandler().provide20MsAudio().put(bytes);
						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		if (e.getAuthor().isBot()) {
			return;
		}
		Message m = e.getMessage();
		String message = m.getContentRaw().toLowerCase();
		if (!queue.containsKey(e.getGuild())) {
			queue.put(e.getGuild(), new ArrayList<>());
		}
		if (message.startsWith(prefix)) {
			if (message.startsWith(prefix + "play")) {
				playSong(e, e.getGuild());
			} else if (message.startsWith(prefix + "help")) {
				e.getChannel().sendMessage(createBuilder(e).build()).complete();
			}
		}
	}
	
	private static void playSong(GuildMessageReceivedEvent e, Guild guild) {
		String[] args = e.getMessage().getContentRaw().split(" ");
		if (args[1].startsWith("<") && args[1].endsWith(">")) {
			args[1] = args[1].substring(1, args[1].length() - 1);
		}
		System.out.println(args[1]);
		VoiceChannel vc = null;
		for (VoiceChannel c : guild.getVoiceChannels()) {
			if (c.getMembers().contains(e.getMember())) {
				vc = c;
				break;
			}
		}
		if (guild.getSelfMember().getVoiceState().inVoiceChannel()) {
			try {
				YoutubeVideoInfo info = doYoutubeDLRequest(args[1]);
				if (streamsByServer.containsKey(e.getGuild().getId()))
					streamsByServer.replace(e.getGuild().getId(), info);
				else streamsByServer.put(e.getGuild().getId(), info);
				if (info.viewCount == -1) e.getChannel().sendMessage(info.name).complete();
				else e.getChannel().sendMessage(createBuilder(info, e).build()).complete();
			} catch (Throwable err) {
				e.getChannel().sendMessage(createBuilder(err).build()).complete();
			}
			return;
		}
		if (vc != null) {
			if (!guild.getSelfMember().hasPermission(vc, Permission.VOICE_CONNECT) || !guild.getSelfMember().hasPermission(vc, Permission.VOICE_SPEAK)) {
				e.getChannel().sendMessage("Insufficient permissions!").queue();
			} else {
				AudioManager manager = guild.getAudioManager();
				manager.openAudioConnection(vc);
				bot.getDirectAudioController().connect(vc);
				e.getChannel().sendMessage("Connected successfully!").queue();
				try {
					YoutubeVideoInfo info = doYoutubeDLRequest(args[1]);
					streamsByServer.put(e.getGuild().getId(), info);
					if (info.viewCount == -1) e.getChannel().sendMessage(info.name).complete();
					else e.getChannel().sendMessage(createBuilder(info, e).build()).complete();
				} catch (Throwable err) {
					e.getChannel().sendMessage(createBuilder(err).build()).complete();
				}
			}
		} else {
			e.getChannel().sendMessage("Please join a voice channel!").queue();
		}
	}
	
	private static EmbedBuilder createBuilder(YoutubeVideoInfo info, GuildMessageReceivedEvent e) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(new Color(((int) Math.abs(info.name.length() * 3732.12382f)) % 255, Math.abs(Objects.hash(info.name)) % 255, Math.abs(Objects.hash(info.name.toLowerCase())) % 255));
		builder.setTitle("Now Playing:");
		builder.setDescription(info.name);
		builder.addField("Link: ", info.link, false);
		builder.addField("Video has: ", info.viewCount + " views", false);
		String url = info.link;
		String videoId = url.substring(url.indexOf("v=") + 2, url.indexOf("&") > 0 ? url.indexOf("&") : url.length());
		builder.setThumbnail("https://i.ytimg.com/vi/%id%/hqdefault.jpg".replace("%id%", videoId));
		builder.setAuthor("Requested by: " + e.getMember().getEffectiveName(), null, e.getAuthor().getAvatarUrl());
		builder.setFooter("Bot by: GiantLuigi4 and LorenzoPapi");
		return builder;
	}
	
	private static EmbedBuilder createBuilder(GuildMessageReceivedEvent e) {
		EmbedBuilder builder = new EmbedBuilder();
		String name = e.getMember().getEffectiveName() + e.getMember().getAsMention() + e.getMember().getColor().toString() + e.getMember().getUser().getDiscriminator();
		builder.setColor(new Color(((int) Math.abs(name.length() * 3732.12382f)) % 255, Math.abs(Objects.hash(name)) % 255, Math.abs(Objects.hash(name.toLowerCase())) % 255));
		builder.setAuthor("Requested by: " + e.getMember().getEffectiveName(), null, e.getAuthor().getAvatarUrl());
		builder.setFooter("Bot by: GiantLuigi4 and LorenzoPapi");
		builder.setThumbnail(bot.getSelfUser().getAvatarUrl());
		builder.addField("-music:help", "Displays this embed", false);
		builder.addField("-music:play [link to youtube video]", "Plays a video's audio to the voice channel which you are currently in", false);
		return builder;
	}
	
	private static EmbedBuilder createBuilder(Throwable throwable) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(new Color(255, 0, 0));
		builder.setTitle("An error occurred:");
		builder.setDescription(throwable.getLocalizedMessage());
		boolean hitBotLine = false;
		for (StackTraceElement element : throwable.getStackTrace()) {
			hitBotLine = hitBotLine || element.toString().startsWith("bots");
			if (hitBotLine && !element.toString().startsWith("bots")) break;
			builder.addField("", element.toString(), false);
		}
		return builder;
	}
	
	private static YoutubeVideoInfo doYoutubeDLRequest(String url) throws UnsupportedAudioFileException, YoutubeDLException, IOException, YoutubeException, EncoderException, LineUnavailableException {
		String videoId = url.substring(url.indexOf("v=") + 2, url.indexOf("&") > 0 ? url.indexOf("&") : url.length()); //lorenzo's method of getting only the video id
		try {
			//Downloads the video with YoutubeDL
			//lorenzo's implementation
			YoutubeDLRequest req = new YoutubeDLRequest(url, downloadCache.getPath());
			req.setOption("ignore-errors");
			req.setOption("extract-audio");
			req.setOption("output", "%(id)s.audio");
			req.setOption("retries", 10);
			YoutubeDLResponse response = YoutubeDL.execute(req);
			for (File file : downloadCache.listFiles()) {
				System.out.println(file.getName());
				System.out.println(videoId);
				if (file.getName().startsWith(videoId) && file.getName().matches("(\\w+\\.\\w+)")) {
					System.out.println("E");
					File audioSrc = new File(file.getPath());
					File audioOut = new File(downloadCache, videoId + ".wav");
					convert(audioSrc, audioOut);
					break;
				}
			}
			File audioOut = new File(downloadCache.getAbsolutePath(), videoId + ".wav");
			AudioInputStream stream = AudioSystem.getAudioInputStream(audioOut);
			System.out.println(response.getOut());
			//TODO: this
			return new YoutubeVideoInfo("<@467581725695016960> ahem", -1, stream, url, AudioSystem.getAudioFileFormat(audioOut));
		} catch (YoutubeDLException | UnsupportedAudioFileException | IOException ex) {
			//downloads the video with YoutubeDownloader
			try {
				YoutubeDownloader downloader = new YoutubeDownloader();
				YoutubeVideo video = downloader.getVideo(videoId);
				Format selectedFormat = video.findFormats((format) -> format.type().equals(Format.AUDIO)).get(0);
				File src = new File(downloadCache + "/" + video.details().title() + "." + selectedFormat.extension().value());
				if (!src.exists()) {
					video.download(selectedFormat, new File(downloadCache.getPath()));
				}
				File targ = new File(downloadCache + "/" + video.details().title() + ".wav");
				convert(src, targ);
				System.out.println(AudioSendHandler.INPUT_FORMAT);
				System.out.println(AudioSystem.getAudioFileFormat(targ).getFormat());
				AudioInputStream stream = AudioSystem.getAudioInputStream(targ);
//				Clip c = AudioSystem.getClip();
//				c.open(stream);
//				c.start();
				return new YoutubeVideoInfo(video.details().title(), video.details().viewCount(), stream, url, AudioSystem.getAudioFileFormat(targ));
			} catch (Throwable err) {
				if (ex.getLocalizedMessage().contains("Cannot run program \"youtube-dl\"")) {
					throw err;
				}
				err.printStackTrace();
			}
			throw ex;
		}
	}
	
	private static void convert(File input, File output) {
		try {
			//converts to wav using FFmpeg
			//lorenzo's implementation
			FFmpeg.atPath()
					.addInput(UrlInput.fromPath(input.toPath()))
					.addOutput(UrlOutput.toPath(output.toPath()))
					.execute();
		} catch (Throwable err) {
			//https://github.com/a-schild/jave2
			//converts to wav using jave, as a fallback incase FFmpeg is not installed or throws an error
			//luigi's implementation
			try {
				AudioAttributes audio = new AudioAttributes();
				audio.setSamplingRate(48000);
				audio.setChannels(AudioSendHandler.INPUT_FORMAT.getChannels());
				
				EncodingAttributes attributes = new EncodingAttributes();
				attributes.setOutputFormat("wav");
				attributes.setAudioAttributes(audio);
				
				Encoder encoder = new Encoder(new FFMPEGLocator());
				encoder.encode(new MultimediaObject(input), output, attributes);
				return;
			} catch (Throwable err1) {
				err1.printStackTrace();
			}
			throw err;
		}
	}
}

