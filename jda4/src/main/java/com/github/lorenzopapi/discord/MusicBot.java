package com.github.lorenzopapi.discord;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.Format;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.github.lorenzopapi.discord.utils.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.*;
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
import java.util.List;
import java.util.*;

public class MusicBot extends ListenerAdapter {
	public static String prefixesFile = "musicBotPrefixes.properties";
	public static String extension = ".raw";
	public static String separator = Long.toHexString(System.currentTimeMillis());
	public static Map<Long, String> userToPrefix = new HashMap<>();
	public static Map<Guild, ArrayList<YoutubeVideoInfo>> queue = new HashMap<>();
	private static final File downloadCache = new File("bot_cache");
	private static JDA bot;
	private static final HashMap<String, YoutubeVideoInfo> streamsByServer = new HashMap<>();
	private static final Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
	
	public static void main(String[] args) throws LoginException, InterruptedException {
		Files.create("Settings.properties", "drive:C");
		if (!Files.create("bots.properties")) {
			if (PropertyReader.contains("bots.properties", "musicBot")) {
				downloadCache.mkdirs();
				for (File file : downloadCache.listFiles()) {
					file.delete();
				}
				JDABuilder builder = JDABuilder.createLight(PropertyReader.read("bots.properties", "musicBot"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
						.enableCache(CacheFlag.VOICE_STATE)
//						.enableIntents(GatewayIntent.GUILD_MEMBERS)
//						.setMemberCachePolicy(MemberCachePolicy.ALL)
						.addEventListeners(new MusicBot())
						.setActivity(Activity.watching("-music:help"));
				bot = builder.build();
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
		if (message.startsWith(prefix + "playlist")) {
			String subCommand = message.substring((prefix + "playlist ").length());
			try {
				handlePlaylist(e, m, prefix, subCommand);
			} catch (IOException ignored) {}
		} else if (message.startsWith(prefix + "effects ")) {
			handleEffect(message, prefix, handler);
		} else if (message.startsWith(prefix) || message.startsWith("-music:")) {
			if (message.startsWith(prefix + "play ")) {
				playSong(e, e.getGuild());
			} else if (message.startsWith(prefix + "playfile")) {
				playFile(e, e.getGuild());
			} else if (message.startsWith(prefix + "queue")) {
				if (!getQueue(e.getGuild()).isEmpty()) {
					e.getChannel().sendMessage(queueBuilder(e).build()).complete();
				} else {
					e.getChannel().sendMessage("Queue is empty").complete();
				}
			} else if (message.startsWith(prefix + "insert")) {
				HashMap<String, String> args = parseArgs(e.getMessage().getContentRaw());
				if (args.containsKey("pos")) {
					if (args.containsKey("video")) {
						YoutubeVideoInfo info = getQueue(e.getGuild()).get(Integer.parseInt(args.get("pos")));
						if (args.containsKey("r")) {
							if (e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
								addToQueue(args, e);
							} else {
								e.getChannel().sendMessage("You need Administrator permission to replace queue elements").complete();
							}
						} else {
							if (info == null) {
								addToQueue(args, e);
							} else {
								e.getChannel().sendMessage("There is already a song in the queue at that position.\nAdd the r:1 to the message to replace the song").complete();
							}
						}
					} else {
						e.getChannel().sendMessage("Please add video link").complete();
					}
				} else {
					e.getChannel().sendMessage("Please add queue position").complete();
				}
			} else if (message.startsWith(prefix + "remove")) {
				if (e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
					HashMap<String, String> args = parseArgs(e.getMessage().getContentRaw());
					if (args.containsKey("pos")) {
						YoutubeVideoInfo info = getQueue(e.getGuild()).get(Integer.parseInt(args.get("pos")));
						if (info == null) {
							e.getChannel().sendMessage("There is no song at the position " + args.get("pos") + " in the queue").complete();
						} else {
							getQueue(e.getGuild()).remove(Integer.parseInt(args.get("pos")));
						}
					} else {
						e.getChannel().sendMessage("Please add queue position").complete();
					}
				} else {
					e.getChannel().sendMessage("You need Administrator permission to replace queue elements").complete();
				}
			} else if (message.startsWith(prefix + "clear")) {
				if (e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
					queue.get(e.getGuild()).clear();
					e.getChannel().sendMessage("Queue cleared!").complete();
				} else {
					e.getChannel().sendMessage("You need the Administration permission to do that!").complete();
				}
			} else if (message.startsWith(prefix + "skip")) {
				if (e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
					handler.loops = 0;
					handler.counter = handler.audio.length - handler.packetSize;
					e.getChannel().sendMessage("Song Skipped!").complete();
				} else {
					e.getChannel().sendMessage("You need the Administration permission to do that!").complete();
				}
			} else if (message.startsWith(prefix + "help") || message.startsWith("-music:help")) {
				e.getChannel().sendMessage(helpMessageBuilder(e).build()).complete();
			} else if (message.startsWith(prefix + "leave")) {
				e.getGuild().getAudioManager().closeAudioConnection();
			} else if (message.startsWith(prefix + "info")) {
				e.getChannel().sendMessage(infoMessageBuilder(e).build()).complete();
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

	private static void handleEffect(String message, String prefix, SendingHandler handler) {
		ScheduledEffect effect = new ScheduledEffect();
		String effects = message.substring((prefix + "effects").length());
		if (effects.equals(" reset")) {
			handler.isForTheWorstApplied = false;
			handler.volume = 100;
			handler.byteSwap = 1;
			handler.bassBoost = 0;
			handler.effectsQueue.clear();
			handler.pseudoRetro = 1;
			handler.packetSize = 3840;
			handler.isReverseOn = false;
		} else {
			if (effects.equals(" for the worst")) {
				handler.isForTheWorstApplied = !handler.isForTheWorstApplied;
			} else if (effects.equals(" reverse")) {
				handler.isReverseOn = !handler.isReverseOn;
			}
		}	//TODO: user presets
		HashMap<String, String> args = parseArgs(effects);
		if (args.containsKey("volume")) effect.volume =  Float.parseFloat(args.get("volume"));
		else if (args.containsKey("v")) effect.volume =  Float.parseFloat(args.get("v"));
		if (args.containsKey("byteswap")) effect.byteSwap =  Integer.parseInt(args.get("byteswap"));
		if (args.containsKey("pr")) effect.pseudoRetro =  Integer.parseInt(args.get("pr"));
		else if (args.containsKey("psuedo_retro")) effect.pseudoRetro =  Integer.parseInt(args.get("psuedo_retro"));
		if (args.containsKey("bassboost")) effect.bassBoost =  Integer.parseInt(args.get("bassboost"));
		else if (args.containsKey("bb")) effect.bassBoost =  Integer.parseInt(args.get("bb"));
		else if (args.containsKey("bass_boost")) effect.bassBoost =  Integer.parseInt(args.get("bass_boost"));
		if (args.containsKey("delay")) effect.delay = Integer.parseInt(args.get("delay"));
		if (args.containsKey("chance")) effect.chance = Integer.parseInt(args.get("chance"));
		if (args.containsKey("loops")) handler.loops =  Integer.parseInt(args.get("loops"));
		else if (args.containsKey("l")) handler.loops =  Integer.parseInt(args.get("l"));
		handler.effectsQueue.add(effect);
	}
	
	private static void handlePlaylist(GuildMessageReceivedEvent e, Message m, String prefix, String subCommand) throws IOException {
		if (subCommand.startsWith("create")) {
			String message1 = m.getContentRaw();
			if (!subCommand.startsWith("create ")) {
				e.getChannel().sendMessage("Please provide a name for your playlist").reference(e.getMessage()).mentionRepliedUser(false).complete();
			} else {
				subCommand = message1.substring((prefix + "playlist ").length());
				String listName = subCommand.substring("create ".length());
				File file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + listName + "/attributes.properties");
				if (file.exists()) {
					String ownerID = PropertyReader.read(file, "owner");
					Member member = e.getGuild().getMemberById(ownerID);
					if (e.getMember().getId().equals(ownerID)) {
						e.getChannel()
								.sendMessage("Playlist " + listName + " already exists, it is owned by you.")
								.reference(e.getMessage()).mentionRepliedUser(false).complete();
						Files.write(file,
								"owner:" + e.getMember().getId() + "\n" +
										"ownerName:" + e.getMember().getUser().getName() + "\n" +
										"ownerDiscriminator:" + e.getMember().getUser().getDiscriminator()
						);
						return;
					}
					String name = "";
					for (Member member1 : e.getGuild().getMembers()) {
						if (member1.getId().equals(ownerID)) {
							name = member.getEffectiveName() + "#" + member.getUser().getDiscriminator();
						}
					}
					if (name.equals("")) {
						if (member != null) {
							name = member.getEffectiveName() + "#" + member.getUser().getDiscriminator();
						} else {
							if  (e.getJDA().getUserById(ownerID) != null) {
								name = e.getJDA().getUserById(ownerID).getName() + "#" + e.getJDA().getUserById(ownerID).getDiscriminator();
							}
						}
					}
					if (name.equals("")) {
						name = PropertyReader.read(file, "ownerName") + "#" + PropertyReader.read(file, "ownerDiscriminator");
					}
					e.getChannel()
							.sendMessage("Playlist " + listName + " already exists, it is owned by " + name + ".")
							.reference(e.getMessage()).mentionRepliedUser(false).complete();
				} else {
					file.getParentFile().mkdirs();
					Files.create(file.toString());
					Files.write(file,
							"owner:" + e.getMember().getId() + "\n" +
									"ownerName:" + e.getMember().getUser().getName() + "\n" +
									"ownerDiscriminator:" + e.getMember().getUser().getDiscriminator()
					);
					e.getChannel().sendMessage(
							"Playlist " + listName + "has been created successfully!\n" +
									"Use `-music:playlist add " + listName + " [url]` to start filling it up."
					).reference(e.getMessage()).mentionRepliedUser(false).complete();
					Files.create("bots/music/playlists/" + e.getGuild().getId() + "/" + listName + "/playlist.json", "{}");
				}
			}
		} else if (subCommand.startsWith("play")) {
			String message1 = m.getContentRaw();
			if (!subCommand.startsWith("play ")) {
				e.getChannel().sendMessage("Please provide a name for your playlist").reference(e.getMessage()).mentionRepliedUser(false).complete();
			} else {
				subCommand = message1.substring((prefix + "playlist ").length());
				String listName = subCommand.substring("play ".length());
				File file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + listName + "/playlist.json");
				JsonObject listJson = gson.fromJson(Files.read(file), JsonObject.class);
				Playlist playlist = Playlist.deserialize(listJson);
				if (e.getGuild().getAudioManager().getConnectedChannel() == null) {
					VoiceChannel vc;
					for (VoiceChannel c : e.getGuild().getVoiceChannels()) {
						if (c.getMembers().contains(e.getMember())) {
							vc = c;
							e.getGuild().getAudioManager().openAudioConnection(vc);
							break;
						}
					}
				}
				for (YoutubeVideoInfo video : playlist.getVideos()) {
					getQueue(e.getGuild()).add(video);
				}
				if (!e.getGuild().getAudioManager().isConnected()) {
					e.getGuild().getAudioManager().setSendingHandler(new SendingHandler(getQueue(e.getGuild()), e.getGuild().getAudioManager()));
				}
			}
		} else if (subCommand.startsWith("add")) {
			String message1 = m.getContentRaw();
			if (!subCommand.startsWith("add ")) {
				e.getChannel().sendMessage("Please provide the name of the playlist you want to add a video to, as well as the link to the video").reference(e.getMessage()).mentionRepliedUser(false).complete();
			} else {
				subCommand = message1.substring((prefix + "playlist ").length());
				String[] args = subCommand.substring("add ".length()).split(" ");
				String listName = args[0];
				File file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + listName + "/attributes.properties");
				String ownerID = PropertyReader.read(file, "owner");
				if (!e.getMember().getId().equals(ownerID)) {
					e.getChannel().sendMessage(
							"You cannot add a video to a playlist you do not own"
					).reference(e.getMessage()).mentionRepliedUser(false).complete();
					return;
				}
				file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + listName + "/playlist.json");
				Files.create("bots/music/playlists/" + e.getGuild().getId() + "/" + listName + "/playlist.json", "{}");
				JsonObject listJson = gson.fromJson(Files.read(file), JsonObject.class);
				Playlist playlist = Playlist.deserialize(listJson);
				String info = "";
				if (args.length > 2) {
					for (int index = 2; index < args.length; index++) {
						info += args[index] + " ";
					}
				}
				try {
					System.out.println(args[1]);
					YoutubeVideoInfo info1 = doYoutubeDLRequest(args[1]);
					HashMap<String, String> args1 = parseArgs(info);
					setupSpecial(info1, args1);
					if (args1.containsKey("index")) playlist.addVideo(Integer.parseInt(args1.get("index") + 1), info1);
					else playlist.addVideo(info1);
					JsonObject list = playlist.serialize();
					Files.write(file, gson.toJson(list));
					e.getChannel().sendMessage(
							"Successfully added `" + info1.name + "` to `" + listName + "`."
					).reference(e.getMessage()).mentionRepliedUser(false).complete();
				} catch (Throwable err) {
					err.printStackTrace();
				}
			}
		} else if (subCommand.startsWith("remove")) {
			String message1 = m.getContentRaw();
			if (!subCommand.startsWith("remove ")) {
				e.getChannel().sendMessage("Please provide the name of the playlist you want to remove a video from, as well as the index of the video").reference(e.getMessage()).mentionRepliedUser(false).complete();
			} else {
				subCommand = message1.substring((prefix + "playlist ").length());
				String[] args = subCommand.substring("remove ".length()).split(" ");
				String listName = args[0];
				File file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + listName + "/attributes.properties");
				String ownerID = PropertyReader.read(file, "owner");
				if (!e.getMember().getId().equals(ownerID)) {
					e.getChannel().sendMessage(
							"You cannot remove a video from a playlist you do not own"
					).reference(e.getMessage()).mentionRepliedUser(false).complete();
					return;
				}
				file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + listName + "/playlist.json");
				JsonObject listJson = gson.fromJson(Files.read(file), JsonObject.class);
				Playlist playlist = Playlist.deserialize(listJson);
				YoutubeVideoInfo info1 = playlist.removeVideo(Integer.parseInt(args[1]) + 1);
				JsonObject list = playlist.serialize();
				Files.write(file, gson.toJson(list));
				e.getChannel().sendMessage(
						"Successfully removed `" + info1.name + "` from `" + listName + "`."
				).reference(e.getMessage()).mentionRepliedUser(false).complete();
			}
		} else if (subCommand.startsWith("delete")) {
			String message1 = m.getContentRaw();
			if (!subCommand.startsWith("delete ")) {
				e.getChannel().sendMessage("Please provide the name of the playlist you want to delete").reference(e.getMessage()).mentionRepliedUser(false).complete();
			} else {
				subCommand = message1.substring((prefix + "playlist ").length());
				String[] args = subCommand.substring("remove ".length()).split(" ");
				String listName = args[0];
				File file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + listName + "/attributes.properties");
				String ownerID = PropertyReader.read(file, "owner");
				if (!e.getMember().getId().equals(ownerID)) {
					e.getChannel().sendMessage(
							"You cannot add a video to a playlist you do not own"
					).reference(e.getMessage()).mentionRepliedUser(false).complete();
					return;
				}
				file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + listName);
				for (File listFile : file.listFiles()) {
					listFile.delete();
				}
				file.delete();
				e.getChannel().sendMessage(
						"Successfully deleted `" + listName + "`.\nAny servers which hold a copy of this playlist will no longer be able to play it."
				).reference(e.getMessage()).mentionRepliedUser(false).complete();
			}
		} else if (subCommand.startsWith("copy")) {
			String message1 = m.getContentRaw();
			if (!subCommand.startsWith("copy ")) {
				e.getChannel().sendMessage("Please provide the name of the playlist you want to copy, and which server you want to copy it from").reference(e.getMessage()).mentionRepliedUser(false).complete();
			} else {
				subCommand = message1.substring((prefix + "playlist ").length());
				String[] args = subCommand.substring("copy ".length()).split(" ");
				String guildName = "";
				for (int index = 1; index < args.length; index++) {
					guildName += args[index] + " ";
				}
				Guild srcGuild = null;
				for (Guild guild : bot.getGuilds()) {
					if (guild.getName().startsWith(guildName)) {
						srcGuild = guild;
						break;
					}
				}
				if (srcGuild != null) {
					String pointer = srcGuild.getId() + "/" + args[0];
					File file = new File("bots/music/playlists/" + pointer);
					File file1 = new File("bots/music/playlists/" + pointer + "/attributes.properties");
					if (!file.exists()) {
						e.getChannel().sendMessage(
								"Server `" + srcGuild.getName() + "` does not have a playlist called `" + args[0] + "`."
						).reference(e.getMessage()).mentionRepliedUser(false).complete();
					}
					Playlist playlist = new Playlist(new ArrayList<>());
					playlist.pointer = pointer;
					file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + args[0]);
					if (file.exists()) {
						e.getChannel().sendMessage(
								"The playlist `" + args[0] + "` already exists in this server."
						).reference(e.getMessage()).mentionRepliedUser(false).complete();
					}
					file.mkdirs();
					file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + args[0] + "/attributes.properties");
					Files.write(file, Files.read(file1));
					file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + args[0] + "/playlist.json");
					JsonObject list = playlist.serialize();
					Files.write(file, gson.toJson(list));
					e.getChannel().sendMessage(
							"Successfully copied `" + args[0] + "` from `" + srcGuild.getName() + "`"
					).reference(e.getMessage()).mentionRepliedUser(false).complete();
				} else {
					e.getChannel().sendMessage(
							"Server `" + guildName + "` is unknown to the me, and no servers known to me start with that name"
					).reference(e.getMessage()).mentionRepliedUser(false).complete();
				}
