package bots.role_reaction;

import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import utils.PropertyReader;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class RoleReactionBot extends ListenerAdapter {
    public static RoleReactionBot bot;
    public static JDA botBuilt;
    
    public static void main(String[] args) {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        String token = PropertyReader.read("bots.properties","roleReaction");
        builder.setToken(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setGame(Game.watching("for -rrb:help." + ".. V0.2"));
        bot = new RoleReactionBot();
        builder.addEventListener(bot);
        try {
            botBuilt = builder.buildAsync();
        } catch (LoginException ignored) {
        }
    }
    
    private final String id = "659769290857250846";
    
    @Override
    public void onReady(ReadyEvent event) {
        if (event.getJDA().getSelfUser().getId().equals(id)) {
            for (Guild g : botBuilt.getGuilds()) {
                //System.out.println("b");
                File fi = new File("D:\\bot\\rrb\\messages\\" + g.getId());
                //System.out.println("c");
                try {
                    for (File fi2 : Objects.requireNonNull(fi.listFiles())) {
                        for (File fi3 : Objects.requireNonNull(fi2.listFiles())) {
                            try {
                                Scanner sc = new Scanner(fi3);
                                for (TextChannel txtchn : botBuilt.getGuildById(fi.getName()).getTextChannels()) {
                                    try {
                                        RestAction<Message> msg = txtchn.getMessageById(fi2.getName());
                                        msg.complete();
                                        Emote emote = botBuilt.getEmoteById(Long.parseLong(fi3.getName().substring(0, fi3.getName().length() - 4)));
                                        msg.queue();
                                        String role = sc.nextLine();
                                        msg.complete().addReaction(emote).submit();
                                        for (MessageReaction reaction : msg.complete().getReactions()) {
                                            if (reaction.getReactionEmote().getId().equals(emote.getId())) {
                                                Guild guild = reaction.getGuild();
                                                List<Member> users = guild.getMembersWithRoles(guild.getRoleById(role));
                                                ArrayList<Member> withReaction = new ArrayList<>();
                                                ArrayList<User> toRemove = new ArrayList<>();
                                                for (User user : reaction.getUsers()) {
                                                    if (!users.contains(guild.getMember(user))) {
                                                        try {
                                                            if (guild.getMember(user) != null) {
                                                                withReaction.add(guild.getMember(user));
                                                                guild.getController().addRolesToMember(guild.getMember(user), guild.getRoleById(role)).complete();
                                                            } else {
                                                                toRemove.add(user);
                                                            }
                                                        } catch (Throwable ignored) {
                                                            ignored.printStackTrace();
                                                        }
                                                    } else {
                                                        withReaction.add(guild.getMember(user));
                                                    }
                                                }
                                                for (Member user : users) {
                                                    if (!withReaction.contains(user)) {
                                                        guild.getController().removeRolesFromMember(user, guild.getRoleById(role)).complete();
                                                    }
                                                }
                                                for (User user : toRemove) {
                                                    reaction.removeReaction(user);
                                                }
                                            }
                                        }
                                    } catch (Throwable ignored) {
                                    }
                                }
                                sc.close();
                            } catch (IOException err) {
                                System.out.println("Fatal Error");
                            }
                        }
                    }
                } catch (NullPointerException err) {
                    //System.out.println("Guild has no role reactions setup.");
                }
            }
            super.onReady(event);
        }
    }
    
    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (event.getJDA().getSelfUser().getId().equals(id)) {
            if (!event.getAuthor().isBot()) {
                event.getChannel().sendMessage("Long invite url (dev doesn't know how to make it shorter without tiny url):\n<https://discordapp.com/api/oauth2/authorize?client_id=659769290857250846&permissions=8&redirect_uri=https%3A%2F%2Fdiscordapp.com%2Fapi%2Fwebhooks%2F659769495577034752%2FoPZDb7NqGjhSvPWXfLLFIGTezz0rSo7LWFrkZHcwxkjjKC2tYYOJtgUueK3TOy_nkBpQ&scope=bot>").complete();
                event.getChannel().sendMessage("Usage:`-roleReactions:addRR [messageID] [reaction Emote id or actual emote] [role id or name]`").complete();
            }
            super.onPrivateMessageReceived(event);
        }
    }
    
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getJDA().getSelfUser().getId().equals(id)) {
            if (event.getGuild().getMemberById(event.getAuthor().getId()).hasPermission(Permission.ADMINISTRATOR)) {
                File fi = new File("D:\\bot\\rrb\\messages\\" + event.getGuild().getId());
                if (event.getMessage().getContentRaw().startsWith("-rrb:addrr") && event.getChannel().getName().contains("bot")) {
                    try {
                        String arg = event.getMessage().getContentRaw().substring(("-rrb:addrr ").length());
    
                        String[] args = arg.split(" ");
                        File fi2 = new File(fi.getPath() + "\\" + args[0]);
                        File fi3 = new File(fi.getPath() + "\\" + args[0] + "\\" + args[1] + ".txt");
                        fi2.mkdirs();
                        try {
                            //Check if the message specified already has the emote specified as a reaction role
                            if (fi3.exists()) {
                                //Setup embed
                                EmbedBuilder builder = new EmbedBuilder();
                                builder.setColor(Color.ORANGE);
                                builder.addField("Invalid Reaction. ", "Reaction with emote: " + botBuilt.getEmoteById(args[1]).getAsMention() + " already exists on the specified message.", true);
    
                                //Send response message
                                MessageAction action = event.getChannel().sendMessage(" ");
                                action = action.embed(builder.build());
                                action.complete();
    
                                return;
                            }
    
    
                            TextChannel textChannel = null;
    
                            List<TextChannel> channels = event.getGuild().getTextChannels();
                            //Get text channel in which the message for the reaction is in
                            for (TextChannel chn : channels) {
                                try {
                                    RestAction<Message> msg = chn.getMessageById(args[0]);
                                    if (msg != null) {
                                        if (msg.complete().getId().equals(args[0])) {
                                            textChannel = chn;
                                            msg.complete().addReaction(botBuilt.getEmoteById(args[1])).complete();
                                            break;
                                        }
                                    }
                                } catch (Throwable ignored) {
                                }
                            }
    
                            EmbedBuilder builder = new EmbedBuilder();
    
                            //Setup embed message (throws an error if something is missing, which prevents it from creating a worthless, broken file)
                            builder.setFooter("Reaction by: " + event.getAuthor().getName(), event.getAuthor().getAvatarUrl());
                            builder.setTitle("Reaction added!");
                            builder.setColor(Color.GREEN);
    
                            builder.addField("Channel", textChannel.getAsMention(), false);
                            builder.addField("Message", textChannel.getMessageById(args[0]).complete().getContentRaw(), false);
                            builder.addField("Emote", botBuilt.getEmoteById(args[1]).getAsMention(), false);
                            builder.addField("Role", botBuilt.getRoleById(args[2]).getAsMention(), false);
    
    
                            //Create file for reaction
                            fi3.createNewFile();
                            FileWriter writer = new FileWriter(fi3);
                            writer.write(args[2]);
                            writer.close();
    
    
                            //Send response message so the bot doesn't seem like it failed
                            MessageAction action = event.getChannel().sendMessage(" ");
                            action = action.embed(builder.build());
                            action.complete();
                        } catch (Throwable err) {
    
                            //Setup embed message
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setColor(Color.RED);
                            builder.addField("An error occured,", err.getLocalizedMessage(), true);
                            try {
                                builder.addField("Please notify:", botBuilt.getUserById(380845972441530368L).getAsMention(), false);
                            } catch (Throwable ignored) {
                            }
                            err.printStackTrace();
    
                            //Send error message so the user knows the bot failed
                            MessageAction action = event.getChannel().sendMessage(" ");
                            action = action.embed(builder.build());
                            action.complete();
                        }
                    } catch (Throwable err) {
    
                        //Setup embed message
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setColor(Color.RED);
                        builder.addField("An error occured,", err.getLocalizedMessage(), true);
                        try {
                            builder.addField("Please notify:", botBuilt.getUserById(380845972441530368L).getAsMention(), false);
                        } catch (Throwable ignored) {
                        }
                        err.printStackTrace();
    
                        //Send error message so the user knows the bot failed
                        MessageAction action = event.getChannel().sendMessage(" ");
                        action = action.embed(builder.build());
                        action.complete();
                    }
                }
            }
            if (event.getMessage().getContentRaw().startsWith("-rrb:help") && event.getChannel().getName().contains("bot")) {
                event.getChannel().sendMessage("" +
                        "Commands:\n" +
                        "`-rrb:help` -> display this message\n" +
                        "`-rrb:addrr [message id] [emote id] [role id]` -> add a reaction role.\n" +
                        "**DM for invite link.**" +
                        "").complete();
            }
        }
    }
    
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getJDA().getSelfUser().getId().equals(id)) {
            if (event.getGuild().getMemberById(event.getAuthor().getId()).hasPermission(Permission.ADMINISTRATOR)) {
                if (!event.getAuthor().isBot()) {
                    String message = event.getMessage().getContentRaw();
                    if (message.startsWith("-roleReactions:addRR ")) {
                        //System.out.println("hi3");
                        String[] args = message.split(" ", 4);
                        event.getChannel().sendMessage(args[1] + "\n" + args[2]).complete();
    
                        if (args[2].startsWith("<:")) {
                            String arg2 = args[2].split(":", 3)[2];
                            System.out.println(arg2.substring(0, arg2.lastIndexOf(">")));
                            args[2] = arg2.substring(0, arg2.lastIndexOf(">"));
                        }
    
                        try {
                            Long.parseLong(args[3]);
                        } catch (NumberFormatException err) {
                            args[3] = event.getGuild().getRolesByName(args[3], true).get(0).getId();
                        }
    
                        System.out.println(args[2]);
                        File fi = new File("D:\\bot\\rrb\\messages\\" + event.getGuild().getId() + "\\" + args[1] + "\\" + args[2] + ".txt");
    
                        fi.mkdirs();
    
                        try {
                            fi.createNewFile();
                            FileWriter writer = new FileWriter(fi);
                            writer.write(args[3]);
                            writer.close();
                        } catch (IOException err) {
                            System.out.println(err.getMessage());
                        }
                    }
                }
            }
            super.onGuildMessageReceived(event);
        }
    }
    
    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (event.getJDA().getSelfUser().getId().equals(id)) {
            //System.out.println(event.getReactionEmote().toString());
            //System.out.println(event.getReaction().toString());
            //System.out.println(event.getReactionEmote().getId());
    
            File fi = new File("D:\\bot\\rrb\\messages\\" + event.getGuild().getId() + "\\" + event.getMessageId() + "\\" + event.getReactionEmote().getId() + ".txt");
    
            if (fi.exists()) {
                try {
                    Scanner sc = new Scanner(fi);
                    event.getGuild().getController().addRolesToMember(event.getMember(), event.getGuild().getRoleById(sc.nextLong())).complete();
                    sc.close();
                } catch (IOException ignored) {
                }
            }
    
            super.onGuildMessageReactionAdd(event);
        }
    }
    
    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        if (event.getJDA().getSelfUser().getId().equals(id)) {
            //System.out.println(event.getReactionEmote().toString());
            //System.out.println(event.getReaction().toString());
    
            File fi = new File("D:\\bot\\rrb\\messages\\" + event.getGuild().getId() + "\\" + event.getMessageId() + "\\" + event.getReactionEmote().getId() + ".txt");
    
            if (fi.exists()) {
                try {
                    Scanner sc = new Scanner(fi);
                    event.getGuild().getController().removeRolesFromMember(event.getMember(), event.getGuild().getRoleById(sc.nextLong())).complete();
                    sc.close();
                } catch (IOException ignored) {
                }
            }
            super.onGuildMessageReactionRemove(event);
        }
    }
}
