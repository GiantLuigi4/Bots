package bots.idle_maker;

import bots.BunchOBots;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import utils.PropertyReader;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class IdleMkr extends ListenerAdapter {
    public static ArrayList<Structure> structures = new ArrayList<>();
    
    public static IdleMkr bot;
    public static JDA botBuilt;
    
    private static String id = "656255286834888707";
    
    public static Emote emote = null;
    public static String currencyname = "cogs";
    
    public static void main(String[] args) {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        String token = PropertyReader.read("bots.properties", "idleMaker");
        builder.setToken(token);
        builder.setStatus(OnlineStatus.IDLE);
        builder.setGame(Game.watching("for -idleMaker:help, version:" + "V0.9"));
        IdleMkr bot = new IdleMkr();
        builder.addEventListener(bot);
        try {
            botBuilt = builder.buildAsync();
            Thread.sleep(1000);
            id = botBuilt.getSelfUser().getId();
        } catch (Throwable err) {
        }
        //bots.idle_maker.handlestructs.getamt("380845972441530368",2L);
        structures = HandleStructs.getStructs();
        emote = botBuilt.getEmoteById(Long.parseLong("657410151178960916"));
        currencyname = emote.getAsMention() + "cogs";
        HandleStructs.getCPS("380845972441530368");
    }
    
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getJDA().getSelfUser().getId().equals(id)) {
            String msgId = event.getMessageId();
            Message message = event.getChannel().getMessageById(msgId).complete();
            String messageText = message.getContentDisplay();
    
            String prefix = null;
    
            try {
                String userid = message.getAuthor().getId();
                File fi = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\" + "personalprefix.txt");
                Scanner sc = new Scanner(fi);
                prefix = sc.nextLine();
                sc.close();
            } catch (IOException err) {
                prefix = "-idle:";
            }
    
            Boolean hasSave = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + message.getAuthor().getId()).exists();
            Boolean oldpurchases = !(new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + message.getAuthor().getId() + "\\purchases").exists());
            //System.out.println(hasSave);
            //System.out.println(oldpurchases);
            if (hasSave) {
                String userid = message.getAuthor().getId();
                Scanner sc = null;
    
                if (oldpurchases) {
                    try {
                        sc = new Scanner(new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\purchases.txt"));
                    } catch (IOException err) {
                        //System.out.println("err");
                    }
                }
    
                for (int i = 0; i < structures.size(); i++) {
                    String file = BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\purchases\\" + structures.get(i).getID() + ".txt";
                    File fistruct = new File(file);
                    fistruct.getParentFile().mkdirs();
                    if (sc != null) {
                        if (!fistruct.exists()) {
                            try {
                                fistruct.createNewFile();
                                FileWriter fiwrite = new FileWriter(fistruct);
                                if (oldpurchases) {
                                    fiwrite.write(sc.nextLine());
                                } else {
                                    fiwrite.write("0");
                                }
                                fiwrite.close();
                            } catch (IOException err) {
                                //System.out.println("err");
                            }
                        }
                    }
                }
    
                try {
                    sc.close();
                } catch (NullPointerException err) {
                }
            }
    
            if (event.getChannel().getName().contains("bot")) {
                if (!event.getAuthor().isBot()) {
                    if (messageText.startsWith("-idleMaker:help")) {
                        String name = message.getAuthor().getName();
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setAuthor(message.getAuthor().getName() + " requested:");
                        embedBuilder.setTitle(name + "'s help");
                        embedBuilder.setColor(new Color(8, 124, 250));
                        embedBuilder.addField("**" + "-idle:" + "suggest -**[type]** -**[name]** -**[(integer,humanly understandable)  cps/function]** -**[description]** -**[(integer,humanly understandable, optional) base cost]", "Suggest something to be added to idle maker. Current types, structure", true);
                        embedBuilder.addField("**" + prefix + "start**", "Start a profile, a lot of commands require you to have a profile in order to be used.", false);
                        embedBuilder.addField("**" + prefix + "buy** [id/name]", "buy a structure.", false);
                        embedBuilder.addField("**" + prefix + "buy:**[amount] [id/name]", "buy a selected amount of a structure.", false);
                        embedBuilder.addField("**" + prefix + "list**", "list  all existing structures.", false);
                        embedBuilder.addField("**" + prefix + "search** [name]", "search a structure and find out more about it.", false);
                        embedBuilder.addField("**" + prefix + "prefix** [prefix]", "change your personal prefix, so you don't have to always type " + prefix.length() + " characters before your commands.", false);

                    /*Message msg = event.getChannel().sendMessage("" +
                            "`"+prefix+"suggest -`[type]` -`[name]` -`[(integer,humanly understandable)  cps/function]` -`[description]` -`[(integer,humanly understandable, optional) base cost]" +
                            "\n ex: `"+prefix+"suggest -structure -search browser -50 -www`\n" +
                            "`"+prefix+"start` start playing idle maker (creates profile) **(will also wipe out your previous profile if you had one)**\n" +
                            "|-`"+prefix+"buy [id]`\n" +
                            "|-`"+prefix+"buy [name]`\n" +
                            "|-`"+prefix+"buy:[count] [id]`\n" +
                            "|-`"+prefix+"buy:[count] [name]`\n" +
                            "Buy a structure.\n" +
                            "`"+prefix+"list` list all structures in order of base price.\n" +
                            "`"+prefix+"search `[name] search for a specific structure to get it's stats.\n" +
                            "`"+prefix+"prefix `[prefix] Change your personal prefix, this works across servers, and only affects you."
                    ).complete();*/
                        //event.getChannel().sendMessage(msg);
                        event.getChannel().sendMessage(" ").embed(embedBuilder.build()).complete();
                    }
                    if (messageText.startsWith(prefix + "accept") && message.getAuthor().getId().equals("380845972441530368")) {
                        String arg = messageText.substring((prefix + "accept").length() + 1);
                    }
                    if (messageText.startsWith(prefix + "listSuggestions") && message.getAuthor().getId().equals("380845972441530368")) {
                        String arg = messageText.substring((prefix + "accept").length() + 1);
                    }
                    if (messageText.startsWith(prefix + "prefix")) {
                        String userid = message.getAuthor().getId();
                        File fi = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\" + "personalprefix.txt");
    
                        String arg = messageText.substring((prefix + "prefix ").length());
                        //System.out.println(arg);
    
                        event.getChannel().sendMessage("" +
                                "Prefix successfully set from:`" + prefix + "`, to :`" + arg + "`.\n" +
                                "If you get confused, run -idleMaker:help" +
                                "").complete();
    
                        try {
                            FileWriter righter = new FileWriter(fi);
                            righter.write(arg);
                            righter.close();
                        } catch (IOException err) {
                        }
                    }
                    if (messageText.startsWith(prefix + "search")) {
                        String name = messageText.substring((prefix + "search").length() + 1);
    
                        Structure struct = null;
                        int num = -1;
    
    
                        try {
                            struct = structures.get(HandleStructs.findStruct(name));
                            num = HandleStructs.findStruct(name);

                        /*event.getChannel().sendMessage("" +
                                "name:`"+struct.name+"`\n" +
                                "desc:`"+struct.description+"`\n"+
                                "_cps:`"+struct.cps+"`\n"+
                                "__id:`"+struct.getID()+"`\n"+
                                "_num:`"+num+"`"
                        ).complete();*/
                            String username = message.getAuthor().getName();
                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setAuthor(username + " requested:");
                            embedBuilder.setTitle(struct.name + "'s info");
                            embedBuilder.setColor(new Color(8, 124, 250));
                            //embedBuilder.addField("Name:",struct.name,true);
                            embedBuilder.addField("Description:", struct.description, false);
                            embedBuilder.addField(currencyname + " per second:", "" + struct.cps, false);
                            embedBuilder.addField(currencyname + " to purchase for the first time:", "" + struct.cost, false);
                            embedBuilder.addField("ID (for future reference):", "" + struct.getID(), false);
                            embedBuilder.addField("ID (to buy):", "" + num, false);
        
                            event.getChannel().sendMessage(" ").embed(embedBuilder.build()).complete();

                        /*System.out.println("name:"+struct.name);
                        System.out.println("desc:"+struct.description);
                        System.out.println("cost:"+struct.cost);
                        System.out.println("_cps:"+struct.cps);
                        System.out.println("__id:"+struct.getID());
                        System.out.println("_num:"+num);*/
                        } catch (NullPointerException err) {
                            System.out.println(err.getMessage());
                            event.getChannel().sendMessage(err.getMessage()).complete();
                        }
                    }
    
                    if (messageText.startsWith(prefix + "buy")) {
                        String userid = message.getAuthor().getId();
                        String mention = message.getAuthor().getAsMention();
                        String name = message.getAuthor().getName();
        
                        String messageTextCmd = "";
                        Long count = 0L;
        
        
                        if (messageText.startsWith(prefix + "buy:")) {
                            String text = messageText.substring((prefix).length());
                            String[] msg = text.split(":", 2);
                            messageTextCmd += msg[0];
                            count = (Long.parseLong(msg[1].split(" ", 2)[0]) - 1);
                            messageTextCmd += msg[1].split(" ", 2)[1];
                            //System.out.println(messageText);
                            //System.out.println(msg[1].split(" ",2)[1]);
                            //System.out.println("count:"+count);
                        } else {
                            messageTextCmd = messageText;
                        }
        
        
                        String arg;
                        if (messageTextCmd.startsWith(prefix)) {
                            arg = messageTextCmd.substring((prefix + "buy").length() + 1);
                        } else {
                            arg = messageTextCmd.substring(("buy").length());
                        }
        
                        //System.out.println(arg);
        
                        boolean isnumber = true;
                        try {
                            new BigInteger(arg);
                            isnumber = true;
                        } catch (NumberFormatException err) {
                            isnumber = false;
                            //System.out.println("err");
                        }
        
                        File fi = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\");
                        File fi2 = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\" + "lastupdated.txt");
        
                        Structure struct = null;
                        int num = -1;
                        try {
                            if (isnumber) {
                                struct = structures.get(Integer.parseInt(arg));
                                num = Integer.parseInt(arg);
    
                                //System.out.println(struct.name);
    
                                //event.getChannel().sendMessage("name:`"+struct.name+"`").complete();
    
                                //System.out.println("name:"+struct.name);
    
                                event.getChannel().sendMessage(" ").embed(HandleStructs.buyStruct(userid, mention, struct, fi, fi2, count, name).build()).complete();
    
                            } else {
                                try {
                                    struct = structures.get(HandleStructs.findStruct(arg.toLowerCase()));
                                    num = HandleStructs.findStruct(arg.toLowerCase());
    
                                    //System.out.println(arg.toLowerCase());
    
                                    //event.getChannel().sendMessage("name:`"+struct.name+"`").complete();
    
                                    //System.out.println("name:"+struct.name);
    
                                    Structure finalStruct = struct;
                                    Long finalCount = count;
                                    Thread buyThread = new Thread(() -> event.getChannel().sendMessage(" ").embed(HandleStructs.buyStruct(userid, mention, finalStruct, fi, fi2, finalCount, name).build()).complete());
                                    buyThread.start();
                                } catch (NullPointerException err) {
                                    //System.out.println(err.getMessage());
                                    EmbedBuilder embedBuilder = new EmbedBuilder();
                                    embedBuilder.setAuthor("");
                                    embedBuilder.setTitle("Error:");
                                    embedBuilder.setColor(new Color(255, 10, 10));
                                    embedBuilder.addField("And error occurred:", "`" + err.getMessage() + "`", true);
                                    embedBuilder.addField("Please report this to:", "GiantLuigi4#6616", false);
                                    event.getChannel().sendMessage(" ").embed(embedBuilder.build()).complete();
                                    //event.getChannel().sendMessage(err.getMessage()).complete();
                                }
                            }
                        } catch (IndexOutOfBoundsException err) {
                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setAuthor("");
                            embedBuilder.setTitle("Error:");
                            embedBuilder.setColor(new Color(255, 63, 10));
                            embedBuilder.addField("And error occurred:", "`" + err.getMessage() + "`", true);
                            embedBuilder.addField("**Please make sure you used valid arguments,**:", "and try again.", false);
                            embedBuilder.addField("If it continues to fail,:", "report this to GiantLuigi4#6616", false);
                            event.getChannel().sendMessage(" ").embed(embedBuilder.build()).complete();
                        }
        
                    }
                    if (messageText.startsWith(prefix + "list")) {
                        String msg = "";
    
                        String name = message.getAuthor().getName();
    
                        String userid = message.getAuthor().getId();
    
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setAuthor(message.getAuthor().getName() + " requested:");
                        embedBuilder.setTitle(name + "'s structures");
                        embedBuilder.setColor(new Color(8, 124, 250));
    
                        File fi = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\");
                        try {
                            for (int i = 0; i < IdleMkr.structures.size(); i++) {
                                Structure struct = structures.get(i);
                                File file = new File(fi.getPath() + "\\purchases\\" + struct.getID() + ".txt");
                                if (!file.exists()) {
                                    file.createNewFile();
                                    FileWriter writer = new FileWriter(file);
                                    writer.write("0");
                                    writer.close();
                                }
                                Scanner sc = new Scanner(file);
    
                                BigInteger num = new BigInteger(sc.nextLine());
                                BigInteger costfor1 = (new BigInteger("" + struct.cost).add(num.multiply(num)).add(new BigInteger("" + struct.cost)));
    
                                embedBuilder.addField(struct.name, "cps: **" + struct.cps + "**, cost: **" + costfor1 + "**, Count: **" + num + "**", false);
    
                                msg += "`" + i + ". " + struct.name + "`\n";
    
                                sc.close();
                            }
    
                            //event.getChannel().sendMessage(msg).complete();
    
                            MessageAction msg2 = event.getChannel().sendMessage(" ").embed(embedBuilder.build());
                            msg2.complete();
                        } catch (IOException err) {
                            embedBuilder.setColor(new Color(250, 63, 10));
                            embedBuilder.clearFields();
                            embedBuilder.addField("An error occurred:", "`" + err.getMessage() + "`", true);
                            embedBuilder.addField("Try doing " + prefix + "start,", "then try again, if it fails again,", false);
                            embedBuilder.addField("Please report this to:", "GiantLuigi4#6616", false);
                            MessageAction msg2 = event.getChannel().sendMessage(" ").embed(embedBuilder.build());
                            msg2.complete();
                            err.printStackTrace();
                        } catch (NoSuchElementException err) {
                            embedBuilder.setColor(new Color(250, 63, 10));
                            embedBuilder.clearFields();
                            embedBuilder.addField("An error occurred:", "`" + err.getMessage() + "`", true);
                            embedBuilder.addField("Try doing" + prefix + "buy 0,", "then try again, if it fails again,", false);
                            embedBuilder.addField("Please report this to:", "GiantLuigi4#6616", false);
                            MessageAction msg2 = event.getChannel().sendMessage(" ").embed(embedBuilder.build());
                            msg2.complete();
                            err.printStackTrace();
                        }
    
                    }
                    if (messageText.startsWith(prefix + "start")) {
                        String userid = message.getAuthor().getId();
                        String mention = message.getAuthor().getAsMention();
    
                        File fi = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid);
                        File fi2 = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\" + "lastupdated.txt");
                        //File fi3 = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\"+userid+"\\"+"purchases.txt");
                        File fi4 = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\" + "totalcoins.txt");
                        File fi5 = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\" + "personalprefix.txt");
    
                        fi.mkdirs();
    
                        for (int i = 0; i < structures.size(); i++) {
                            String file = BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\purchases\\" + structures.get(i).getID() + ".txt";
                            File fistruct = new File(file);
                            if (!fistruct.exists()) {
                                try {
                                    fistruct.createNewFile();
                                    FileWriter fiwrite = new FileWriter(fistruct);
                                    fiwrite.write("0");
                                    fiwrite.close();
                                } catch (IOException err) {
                                }
                            } else {
                                try {
                                    FileWriter fiwrite = new FileWriter(fistruct);
                                    fiwrite.write("0");
                                    fiwrite.close();
                                } catch (IOException err) {
                                }
                            }
                        }
    
                        //fi.mkdirs();
    
                        try {
                            fi2.createNewFile();
                            //fi3.createNewFile();
                            fi4.createNewFile();
                            fi5.createNewFile();
                        } catch (IOException err) {
                        }
    
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException err) {
        
                        }
    
                        Date date = new Date();
                        //System.out.println(date.getTime()+"\n"+5);
                        try {
                            FileWriter righter = new FileWriter(fi2);
                            righter.write(date.getTime() + "\n" + 5);
                            righter.close();
                            //FileWriter righter2 = new FileWriter(fi3);
                            //righter2.write("0");
                            //righter2.close();
                            FileWriter righter3 = new FileWriter(fi4);
                            righter3.write("0");
                            righter3.close();
                            FileWriter righter4 = new FileWriter(fi5);
                            righter4.write("-idle:");
                            righter4.close();
                        } catch (IOException err) {
                        }
    
                        String username = message.getAuthor().getName();
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setAuthor(username + " has created a profile!");
                        embedBuilder.setTitle(" ");
                        embedBuilder.setColor(new Color(8, 124, 250));
                        embedBuilder.addField("Time started:", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()), true);
    
                        event.getChannel().sendMessage(" ").embed(embedBuilder.build()).complete();
    
                        //Message msg2 = event.getChannel().sendMessage(mention + "! Your profile has successfully been created!").complete();
                        //event.getChannel().sendMessage(msg2);
    
                    }
                    if (messageText.startsWith(prefix + "stats")) {
                        try {
                            String userid = message.getAuthor().getId();
                            String mention = message.getAuthor().getAsMention();
                            String name = message.getAuthor().getName();
                            if (messageText.contains("@")) {
                                String raw = event.getMessage().getContentRaw();
                                userid = raw.substring((prefix + "stats <@!").length(), raw.length() - 1);
                                System.out.println(userid);
                                name = event.getJDA().getUserById(userid).getName();
                            }
                            String currency = "" + HandleStructs.getAmt(userid, HandleStructs.getCPS(userid));
                            String cps = "" + HandleStructs.getCPS(userid);
                            String totalcoins = "";
                            try {
                                Scanner sc = new Scanner(new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\" + "totalcoins.txt"));
                                totalcoins = "" + sc.nextLine();
                                sc.close();
                            } catch (IOException err) {
                            }
    
                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setAuthor(message.getAuthor().getName() + " requested:");
                            embedBuilder.setTitle(name + "'s stats");
                            embedBuilder.setColor(new Color(8, 124, 250));
                            embedBuilder.addField(currencyname + ":", currency, true);
                            embedBuilder.addField(currencyname + " per second:", cps, false);
                            embedBuilder.addField(currencyname + " in lifetime:", totalcoins, false);
                            MessageAction msg = event.getChannel().sendMessage(" ").embed(embedBuilder.build());
                            msg.complete();
                        } catch (NullPointerException err) {
                            event.getChannel().sendMessage("Error:`" + err.getMessage() + "`.\nProbably caused by the user not having a profile.");
                        }
                    }
                    if (messageText.startsWith("-idle:" + "suggest")) {
                        try {
                            String[] args = messageText.split("-", 6);
                            //
                            String desc = args[5];
                            String truedesc = "";
                            //
                            String basecost = "";
                            //
                            //
                            //System.out.println(args[5]);
                            //
                            if (desc.contains(" -")) {
                                for (int i = desc.lastIndexOf(" -") + 2; i < desc.length(); i++) {
                                    basecost += desc.charAt(i);
                                }
                                for (int i = 0; i < desc.lastIndexOf(" -"); i++) {
                                    truedesc += desc.charAt(i);
                                }
                                //System.out.println("b");
                            } else {
                                truedesc = desc;
                                //System.out.println("a");
                            }
                            //
                            int bc = 0;
                            if (basecost.equals("")) {
                                bc = (int) Math.sqrt(Integer.parseInt(args[4].split(" ", 2)[0])) / 1;
                                //System.out.println(bc);
                                basecost = "" + bc;
                            }
                            //
                        /*System.out.println(
                                args[0] + "\n" +
                                        args[1].substring(0,args[1].length()-1) + "\n" +
                                        args[2].substring(0,args[2].length()-1) + "\n" +
                                        args[3].substring(0,args[3].length()-1) + "\n" +
                                        args[4].split(" ",2)[0] + "\n" +
                                        truedesc + "\n" +
                                        basecost + "\n"
                        );*/
    
                            File fi = new File(BunchOBots.drive+":\\bot\\idlemk\\awatingconf\\" + args[2].substring(0, args[2].length() - 1) + "\\" + args[3].substring(0, args[3].length() - 1));
                            File fi2 = new File(BunchOBots.drive+":\\bot\\idlemk\\awatingconf\\" + args[2].substring(0, args[2].length() - 1) + "\\" + args[3].substring(0, args[3].length() - 1) + "\\suggestion.txt");
                            fi2.getParentFile().mkdirs();
                            Message msg = event.getChannel().sendMessage("Adding suggestion.").complete();
                            event.getChannel().sendMessage(msg);
                            try {
                                if (!fi.exists() || (fi2.isDirectory() || !fi2.exists())) {
                                    fi2.createNewFile();
    
                                    FileWriter righter = new FileWriter(fi2);
                                    righter.write(
                                            "cps/func:\n"
                                                    + args[4].split(" ", 2)[0] + "\n"
                                                    + "desc:\n"
                                                    + truedesc + "\n"
                                                    + "bc:" + "\n"
                                                    + basecost + "\n"
                                    );
                                    righter.close();
    
                                    String name = message.getAuthor().getName();
    
                                    EmbedBuilder embedBuilder = new EmbedBuilder();
                                    embedBuilder.setAuthor(name + " suggested:");
                                    embedBuilder.setTitle(args[3].substring(0, args[3].length() - 1));
                                    embedBuilder.setColor(new Color(8, 124, 250));
                                    embedBuilder.addField("Type:", args[2].substring(0, args[2].length() - 1), false);
                                    embedBuilder.addField("CPS/function:", args[4].split(" ", 2)[0], true);
                                    embedBuilder.addField("Description:", truedesc, false);
                                    embedBuilder.addField("Base Cost:", basecost, true);
                                    MessageAction msg2 = event.getChannel().sendMessage(" ").embed(embedBuilder.build());
                                    msg2.complete();
    
                                    //Message msg2 = event.getChannel().sendMessage("Suggestion added successfully.").complete();
                                    //event.getChannel().sendMessage(msg2);
                                } else {
                                    String name = message.getAuthor().getName();
    
                                    EmbedBuilder embedBuilder = new EmbedBuilder();
                                    embedBuilder.setAuthor(name);
                                    embedBuilder.setTitle("Suggestion Failed.");
                                    embedBuilder.setColor(new Color(250, 63, 10));
                                    embedBuilder.addField("Suggestion Type:", args[2].substring(0, args[2].length() - 1), false);
                                    embedBuilder.addField("Suggestion Name:", args[3].substring(0, args[3].length() - 1), false);
                                    MessageAction msg2 = event.getChannel().sendMessage(" ").embed(embedBuilder.build());
                                    msg2.complete();
    
                                    //Message msg2 = event.getChannel().sendMessage("Suggestion has already been made.").complete();
                                    //event.getChannel().sendMessage(msg2);
                                }
                            } catch (IOException err) {
                                String name = message.getAuthor().getName();
    
                                EmbedBuilder embedBuilder = new EmbedBuilder();
                                embedBuilder.setAuthor(name);
                                embedBuilder.setTitle("Suggestion Failed.");
                                embedBuilder.setColor(new Color(250, 10, 10));
                                embedBuilder.addField("Error:", "`" + err.getMessage() + "`", false);
                                embedBuilder.addField("Report this error to:", "GiantLuigi4#6616", false);
                                MessageAction msg2 = event.getChannel().sendMessage(" ").embed(embedBuilder.build());
                                msg2.complete();
    
                                //Message msg2 = event.getChannel().sendMessage("An error occured:`" + err.getMessage() + "`. Please contact GiantLuigi4#6616").complete();
                                //event.getChannel().sendMessage(msg2);
                            }
    
                        } catch (ArrayIndexOutOfBoundsException err) {
                            String name = message.getAuthor().getName();
    
                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setAuthor(name);
                            embedBuilder.setTitle("Suggestion Failed.");
                            embedBuilder.setColor(new Color(255, 50, 25));
                            embedBuilder.addField("", "You must fill out ALL arguments. Do -idleMaker:help for help", false);
                            MessageAction msg2 = event.getChannel().sendMessage(" ").embed(embedBuilder.build());
                            msg2.complete();
    
                            //Message msg2 = event.getChannel().sendMessage("Please fill out ALL arguments.").complete();
                            //event.getChannel().sendMessage(msg2);
                        }
                    }
                }
            }
        }
    }
}
