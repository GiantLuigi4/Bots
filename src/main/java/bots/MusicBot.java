// MADE BY PIZZA BOI

package com.github.lorenzopapi.discord;


import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MusicBot extends ListenerAdapter {
	private String prefix = "!";
	public static Map<Guild, ArrayList<?>> queue = new HashMap<>();
	private static final File downloadCache = new File("bot_cache");

	public static void main(String[] args) throws LoginException {
		downloadCache.mkdirs();
		JDABuilder.createLight("token", GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
				.enableCache(CacheFlag.VOICE_STATE)
				.addEventListeners(new MusicBot())
				.setActivity(Activity.watching("yes"))
				.build();
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
			}
		}
	}

	private static void playSong(GuildMessageReceivedEvent e, Guild guild) {
		String[] args = e.getMessage().getContentRaw().split(" ");
		VoiceChannel vc = null;
		for (VoiceChannel c : guild.getVoiceChannels()) {
			if (c.getMembers().contains(e.getMember())) {
				vc = c;
				break;
			}
		}
		if (guild.getSelfMember().getVoiceState().inVoiceChannel()) {
			doYoutubeDLRequest(args[1]);
			return;
		}
		if (vc != null) {
			if (!guild.getSelfMember().hasPermission(vc, Permission.VOICE_CONNECT) || !guild.getSelfMember().hasPermission(vc, Permission.VOICE_SPEAK)) {
				e.getChannel().sendMessage("Insufficient permissions!").queue();
			} else {
				AudioManager manager = guild.getAudioManager();
				manager.openAudioConnection(vc);
				e.getChannel().sendMessage("Connected successfully!").queue();
				AudioInputStream stream = doYoutubeDLRequest(args[1]);
			}
		} else {
			e.getChannel().sendMessage("Not voice channel!").queue();
		}

	}

	private static AudioInputStream doYoutubeDLRequest(String url) {
		try {
			YoutubeDLRequest req = new YoutubeDLRequest(url, downloadCache.getPath());
			String s = url.substring(url.indexOf("v=") + 2, url.indexOf("&") > 0 ? url.indexOf("&") : url.length());
			req.setOption("ignore-errors");
			req.setOption("extract-audio");
			req.setOption("output", "%(id)s.audio");
			req.setOption("retries", 10);
			YoutubeDLResponse response = YoutubeDL.execute(req);
			for (File file : downloadCache.listFiles()) {
				System.out.println(file.getName());
				System.out.println(s);
				if (file.getName().startsWith(s) && file.getName().matches("(\\w+\\.\\w+)")) {
					System.out.println("E");
					Path audioSrc = new File(file.getPath()).toPath();
					Path audioOut = new File(downloadCache, s + ".wav").toPath();
					FFmpeg.atPath()
							.addInput(UrlInput.fromPath(audioSrc))
							.addOutput(UrlOutput.toPath(audioOut))
							.execute();
					break;
				}
			}
			AudioInputStream stream = AudioSystem.getAudioInputStream(new File(downloadCache.getAbsolutePath(), s + ".wav"));
			System.out.println(response.getOut());
			return stream;
		} catch (YoutubeDLException | UnsupportedAudioFileException | IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}