//					subCommand = message1.substring((prefix + "playlist ").length());
//					String[] args = subCommand.substring("remove ".length()).split(" ");
//					String listName = args[0];
//					File file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + listName + "/attributes.properties");
//					String ownerID = PropertyReader.read(file, "owner");
//					Member member = e.getGuild().getMemberById(ownerID);
//					if (!e.getMember().getId().equals(ownerID)) {
//						e.getChannel().sendMessage(
//								"You cannot add a video to a playlist you do not own"
//						).reference(e.getMessage()).mentionRepliedUser(false).complete();
//						return;
//					}
//					file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + listName);
//					for (File listFile : file.listFiles()) {
//						listFile.delete();
//					}
//					file.delete();
//					e.getChannel().sendMessage(
//							"Successfully deleted `" + listName + "`.\nAny servers which hold a copy of this playlist will no longer be able to play it."
//					).reference(e.getMessage()).mentionRepliedUser(false).complete();
			}
		} else if (subCommand.startsWith("songs")) {
			String message1 = m.getContentRaw();
			if (!subCommand.startsWith("songs ")) {
				e.getChannel().sendMessage("Specify a playlist to list").complete();
			} else {
				subCommand = message1.substring((prefix + "playlist ").length());
				String[] args = subCommand.substring("songs ".length()).split(" ");
				File file = new File("bots/music/playlists/" + e.getGuild().getId() + "/" + args[0] + "/playlist.json");
				if (!file.exists()) {
					e.getChannel().sendMessage("Playlist is empty! Fill it up with -music:add").complete();
				} else {
					JsonObject listJson = gson.fromJson(Files.read(file), JsonObject.class);
					Playlist playlist = Playlist.deserialize(listJson);
					e.getChannel().sendMessage(playlistSongsBuilder(args, playlist.getVideos(), e).build()).complete();
				}
			}
		} else if (subCommand.startsWith("list")) {
			String message1 = m.getContentRaw();
			subCommand = message1.substring((prefix + "playlist ").length());
			String[] args = subCommand.substring("list".length()).split(" ");
			e.getChannel().sendMessage(playlistListBuilder(args[0], e).build()).complete();
		} else {
			e.getChannel().sendMessage("`" + subCommand + "`" + " is not a valid subcommand for playlist").reference(e.getMessage()).mentionRepliedUser(false).complete();
		}
	}

	private static void addToQueue(HashMap<String, String> args, GuildMessageReceivedEvent e) {
		try {
			String video = args.get("video");
			if (video.startsWith("<") && video.endsWith(">")) {
				video = video.substring(1, video.length() - 1);
			}
			YoutubeVideoInfo info = doYoutubeDLRequest(video);
			setupSpecial(info, args);
			getQueue(e.getGuild()).set(Integer.parseInt(args.get("pos")) - 1, info);
			e.getChannel().sendMessage("Successfully modified the queue!").complete();
		} catch (IOException | YoutubeException ignored) {}
	}

	private static void playFile(GuildMessageReceivedEvent e, Guild guild) {
		AudioManager manager = guild.getAudioManager();
		if (e.getMessage().getAttachments().isEmpty()) {
			e.getChannel().sendMessage("Please provide an attachment").reference(e.getMessage()).mentionRepliedUser(false).complete();
			return;
		} else if (e.getMessage().getAttachments().size() > 1) {
			e.getChannel().sendMessage("Warn: a list of attachment has been given, but only the first will be used").reference(e.getMessage()).mentionRepliedUser(false).complete();
		}
		HashMap<String, String> args1 = parseArgs(e.getMessage().getContentRaw());
		VoiceChannel vc = null;
		for (VoiceChannel c : guild.getVoiceChannels()) {
			if (c.getMembers().contains(e.getMember())) {
				vc = c;
				break;
			}
		}
		Message.Attachment attachment = e.getMessage().getAttachments().get(0);
		if (guild.getSelfMember().getVoiceState().inVoiceChannel()) {
			try {
				File file = attachment.downloadToFile().get();
				FileInputStream fis = new FileInputStream(file);
				byte[] audio = new byte[fis.available()];
				fis.read(audio);
				YoutubeVideoInfo info = new YoutubeVideoInfo(attachment.getFileName(), 1, audio, "");
				if (streamsByServer.containsKey(e.getGuild().getId()))
					streamsByServer.replace(e.getGuild().getId(), info);
				else
					streamsByServer.put(e.getGuild().getId(), info);
				if (info.viewCount == -1)
					e.getChannel().sendMessage(info.name).reference(e.getMessage()).mentionRepliedUser(false).complete();
				else
					e.getChannel().sendMessage(playingFileMessageBuilder(info, e, true).build()).reference(e.getMessage()).mentionRepliedUser(false).complete();
				ArrayList<YoutubeVideoInfo> infos = getQueue(guild);
				setupSpecial(info, args1);
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
					File file = attachment.downloadToFile().get();
					FileInputStream fis = new FileInputStream(file);
					byte[] audio = new byte[fis.available()];
					fis.read(audio);
					YoutubeVideoInfo info = new YoutubeVideoInfo(attachment.getFileName(), 1, audio, "");
					streamsByServer.put(e.getGuild().getId(), info);
					if (info.viewCount == -1)
						e.getChannel().sendMessage(info.name).reference(e.getMessage()).mentionRepliedUser(false).complete();
					else
						e.getChannel().sendMessage(playingFileMessageBuilder(info, e, false).build()).reference(e.getMessage()).mentionRepliedUser(false).complete();
					ArrayList<YoutubeVideoInfo> infos = getQueue(guild);
					setupSpecial(info, args1);
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

	private static void playSong(GuildMessageReceivedEvent e, Guild guild) {
		String[] args = e.getMessage().getContentRaw().split(" ");
		AudioManager manager = guild.getAudioManager();
		if (args.length <= 1) {
			e.getChannel().sendMessage("Please provide a link to a valid youtube video").reference(e.getMessage()).mentionRepliedUser(false).complete();
			return;
		}
		HashMap<String, String> args1 = parseArgs(e.getMessage().getContentRaw());
		String video;
		if (args1.containsKey("video")) {
			video = args1.get("video");
		} else {
			video = args[1];
		}
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
				setupSpecial(info, args1);
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
					setupSpecial(info, args1);
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
	
	private static void setupSpecial(YoutubeVideoInfo info, HashMap<String, String> args1) {
		if (args1.containsKey("bb")) info.bassBoost = Byte.parseByte(args1.get("bb"));
		if (args1.containsKey("bassboost")) info.bassBoost = Byte.parseByte(args1.get("bassboost"));
		if (args1.containsKey("speed")) info.speed = Float.parseFloat(args1.get("speed"));
		if (args1.containsKey("s")) info.speed = Float.parseFloat(args1.get("s"));
		if (args1.containsKey("loop")) info.loopCount = Integer.parseInt(args1.get("loop"));
		if (args1.containsKey("l")) info.loopCount = Integer.parseInt(args1.get("l"));
		if (args1.containsKey("start")) info.startTimestamp = args1.get("start");
		if (args1.containsKey("end")) info.endTimestamp = args1.get("end");
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

	private static EmbedBuilder playingFileMessageBuilder(YoutubeVideoInfo info, GuildMessageReceivedEvent e, boolean isQueueing) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(new Color(((int) Math.abs(info.name.length() * 3732.12382f)) % 255, Math.abs(Objects.hash(info.name)) % 255, Math.abs(Objects.hash(info.name.toLowerCase())) % 255));
		if (isQueueing) builder.setTitle("Added to queue: ");
		else builder.setTitle("Now Playing:");
		String url = info.link;
		builder.setDescription(info.name);
		builder.addField("Link: ", url, false);
		builder.addField("Video has: ", info.viewCount + " views", false);
		builder.setAuthor("Requested by: " + e.getMember().getEffectiveName(), null, e.getAuthor().getAvatarUrl());
		builder.setFooter("Bot by: GiantLuigi4 and LorenzoPapi");
		return builder;
	}

	private static EmbedBuilder infoMessageBuilder(GuildMessageReceivedEvent e) {
		SendingHandler handler = (SendingHandler) e.getGuild().getAudioManager().getSendingHandler();
		YoutubeVideoInfo info = handler.info;
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(new Color(((int) Math.abs(info.name.length() * 3732.12382f)) % 255, Math.abs(Objects.hash(info.name)) % 255, Math.abs(Objects.hash(info.name.toLowerCase())) % 255));
		builder.setTitle("Currently playing: ");
		String url = info.link;
		builder.setDescription(info.name);
		builder.addField("Link: ", url, false);
		builder.addField("Video has:", info.viewCount + " views", false);
		builder.addField("Timestamp:", handler.getTimestamp() + " / " + handler.getEndTimestamp(), true);
		builder.addField("Selected Range:", handler.getStartTimestamp() + " - " + handler.getEndTimestamp(), true);
		builder.addField("**Remaining Loops:" + (handler.loops) + "**", "**Speed:" + info.speed + "**", false);
		String videoId = url.substring(url.indexOf("v=") + 2, url.indexOf("&") > 0 ? url.indexOf("&") : url.length());
		builder.setThumbnail("https://i.ytimg.com/vi/%id%/hqdefault.jpg".replace("%id%", videoId));
		builder.setAuthor("Requested by: " + e.getMember().getEffectiveName(), null, e.getAuthor().getAvatarUrl());
		builder.setFooter("Bot by: GiantLuigi4 and LorenzoPapi");
		return builder;
	}

	private static EmbedBuilder playlistListBuilder(String page, GuildMessageReceivedEvent e) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(new Color(((int) Math.abs(e.hashCode() * 3732.12382f)) % 255, Math.abs(Objects.hash("queue")) % 255, Math.abs(Objects.hash("QUEUE")) % 255));
		File folder = new File("bots/music/playlists/" + e.getGuild().getId() + "/");
		int playlistCounter = folder.listFiles().length;
		if (playlistCounter > 10) {
			if (!page.isEmpty()) {
				builder.setTitle("Playlists available, page " + page);
				int p = Integer.parseInt(page);
				for (int i = 10 * (p - 1); i < Math.min((10 * p), playlistCounter); i++) {
					JsonObject listJson = gson.fromJson(Files.read(folder.listFiles()[i] + "/playlist.json"), JsonObject.class);
					Playlist list = Playlist.deserialize(listJson);
					String owner = PropertyReader.read(folder.listFiles()[i] + "/attributes.properties", "owner");
					builder.addField((i + 1) + ". " + folder.listFiles()[i], "By: " + e.getGuild().getMember(User.fromId(owner)).getEffectiveName() + "\n" + "Songs: " + list.getVideos().size(), false);
				}
			} else {
				builder.setTitle("Playlists available, page 1");
				for (int i = 0; i < 10; i++) {
					JsonObject listJson = gson.fromJson(Files.read(folder.listFiles()[i] + "/playlist.json"), JsonObject.class);
					Playlist list = Playlist.deserialize(listJson);
					String owner = PropertyReader.read(folder.listFiles()[i] + "/attributes.properties", "owner");
					builder.addField((i + 1) + ". " + folder.listFiles()[i], "By: " + e.getGuild().getMember(User.fromId(owner)).getEffectiveName() + "\n" + "Songs: " + list.getVideos().size(), false);
				}
			}
		} else {
			builder.setTitle("Playlists available");
			File[] files = folder.listFiles();
			for (int i = 0; i < playlistCounter; i++) {
				JsonObject listJson = gson.fromJson(Files.read(files[i] + "/playlist.json"), JsonObject.class);
				Playlist list = Playlist.deserialize(listJson);
				System.out.println(files[i] + "/attributes.properties");
				//owner is null because JDA 4 is dumb without intents
				String owner = PropertyReader.read(files[i] + "/attributes.properties", "owner");
				Member member = e.getGuild().getMember(User.fromId(owner));
				if (member != null) owner = member.getNickname();
				else owner = PropertyReader.read(files[i] + "/attributes.properties", "ownerName");
				String name = folder.listFiles()[i].toString();
				builder.addField((i + 1) + ". " + name.substring(name.lastIndexOf("\\") + 1), "By: " + owner + "\n" + "Songs: " + list.getVideos().size(), false);
			}
		}
		builder.setFooter("Bot by: GiantLuigi4 and LorenzoPapi");
		return builder;
	}

	private static EmbedBuilder playlistSongsBuilder(String[] args, List<YoutubeVideoInfo> infos, GuildMessageReceivedEvent e) {
		String playlist = args[0];
		String page = "";
		if (args.length > 1) {
			page = args[1];
		}
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(new Color(((int) Math.abs(e.hashCode() * 3732.12382f)) % 255, Math.abs(Objects.hash("queue")) % 255, Math.abs(Objects.hash("QUEUE")) % 255));
		if (infos.size() > 10) {
			if (!page.isEmpty()) {
				builder.setTitle("Songs of playlist " + playlist + ", page " + page);
				int p = Integer.parseInt(page);
				for (int i = 10 * (p - 1); i < Math.min((10 * p), infos.size()); i++) {
					YoutubeVideoInfo info = infos.get(i);
					builder.addField((i + 1) + ". " + info.name, "Looping: " + info.loopCount + " times\n" + "Speed: " + info.speed, false);
				}
			} else {
				builder.setTitle("Songs of playlist " + playlist + ", page 1");
				for (int i = 0; i < 10; i++) {
					YoutubeVideoInfo info = infos.get(i);
					builder.addField((i + 1) + ". " + info.name, "Looping: " + info.loopCount + " times\n" + "Speed: " + info.speed, false);
				}
			}
		} else {
			builder.setTitle("Songs of playlist " + playlist);
			for (int i = 0; i < infos.size(); i++) {
				YoutubeVideoInfo info = infos.get(i);
				builder.addField((i + 1) + ". " + info.name, "Looping: " + info.loopCount + " times\n" + "Speed: " + info.speed, false);
			}
		}
		builder.setFooter("Bot by: GiantLuigi4 and LorenzoPapi");
		return builder;
	}

	private static EmbedBuilder queueBuilder(GuildMessageReceivedEvent e) {
		HashMap<String, String> map =  parseArgs(e.getMessage().getContentRaw().toLowerCase());
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(new Color(((int) Math.abs(e.hashCode() * 3732.12382f)) % 255, Math.abs(Objects.hash("queue")) % 255, Math.abs(Objects.hash("QUEUE")) % 255));
		if (getQueue(e.getGuild()).size() > 10) {
			if (!map.isEmpty() && map.containsKey("p")) {
				int page = Integer.parseInt(map.get("p"));
				builder.setTitle("Current queue, page " + page);
				for (int i = 10 * (page - 1); i < Math.min((10 * page), getQueue(e.getGuild()).size()); i++) {
					YoutubeVideoInfo info = getQueue(e.getGuild()).get(i);
					builder.addField((i + 1) + ". " + info.name, "Looping: " + info.loopCount + " times\n" + "Speed: " + info.speed, false);
				}
			} else {
				builder.setTitle("Current queue, page 1");
				for (int i = 0; i < 10; i++) {
					YoutubeVideoInfo info = getQueue(e.getGuild()).get(i);
					builder.addField((i + 1) + ". " + info.name, "Looping: " + info.loopCount + " times\n" + "Speed: " + info.speed, false);
				}
			}
		} else {
			builder.setTitle("Current queue");
			for (int i = 0; i < getQueue(e.getGuild()).size(); i++) {
				YoutubeVideoInfo info = getQueue(e.getGuild()).get(i);
				builder.addField((i + 1) + ". " + info.name, "Looping: " + info.loopCount + " times\n" + "Speed: " + info.speed, false);
			}
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
			if (!string.isEmpty() && string.contains(":") && !string.startsWith("https")) {
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
		builder.addField(prefix + "queue", "Displays the queue, 10 songs per page", false);
		builder.addField(prefix + "insert pos:pos video:link", "Inserts specified song at position of queue.\nAdd r:1 to replace", false);
		builder.addField(prefix + "remove pos:pos", "Removes song at specified position", false);
		builder.addField(prefix + "clear", "Clears queue", false);
		builder.addField(prefix + "playlist", "NYI", false);
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
	
	public static YoutubeVideoInfo doYoutubeDLRequest(String url) throws IOException, YoutubeException {
		if (url.startsWith("https://youtu.be/")) {
			url = url.substring("https://youtu.be/".length());
			url = "https://www.youtube.com/watch?v=" + url;
		}
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
					//audioOut.delete();
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

