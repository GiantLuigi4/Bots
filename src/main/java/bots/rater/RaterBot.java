package bots.rater;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import org.slf4j.spi.MDCAdapter;
import utils.PropertyReader;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class RaterBot extends ListenerAdapter {
    public static JFrame frame = new JFrame("Debug Window-output");
    public static JFrame confg = new JFrame("Confgs");
    public static JWindow frameIMG = new JWindow();
    
    private static int pixelL = 0;
    private static int pixelCheckX = 0;
    private static int pixelT = 0;
    private static int pixelCheckY = 0;
    private static int pixelR = 0;
    private static int pixelD = 0;
    
    private static String id = "640589019989803019";
    
    private static BufferedImage comparisonBimig = null;
    
    public static JDA botBuilt;
    
    public static boolean disabled;
    
    public static JComponent component = new JComponent() {
        @Override
        public void paint(Graphics gOrig) {
            Graphics2D g = (Graphics2D) gOrig;
            BufferedImage imageCompare = null;
            if (comparisonBimig != null) {
                imageCompare = toBufferedImage(comparisonBimig.getScaledInstance(pixelR - pixelL, pixelD - pixelT, 0));
            }
            if (isShown && shown) {
                RenderingHints rh = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                //rh.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHints(rh);
                frameIMG.setLocation(frame.getX(), frame.getY() + frame.getHeight());
                if (img != null) {
                    //int Height=(int)(((2-(1f/frame.getWidth()))*img.getHeight()));
                    //Image image = img.getScaledInstance(frame.getWidth(),Height,0);
                    Image image = img.getScaledInstance(frame.getWidth(), frame.getHeight(), 0);
                    g.drawImage(image, 0, 0, null);
    
                    for (int x = 0; x < img.getWidth(); x++) {
                        for (int y = 0; y < img.getHeight(); y++) {
                            g.setColor(new Color(img.getRGB(x, y)));
                            g.setColor(new Color(ratedImg.getRGB(x, y)));
                            g.fillRect((int) (x), (int) (y), 1, 1);
                            if (x >= pixelL + 1 && y >= pixelT + 1 && x <= pixelR - 1 && y <= pixelD - 1 && comparisonBimig != null)
                            //if(x==pixelL+1 && y==pixelT+1)
                            {
                                try {
    
                                    if (comparisonBimig != null) {
                                        //g.drawImage(imageCompare,x,y,null);
                                        Color col = new Color(imageCompare.getRGB(x - pixelL, y - pixelT));
                                        try {
                                            try {
                                                g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue()));
                                            } catch (IllegalArgumentException err) {
                                                int red;
                                                int green;
                                                int blue;
                                                if (err.getCause().getMessage().contains("green")) {
                                                    green = 255;
                                                } else {
                                                    green = col.getGreen();
                                                }
                                                if (err.getCause().getMessage().contains("red")) {
                                                    red = 255;
                                                } else {
                                                    red = col.getRed();
                                                }
                                                if (err.getCause().getMessage().contains("blue")) {
                                                    blue = 255;
                                                } else {
                                                    blue = col.getBlue();
                                                }
                                                g.setColor(new Color(red, green, blue));
                                            }
                                            g.fillRect((int) (x), (int) (y), 1, 1);
                                        } catch (NullPointerException err) {
                                            g.setColor(new Color(0, 0, 0, 1));
                                        }
                                    }
    
                                } catch (ArrayIndexOutOfBoundsException err) {
                                }
                            }
                        }
                    }
    
                    g.setColor(Color.black);
                    g.drawRect(pixelL, pixelT, pixelR - pixelL, pixelD - pixelT);
                    g.setColor(Color.red);
                    g.drawRect(pixelCheckX + pixelL - 1, pixelCheckY + pixelT - 1, 3, 3);
                    frameIMG.setSize(img.getWidth(), img.getHeight());
                    //frame.setSize(frame.getWidth(),(int)(Height));
                    //textArea.append(Height+"\n");
                    //Color test = new Color(90446);
                } else {
                    frameIMG.setSize(frame.getWidth(), frame.getHeight());
                }
            }
        }
    };
    
    public static JPanel comp = new JPanel(new GridBagLayout());
    
    public static JTextArea textArea = new JTextArea();
    public static JTextArea confgs = new JTextArea();
    public static JScrollPane scrollPane;
    
    public static boolean open = true;
    public static boolean shown = true;
    public static boolean isShown = true;
    public static boolean debug = true;
    public static BufferedImage img = null;
    public static BufferedImage ratedImg = null;
    
    public static File folder = null;
    public static File fileSave = null;
    public static File fileSave2 = null;
    public static Boolean saveFile = false;
    
    public static float GifRating = 0;
    
    public static void main(String[] a) throws LoginException {
        MDCAdapter mdcAdapter = new MDCAdapter() {
            @Override
            public void put(String key, String val) {
    
            }
    
            @Override
            public String get(String key) {
                return null;
            }
    
            @Override
            public void remove(String key) {
        
            }
    
            @Override
            public void clear() {
        
            }
    
            @Override
            public Map<String, String> getCopyOfContextMap() {
                return null;
            }
    
            @Override
            public void setContextMap(Map<String, String> contextMap) {
        
            }
        };
    
        if (PropertyReader.contains("bots.properties", "rbPublic"))
            PublicRaterBot.main(a);
        
        //functions.getPath();
        
        String grabbedToken = PropertyReader.read("bots.properties", "rbPrivate");
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(grabbedToken);
        builder.setGame(Game.watching("for -rater:help, version:" + Messages.version));
        RaterBot bot = new RaterBot();
        builder.addEventListener(bot);
        botBuilt = builder.buildAsync();
        try {
            Thread.sleep(1000);
            id = botBuilt.getSelfUser().getId();
        } catch (Throwable ignored) {
        }
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
    
            }
    
            @Override
            public void windowClosing(WindowEvent e) {
                open = false;
                //System.out.println("Bye");
            }
    
            @Override
            public void windowClosed(WindowEvent e) {
                open = false;
        
                //System.out.println("Bye");
            }
    
            @Override
            public void windowIconified(WindowEvent e) {
                shown = false;
            }
    
            @Override
            public void windowDeiconified(WindowEvent e) {
                shown = true;
            }
    
            @Override
            public void windowActivated(WindowEvent e) {
                isShown = true;
            }
    
            @Override
            public void windowDeactivated(WindowEvent e) {
                isShown = false;
            }
        });
        GridBagConstraints c = new GridBagConstraints();
        
        frameIMG.setSize(200, 200);
        frame.setSize(200, 200);
        textArea.setAutoscrolls(true);
        textArea.setSize(40, 100);
        //textArea.setLineWrap(true);
        
        //https://stackoverflow.com/questions/10292792/getting-image-from-url-java
        Image ico = null;
        /*try {
            URL url = new URL("https://cdn.discordapp.com/avatars/640589019989803019/fab67dd29d7c8552a831eeaa5ffec647.png");
            InputStream is = url.openStream();
            ico=ImageIO.read(is).getScaledInstance(128,128,0);
            is.close();
        } catch (MalformedURLException err)
        {
            System.out.println("MalformedURLException");
        } catch (IOException err)
        {
            System.out.println("IOException");
        } catch (NullPointerException err)
        {
            System.out.println("NullPointerException");
        }*/
        
        frame.setIconImage(ico);
        
        //frame.add(textArea);
        scrollPane = new JScrollPane(textArea);
        scrollPane.setAutoscrolls(true);
        //frame.add(scrollPane);
        scrollPane.validate();
        
        c.fill = GridBagConstraints.HORIZONTAL;
        comp.add(scrollPane, c);
        
        confg.setAlwaysOnTop(true);
        confg.validate();
        confgs.setLineWrap(true);
        
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        confg.add(confgs);
        
        c.ipady = (int) frame.getBounds().getHeight();
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        comp.add(scrollPane, c);
        
        confg.setUndecorated(true);
        
        comp.setBackground(new Color(238, 238, 238, 255));
        comp.validate();
        //frame.add(scrollPane);
        frame.add(comp);
        //frame.pack();
        frameIMG.add(component);
        //frameIMG.setUndecorated(true);
        frame.validate();
        try {
            if (Boolean.parseBoolean(PropertyReader.read("Settings.properties", "debugLog"))) {
                frameIMG.setVisible(true);
                frame.setVisible(true);
                confg.setVisible(true);
            }
        } catch (Throwable ignored) {
        }
        
        Thread thread = new Thread(() -> {
            while (open) {
                frameIMG.setAlwaysOnTop(true);
                frame.setAlwaysOnTop(true);
                if (frame.isActive()) {
                    frameIMG.setVisible(true);
                } else {
                    frameIMG.setVisible(false);
                }
                frame.setSize(frame.getWidth(), 300);
                confg.setSize(20, frame.getHeight());
                confg.setLocation(frame.getX() + frame.getWidth() - 10, frame.getY());
                //confg.pack();
                //frame.toFront();
                component.repaint();
                boolean toDebug = false;
                for (String text : confgs.getText().split(" ")) {
                    if (!toDebug) {
                        toDebug = (open && text.contains("-debug"));
                    }
                    debug = toDebug;
                    if (text.contains("-save:debug_")) {
                        for (String text2 : text.split("_")) {
                            File file = new File(text2);
                            if (file.isDirectory()) {
                                if (file.canRead() && file.canWrite()) {
                                    folder = new File(text2 + ("\\" + (System.currentTimeMillis() * System.nanoTime())));
                                    fileSave = new File(text2 + (folder.getAbsolutePath() + "\\" + "rated.png"));
                                    fileSave2 = new File(text2 + (folder.getAbsolutePath() + "\\" + "original.png"));
                                    //System.out.println(folder.getAbsolutePath());
                                    saveFile = true;
                                }
                            }
                        }
                    }
                }
            }
        });
        thread.start();
        //builder.setEnableShutdownHook(true);
//        Runtime.getRuntime().exit(0);
        
        //392384-perms token
    }
    
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            if (event.getJDA().getSelfUser().getId().equals(id)) {
                //Message message = event.getMessage();
                String msgId = event.getMessageId();
                String Chan = event.getTextChannel().getName();
                String serv = event.getGuild().getName();
                //User user = event.getJDA().getSelfUser();
                Message message = event.getChannel().getMessageById(msgId).complete();
                String messageText = message.getContentDisplay();

        /*if(messageText.startsWith("-rater:disable"))
            disabled=true;
        if(messageText.startsWith("-rater:enable"))
            disabled=false;*/
    
                if (Chan.toLowerCase().contains("bot") && !disabled) {
                    if (messageText.startsWith("-rate:user")) {
                        boolean Ping = false;
                        String username = messageText.split(" ", 2)[1];
                        if (username.startsWith("@")) {
                            username = username.substring(1);
                            if (debug)
                                textArea.append("Rated by ping, results may be off" + "\n");
                            Ping = true;
                        }
    
                        float activity = 0;
                        float messagesRead = 0;
                        Message msgRate = event.getChannel().sendMessage("Loading... Please wait...").complete();
                        if (debug)
                            textArea.append("RankUser:" + username + "\n");
                        if (true) {
                            List<TextChannel> channels = event.getAuthor().getJDA().getTextChannels();
                            for (TextChannel chn : channels) {
                                if (debug)
                                    textArea.append("Read Channel:" + chn.getName() + "\n");
                                if ((!chn.getName().startsWith("_") && !chn.getName().toLowerCase().contains("bot") && !chn.getParent().getName().startsWith("-") && !chn.getParent().getName().toLowerCase().contains("bot")) && (chn.getGuild().equals(event.getGuild()) || messageText.contains("-ignore:server") || messageText.contains("-ignore:serv"))) {
                                    try {
                                        List<Message> msgs = chn.getHistory().retrievePast(100).complete();
                                        for (Message msg : msgs) {
                                            if (msg.getGuild().getName().equals(serv) || messageText.contains("-ignore:server") || messageText.contains("-ignore:serv")) {
                                                String sender = msg.getAuthor().getName();
    
                                                String usernameWithoutLastFourChars = username.substring(0, username.length() - 4);
    
                                                if (debug)
                                                    textArea.append(usernameWithoutLastFourChars + "\n");
    
                                                if (usernameWithoutLastFourChars.endsWith("#"))
                                                    sender += "#" + msg.getAuthor().getDiscriminator();
    
                                                if (debug)
                                                    textArea.append(username + ":" + sender + "\n");
    
                                                if (sender.equals(username)) {
                                                    activity += 1f;
                                                    if (messageText.toLowerCase().contains("bruh"))
                                                        activity /= 5;
                                                    messagesRead += 1f;
                                                    if (debug)
                                                        textArea.append("SentByUser!" + "\n");
                                                } else {
                                                    messagesRead += 1f;
                                                }
                                            }
                                        }
                                    } catch (Throwable err) {
                                        textArea.append(err.getLocalizedMessage());
                                    }
                                    if (debug)
                                        textArea.append("Finished A Message History" + "\n");
                                } else {
                                    if (debug)
                                        textArea.append("UnreadChannel" + "\n");
                                }
                            }
                            try {
                                activity = activity / messagesRead;
                                activity *= 10;
                                if (activity >= 10) {
                                    Emote emote = botBuilt.getEmoteById(Long.parseLong("641385084611723266"));
                                    msgRate.editMessage("I rate " + username + " a " + emote.getAsMention() + "/" + emote.getAsMention()).complete();
                                } else {
                                    msgRate.editMessage("I rate " + username + " a " + activity + "/10").complete();
                                }
                                if (Ping)
                                    event.getChannel().sendMessage("Rate command sent with a ping, results may be completely off.").complete();
                            } catch (Throwable err) {
                                EmbedBuilder builder = new EmbedBuilder();
                                builder.setColor(Color.RED);
                                builder.setAuthor(event.getAuthor().getName());
                                builder.setTitle("Error!");
                                builder.addField("An error occurred:", err.toString(), false);
                                if (err.getLocalizedMessage() != null) {
                                    builder.addField("Message:", err.getLocalizedMessage(), false);
                                } else if (err.getMessage() != null) {
                                    builder.addField("Message:", err.getMessage(), false);
                                }
                                event.getChannel().sendMessage(" ").embed(builder.build()).complete();
                            }
                        }
                    }
                    if (messageText.startsWith("-rater:help")) {
                        if (messageText.startsWith("-rater:help -rate:image")) {
                            Message msgRate = event.getChannel().sendMessage(Messages.helpImage).complete();
                        } else if (messageText.startsWith("-rater:help -rate:user")) {
                            Message msgRate = event.getChannel().sendMessage(Messages.helpUser).complete();
                        } else {
                            event.getChannel().sendMessage(" ").embed(Messages.buildHelp(event.getAuthor().getName()).build()).complete();
                        }
                    }
                    if (messageText.startsWith("-rater:clear") || messageText.startsWith("-rater:empty")) {
                        img = null;
                    }
                    if (
                            messageText.startsWith("-rate:image")
                        //|| messageText.startsWith("-rate:emote")
                    ) {
                        boolean hasImage = false;
                        boolean emoteRate = messageText.startsWith("-rate:emote");
                        boolean outputFile = (messageText.contains("-output:ratedImage") || messageText.contains("-out:ratedImage") || messageText.contains("-output:rated") || messageText.contains("-out:rated"));
                        boolean outputLegacy = (messageText.contains("-output:legacyRated") || messageText.contains("-out:legacy") || messageText.contains("-output:legacy") || messageText.contains("-out:legacyRated"));
                        boolean decimal = (messageText.contains("-output:decimal") || messageText.contains("-out:decimal") || messageText.contains("-output:deci") || messageText.contains("-out:deci"));
                        //Message msgRate = event.getChannel().sendMessage("").complete();
                        if (emoteRate) {
                            String emote = messageText.split(" ", 2)[1];
                            //String id = emote.replaceAll("<:.+:(\\d+)>", "$1");
                            //Emote emo = event.getJDA().getEmotesByName(id,false).get(0);
    
                            //String id = emote.replaceAll("<:(\\d+).+:>", "$1");
                            Emote emo = event.getJDA().getEmotes().get(0);
    
                            System.out.println(emo.getImageUrl());
    
                            Image ico = null;
                            try {
                                URL url = new URL(emo.getImageUrl());
                                ico = ImageIO.read(url);
                                System.out.println(ico.getWidth(null));
                            } catch (MalformedURLException err) {
                            } catch (IOException err) {
                            }
                            rateImage(ico, event, outputFile, outputLegacy, decimal, false);
                        } else {
                            List<Message.Attachment> attachments = event.getMessage().getAttachments();
                            for (Message.Attachment attach : attachments) {
                                hasImage = true;
                                //if (attach.isImage())
                                if (attach.isImage()) {
                                    //if()
    
                                    try {
                                        if (attach.getFileName().endsWith(".gif")) {
                                    /*InputStream inputs = attach.getInputStream();
                                    ImageInputStream stream = ImageIO.createImageInputStream(inputs);
                                    ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
                                    reader.setInput(stream);

                                    for (int img = 0; img<=reader.getNumImages(false); img++)
                                    {
                                        System.out.println("h");
                                        reader.read(img);
                                        BufferedImage thisImg = reader.read(img);
                                        BufferedImage nextImg = reader.read(img);
                                        try {
                                            nextImg = reader.read(img+1);
                                        } catch (IndexOutOfBoundsException err)
                                        { }

                                        for (int x = 0; x<=thisImg.getWidth()-1;x+=1) {
                                            for (int y = 0; y <= thisImg.getHeight() - 1; y += 1) {
                                                thisImg.setRGB(x,y,((thisImg.getRGB(x,y)+nextImg.getRGB(x,y))/2));
                                            }
                                        }


                                        GifRating+=rateImage(thisImg.getScaledInstance(reader.getWidth(img),reader.getHeight(img),0),event,false,false,true,true);
                                        GifRating/=1.5;
                                    }

                                    if(GifRating>=10)
                                    {
                                        Emote emote = botBuilt.getEmoteById(Long.parseLong("641385084611723266"));
                                        Message msgRate = event.getChannel().sendMessage("I rate that image a " + emote.getAsMention() + "/" + emote.getAsMention()).complete();
                                    } else {
                                        Message msgRate = event.getChannel().sendMessage("I rate that image a " + GifRating + "/10").complete();
                                    }*/

                                    /*try {

                                        InputStream inputs = attach.getInputStream();
                                        ImageInputStream stream = ImageIO.createImageInputStream(inputs);

                                        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
                                        reader.setInput(stream);

                                        ImageReader gifImageReader = new GIFImageReader(new GIFImageReaderSpi());

                                        gifImageReader.setInput(stream);

                                        int images = gifImageReader.getNumImages(true);

                                        GifRating=0;

                                        File fo = folder;
                                        fold=fo;

                                        for (int image=0; image < images; image++)
                                        {
                                            BufferedImage thisImg = gifImageReader.read(image);
                                            BufferedImage nextImg = gifImageReader.read(image);
                                            try {
                                                nextImg = gifImageReader.read(image+1);
                                            } catch (IndexOutOfBoundsException err)
                                            { }

                                            for (int x = 0; x<=thisImg.getWidth()-1;x+=1) {
                                                for (int y = 0; y <= thisImg.getHeight() - 1; y += 1) {
                                                    thisImg.setRGB(x,y,((thisImg.getRGB(x,y)+nextImg.getRGB(x,y))/2));
                                                }
                                            }


                                            GifRating+=rateImage(thisImg.getScaledInstance(gifImageReader.getWidth(image),gifImageReader.getHeight(image),0),event,false,false,true,true);
                                            GifRating/=1.5;
                                        }
                                    } catch (IOException err) {}*/

                                        /*if(saveFile)
                                        {
                                            //File fo = folder;
                                            //long time = System.nanoTime()*System.currentTimeMillis();
                                            long time = image;
                                            File fi = new File(fo.getAbsolutePath()+"\\"+(time)+"-rated.png");
                                            File fi2 = new File(fo.getAbsolutePath()+"\\"+(time)+"-original.png");
                                            fo.mkdir();
                                            //fo.mkdirs();
                                            //fi.mkdirs();
                                            //fi2.mkdirs();
                                            try{
                                                fi.createNewFile();
                                                ImageIO.write(ratedImg, "png", fi);
                                            } catch (IOException err)
                                            {
                                                System.out.println("1");
                                            }
                                            try{
                                                fi2.createNewFile();
                                                ImageIO.write(thisImg, "png", fi2);
                                            } catch (IOException err)
                                            {
                                                System.out.println("2");
                                            }
                                        }
                                    }

                                    if(GifRating>=10)
                                    {
                                        Emote emote = botBuilt.getEmoteById(Long.parseLong("641385084611723266"));
                                        Message msgRate = event.getChannel().sendMessage("I rate that image a " + emote.getAsMention() + "/" + emote.getAsMention()).complete();
                                    } else {
                                        Message msgRate = event.getChannel().sendMessage("I rate that image a " + GifRating + "/10").complete();
                                    }*/
    
                                            //String dir = System.getProperty("user.dir") + File.separatorChar;
                                            //String F = dir + "\\memory";
    
                                            //F = dir + "\\memory\\" + attach.getFileName();
    
                                            //Icon ico = (Icon)attach.getAsIcon();
    
                                            //BufferedImage imge = ImageIO.read(new URL(attach.getProxyUrl()));
                                            //BufferedImage imge=iconToImage(ico);
                                            //BufferedImage imge = ImageIO.read(inputs);
    
    
                                            //ImageIO.write(imge, "png", new File(F));
    
                                            //ImageInputStream input = ImageIO.createImageInputStream(new File(F));
    
    
                                            //Image image = ImageIO.read(stream);*/
                                        } else {
                                            //http://www.java2s.com/Code/Java/2D-Graphics-GUI/ReadanImagefrominputStream.htm
                                            InputStream inputs = attach.getInputStream();
                                            BufferedImage img = ImageIO.read(inputs);
                                            rateImage(img, event, outputFile, outputLegacy, decimal, false);
                                        }
                                    } catch (IOException err) {
                                        Message msgRate = event.getChannel().sendMessage("An error occurred, please try again.:" + err.getMessage()).complete();
                                    }
                                } else {
                                    Message msgRate = event.getChannel().sendMessage("That is not an image.").complete();
                                }
                            }
                            if (!hasImage) {
                                Message msgRate = event.getChannel().sendMessage("Please attach an image.").complete();
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
    }
    
    public static float rateImage(Image image, MessageReceivedEvent e, boolean outputFile, boolean Legacy, boolean decimal, boolean gif) {
        float DifferenceInPxls = 0;
        float diff2 = 0;
        
        BufferedImage reader = toBufferedImage(image);
        
        File fo = folder;
        if (saveFile) {
            File fi = new File(fo.getAbsolutePath() + "\\rated.png");
            File fi2 = new File(fo.getAbsolutePath() + "\\original.png");
            fo.mkdirs();
            try {
                fi2.createNewFile();
                ImageIO.write(reader, "png", fi2);
            } catch (IOException err) {
                System.out.println("2");
            }
        }
        
        BufferedImage readerCopy = toBufferedImage(image);
        img = reader;
        //BufferedImage imgCopy=readerCopy;
        //ratedImg=reader;
        
        ratedImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        BufferedImage red = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        BufferedImage green = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        BufferedImage blue = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        BufferedImage img2 = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        
        //reader=origin;
        
        for (int x = 0; x <= reader.getWidth() - 1; x += 1) {
            for (int y = 0; y <= reader.getHeight() - 1; y += 1) {
                Color[] cols = new Color[10];
                cols[0] = new Color(reader.getRGB(x, y));
                cols[1] = new Color(reader.getRGB(x, y));
                cols[2] = new Color(reader.getRGB(x, y));
                cols[3] = new Color(reader.getRGB(x, y));
                cols[4] = new Color(reader.getRGB(x, y));
                cols[5] = new Color(reader.getRGB(x, y));
                cols[6] = new Color(reader.getRGB(x, y));
                cols[7] = new Color(reader.getRGB(x, y));
                cols[8] = new Color(reader.getRGB(x, y));
                cols[9] = new Color(reader.getRGB(x, y));
    
                try {
                    cols[1] = new Color(reader.getRGB(x - 1, y));
                    cols[6] = new Color(img2.getRGB(x - 1, y));
                } catch (ArrayIndexOutOfBoundsException err) {
                }
                try {
                    cols[2] = new Color(reader.getRGB(x, y - 1));
                    cols[7] = new Color(img2.getRGB(x, y - 1));
                } catch (ArrayIndexOutOfBoundsException err) {
                }
                try {
                    cols[3] = new Color(reader.getRGB(x + 1, y));
                    cols[8] = new Color(reader.getRGB(x + 1, y));
                } catch (ArrayIndexOutOfBoundsException err) {
                }
                try {
                    cols[4] = new Color(reader.getRGB(x, y + 1));
                    cols[9] = new Color(reader.getRGB(x, y + 1));
                } catch (ArrayIndexOutOfBoundsException err) {
                }
    
                float CollorAvg = 0;
    
                for (int colo1 = 0; colo1 < 5; colo1++) {
                    for (int colo2 = colo1; colo2 < cols.length; colo2++) {
                        Color col1 = cols[colo1];
                        Color col2 = cols[colo2];
                        try {
                            if (col1.getRGB() >= col2.getRGB()) {
                                CollorAvg += (col2.getRed() / col1.getRed());
                                CollorAvg += (col2.getBlue() / col1.getBlue());
                                CollorAvg += (col2.getGreen() / col1.getGreen());
                            } else {
                                CollorAvg += (col1.getRed() / col2.getRed());
                                CollorAvg += (col1.getBlue() / col2.getBlue());
                                CollorAvg += (col1.getGreen() / col2.getGreen());
                            }
                        } catch (ArithmeticException err) {
                            CollorAvg += 1;
                        }
                        CollorAvg /= 2;
                    }
                }
    
                float CollorAvg2 = 0;
    
                for (int colo1 = 5; colo1 < cols.length; colo1++) {
                    for (int colo2 = colo1; colo2 < cols.length; colo2++) {
                        Color col1 = cols[colo1];
                        Color col2 = cols[colo2];
                        try {
                            if (col1.getRGB() >= col2.getRGB()) {
                                CollorAvg2 += (col2.getRGB() / col1.getRGB());
                            } else {
                                CollorAvg2 += (col1.getRGB() / col2.getRGB());
                            }
                        } catch (ArithmeticException err) {
                            CollorAvg2 += 1;
                        }
                        CollorAvg2 /= 2;
                    }
                }
    
                int ColorPixel = (int) ((1 - (1f / (CollorAvg))) * 100000);
    
                int r = new Color(reader.getRGB(x, y)).getRed();
                int b = new Color(reader.getRGB(x, y)).getBlue();
                int g = new Color(reader.getRGB(x, y)).getGreen();
    
                red.setRGB(x, y, new Color(r, 0, 0).getRGB());
                green.setRGB(x, y, new Color(0, g, 0).getRGB());
                blue.setRGB(x, y, new Color(0, 0, b).getRGB());
    
                int ColorPixel2 = (int) ((1 - (1f / (CollorAvg2))) * 100000);
    
                //textArea.append(ColorPixel);
                ratedImg.setRGB(x, y, ColorPixel);
                img2.setRGB(x, y, ColorPixel);
                Graphics g1 = ratedImg.getGraphics();
                Graphics g2 = img2.getGraphics();
                g1.setColor(new Color(ColorPixel));
                g1.drawRect(x, y, 1, 1);
                g2.setColor(new Color(ColorPixel2));
                g2.drawRect(x, y, 1, 1);
    
                if (diff2 > 10) {
                    diff2 = (1 / diff2) * 1000;
                }
                if (diff2 > 10) {
                    diff2 = (1 / diff2) * 100;
                }
                if (diff2 > 10) {
                    diff2 = 10;
                }
    
                //origin.setRGB(x,y,reader.getRGB(x,y));
    
                //if(col1.getRGB()==(int)(139810.14f*600))
                //{
                //    DifferenceInPxls+=0;
                //} else {
                //float CollorAvg=((float)Math.abs(col1.getRGB()*col2.getRGB()*col3.getRGB()*col4.getRGB()*col5.getRGB()));
                DifferenceInPxls += CollorAvg;//((reader.getWidth()-1)*(reader.getHeight()-1));
                diff2 += CollorAvg2;//((reader.getWidth()-1)*(reader.getHeight()-1));
                //DifferenceInPxls=DifferenceInPxls;
                if (debug) {
                    textArea.append("Color:" + CollorAvg + "\n");
                    textArea.append("TotalDif:" + DifferenceInPxls + "\n");
                }
                //}
    
            }
        }
        
        int operations = 0;
        int stop = 0;
        float totalPercent = 0;
        /*try {
            for (int x = 0; x<=reader.getWidth()-1;x+=2) {
                pixelL=x;
                for (int y = 0; y <= reader.getHeight() - 1; y+=2) {
                    pixelT=y;
                    //System.out.println(""+x + "," + y);
                    stop=0;
                    for (BufferedImage bimig : functions.bimigs) {
                        if(stop == 0)
                        {
                            //System.out.println(""+x + "," + y);
                        }
                        if (stop != 1) {
                            if (reader.getWidth() - x >= bimig.getWidth() && reader.getHeight() - y >= bimig.getHeight()) {
                                try {
                                    //System.out.println("vvv");
                                    //BufferedImage red2 = red.getSubimage(x, y, red.getWidth() - x, red.getHeight() - y);
                                    //BufferedImage blue2 = blue.getSubimage(x, y, blue.getWidth() - x, blue.getHeight() - y);
                                    //BufferedImage green2 = green.getSubimage(x, y, green.getWidth() - x, green.getHeight() - y);
                                    for (int i = x+bimig.getWidth(); i <= red.getWidth() - 1 && stop!=1; i+=(int)red.getHeight()/16) {
                                        pixelR=x+i;
                                        for (int k = y+bimig.getHeight(); k <= red.getHeight() - 1 && stop!=1; k+=(int)(red.getHeight()/16)) {
                                            pixelD=y+k;


                                            BufferedImage red2Subimage = null;
                                            BufferedImage blue2Subimage = null;
                                            BufferedImage green2Subimage = null;
                                            //System.out.println("vv");
                                            try {
                                                //System.out.println("stop"+stop);
                                                float percentCorrect2 = 0;
                                                if (stop != 1) {
                                                    float percentCorrect = 0;
                                                    //red2Subimage = toBufferedImage(red2.getSubimage(0, 0, i, k).getScaledInstance(bimig.getWidth(), bimig.getHeight(), BufferedImage.SCALE_AREA_AVERAGING));
                                                    red2Subimage = toBufferedImage(red.getSubimage(x, y, i, k).getScaledInstance(bimig.getWidth(), bimig.getHeight(), BufferedImage.SCALE_AREA_AVERAGING));
                                                    //blue2Subimage = toBufferedImage(blue2.getSubimage(0, 0, i, k).getScaledInstance(bimig.getWidth(), bimig.getHeight(), BufferedImage.SCALE_AREA_AVERAGING));
                                                    blue2Subimage = toBufferedImage(blue.getSubimage(x, y, i, k).getScaledInstance(bimig.getWidth(), bimig.getHeight(), BufferedImage.SCALE_AREA_AVERAGING));
                                                    //green2Subimage = toBufferedImage(green2.getSubimage(0, 0, i, k).getScaledInstance(bimig.getWidth(), bimig.getHeight(), BufferedImage.SCALE_AREA_AVERAGING));
                                                    green2Subimage = toBufferedImage(green.getSubimage(x, y, i, k).getScaledInstance(bimig.getWidth(), bimig.getHeight(), BufferedImage.SCALE_AREA_AVERAGING));

                                                    comparisonBimig=null;
                                                    BufferedImage imgee = new BufferedImage(bimig.getWidth(),bimig.getHeight(),BufferedImage.TYPE_INT_ARGB);
                                                    Graphics2D comparisonGraphics = (Graphics2D)imgee.getGraphics();
                                                    comparisonBimig = imgee;

                                                    for (int m = 0; m <= bimig.getWidth() - 1 && stop!=1; m+=1) {
                                                        for (int c = 0; c <= bimig.getHeight() - 1 && stop!=1; c+=1) {

                                                            pixelCheckX=m*(red.getSubimage(0, 0, i, k).getWidth()/bimig.getWidth());
                                                            pixelCheckY=c*(red.getSubimage(0, 0, i, k).getHeight()/bimig.getHeight());

                                                            //if (operations == 8000000) {
                                                            //    try {
                                                            //        Thread.sleep(1);
                                                            //        operations=0;
                                                            //    } catch (InterruptedException err) {
                                                            //    }
                                                            //}

                                                            //System.out.println("v");
                                                            Color[] cols = new Color[5];
                                                            cols[0] = new Color(new Color(red2Subimage.getRGB(m, c)).getRed(), new Color(green2Subimage.getRGB(m, c)).getGreen(), new Color(blue2Subimage.getRGB(m, c)).getBlue());
                                                            cols[1] = cols[0];
                                                            cols[2] = cols[0];
                                                            cols[3] = cols[0];
                                                            cols[4] = cols[0];

                                                            try {
                                                                cols[1] = new Color(new Color(red2Subimage.getRGB(m - 1, c)).getRed(), new Color(green2Subimage.getRGB(m - 1, c)).getGreen(), new Color(blue2Subimage.getRGB(m - 1, c)).getBlue());
                                                            } catch (ArrayIndexOutOfBoundsException err) {
                                                            }
                                                            try {
                                                                cols[2] = new Color(new Color(red2Subimage.getRGB(m, c - 1)).getRed(), new Color(green2Subimage.getRGB(m, c - 1)).getGreen(), new Color(blue2Subimage.getRGB(m, c - 1)).getBlue());
                                                            } catch (ArrayIndexOutOfBoundsException err) {
                                                            }
                                                            try {
                                                                cols[3] = new Color(new Color(red2Subimage.getRGB(m + 1, c)).getRed(), new Color(green2Subimage.getRGB(m + 1, c)).getGreen(), new Color(blue2Subimage.getRGB(m + 1, c)).getBlue());
                                                            } catch (ArrayIndexOutOfBoundsException err) {
                                                            }
                                                            try {
                                                                cols[4] = new Color(new Color(red2Subimage.getRGB(m, c + 1)).getRed(), new Color(green2Subimage.getRGB(m, c + 1)).getGreen(), new Color(blue2Subimage.getRGB(m, c + 1)).getBlue());
                                                            } catch (ArrayIndexOutOfBoundsException err) {
                                                            }

                                                            //System.out.println("" + m + "," + c + "," + x + "," + y);

                                                            Color priorityRed = new Color(0, 0, 0);
                                                            try {
                                                                priorityRed = new Color(bimig.getRGB(m - 1, c));
                                                            } catch (ArrayIndexOutOfBoundsException err) {
                                                            }
                                                            Color[] colors = new Color[2];
                                                            colors[0] = cols[0];
                                                            colors[1] = cols[1];
                                                            percentCorrect = functions.getPercentOfAccuracy(cols, priorityRed, percentCorrect, bimig);
                                                            //percentCorrect /= 2;

                                                            //System.out.println(priorityRed);
                                                            //System.out.println(colors[0]);
                                                            //System.out.println(colors[1]);
                                                            //System.out.println(percentCorrect);

                                                            priorityRed = new Color(0, 0, 0);
                                                            try {
                                                                priorityRed = new Color(bimig.getRGB(m, c - 1));
                                                            } catch (ArrayIndexOutOfBoundsException err) {
                                                            }
                                                            colors = new Color[2];
                                                            colors[0] = cols[0];
                                                            colors[1] = cols[2];
                                                            percentCorrect = functions.getPercentOfAccuracy(cols, priorityRed, percentCorrect, bimig);
                                                            //percentCorrect /= 2;

                                                            priorityRed = new Color(0, 0, 0);
                                                            try {
                                                                priorityRed = new Color(bimig.getRGB(m, c + 1));
                                                            } catch (ArrayIndexOutOfBoundsException err) {
                                                            }
                                                            colors = new Color[2];
                                                            colors[0] = cols[0];
                                                            colors[1] = cols[4];
                                                            percentCorrect = functions.getPercentOfAccuracy(cols, priorityRed, percentCorrect, bimig);
                                                            //percentCorrect /= 2;

                                                            priorityRed = new Color(0, 0, 0);
                                                            try {
                                                                priorityRed = new Color(bimig.getRGB(m - 1, c));
                                                            } catch (ArrayIndexOutOfBoundsException err) {
                                                            }
                                                            colors = new Color[2];
                                                            colors[0] = cols[0];
                                                            colors[1] = cols[3];
                                                            percentCorrect = functions.getPercentOfAccuracy(cols, priorityRed, percentCorrect, bimig);
                                                            percentCorrect /= 8;

                                                            RenderingHints rh = new RenderingHints(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);
                                                            //rh.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                                                            comparisonGraphics.setRenderingHints(rh);

                                                            percentCorrect *= (percentCorrect*10);

                                                            if(percentCorrect>=20000&&percentCorrect<=600000000)
                                                            {
                                                                Color Cole=new Color((int)percentCorrect/10);
                                                                //System.out.println(Cole);
                                                                comparisonGraphics.setColor(new Color(Cole.getRGB()));
                                                                comparisonGraphics.fillRect(m,c,1,1);
                                                                percentCorrect2+=percentCorrect;
                                                            } else {
                                                                comparisonGraphics.setColor(new Color(255,255,255));
                                                            }
                                                            percentCorrect/=2;
                                                            //System.out.println("test:" + percentCorrect);
                                                            //operations += 1;
                                                        }
                                                    }
                                                }
                                                percentCorrect2 = 1/percentCorrect2;
                                                if (percentCorrect2 >= 0) {
                                                    //System.out.println("Final:" + (1/(percentCorrect2*percentCorrect2))/100000000);
                                                }
                                                totalPercent += percentCorrect2;
                                                totalPercent /= 2;
                                                totalPercent *= 1;
                                                //System.out.println("Total:" + totalPercent);
                                            } catch (java.awt.image.RasterFormatException err) {
                                                //System.out.println("1");
                                                stop = 1;
                                            }
                                        }
                                    }

                                } catch (java.awt.image.RasterFormatException err) {
                                    System.out.println("2");
                                }
                            }
                        }
                    }
                }
            }
        } catch (java.awt.image.RasterFormatException err) {
            System.out.println("3");
        }*/

        /*totalPercent=(1/(totalPercent*totalPercent))/100000000;
        totalPercent/=2;
        System.out.println(totalPercent);

        String num = ""+totalPercent;
        //num = num.split()[0];
        num = num.substring(num.length()-5);
        System.out.println(num);

        float number = Float.parseFloat(num);

        if(number<10);
        {
            number*=10;
        }

        if(number>=100)
        {
            number/=10;
        }

        number/=2;*/
        
        System.out.println("done");
        
        //img=null;
        
        boolean toogood = false;
        DifferenceInPxls += diff2 + DifferenceInPxls + DifferenceInPxls;
        DifferenceInPxls /= 4;
        DifferenceInPxls /= ((reader.getWidth() - 1) * (reader.getHeight() - 1));
        //DifferenceInPxls=(1-(1/DifferenceInPxls))*10;
        //DifferenceInPxls+=number;
        if (debug) {
            textArea.append("TotalRating:" + DifferenceInPxls + "\n");
        }
        DifferenceInPxls = Math.abs((DifferenceInPxls));
        if (DifferenceInPxls > 10) {
            DifferenceInPxls = (1 / DifferenceInPxls) * 1000;
            toogood = true;
        }
        if (DifferenceInPxls > 10) {
            DifferenceInPxls = (1 / DifferenceInPxls) * 100;
        }
        if (DifferenceInPxls > 10) {
            DifferenceInPxls = 10;
        }
        
        if (!decimal) {
            DifferenceInPxls = Math.round(DifferenceInPxls);
        }
        
        if (!gif) {
            if (DifferenceInPxls == 10) {
                Emote emote = botBuilt.getEmoteById(Long.parseLong("641385084611723266"));
    
                MessageAction msgAct = e.getChannel().sendMessage("I rate that image a " + emote.getAsMention() + "/" + emote.getAsMention());
    
                byte[] imageInByte = null;
                if (outputFile || Legacy) {
                    //https://www.mkyong.com/java/how-to-convert-bufferedimage-to-byte-in-java/
                    if (outputFile) {
                        BufferedImage originalImage = ratedImg;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try {
                            ImageIO.write(originalImage, "jpg", baos);
                            baos.flush();
                            imageInByte = baos.toByteArray();
                            baos.close();
                        } catch (IOException err) {
                        }
                        msgAct = msgAct.addFile(imageInByte, "RatedImage.png");
                    }
                    if (Legacy) {
                        BufferedImage originalImage = img2;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try {
                            ImageIO.write(originalImage, "jpg", baos);
                            baos.flush();
                            imageInByte = baos.toByteArray();
                            baos.close();
                        } catch (IOException err) {
                        }
                        msgAct = msgAct.addFile(imageInByte, "RatedImageLegacy.png");
                    }
                }
                Message msgRate = msgAct.complete();
            } else {
                MessageAction msgAct = e.getChannel().sendMessage("I rate that image a " + DifferenceInPxls + "/10");
    
                byte[] imageInByte = null;
                if (outputFile || Legacy) {
                    //https://www.mkyong.com/java/how-to-convert-bufferedimage-to-byte-in-java/
                    if (outputFile) {
                        BufferedImage originalImage = ratedImg;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try {
                            ImageIO.write(originalImage, "jpg", baos);
                            baos.flush();
                            imageInByte = baos.toByteArray();
                            baos.close();
                        } catch (IOException err) {
                        }
                        msgAct = msgAct.addFile(imageInByte, "RatedImage.png");
                    }
                    if (Legacy) {
                        BufferedImage originalImage = img2;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        try {
                            ImageIO.write(originalImage, "jpg", baos);
                            baos.flush();
                            imageInByte = baos.toByteArray();
                            baos.close();
                        } catch (IOException err) {
                        }
                        msgAct = msgAct.addFile(imageInByte, "RatedImageLegacy.png");
                    }
                }
                Message msgRate = msgAct.complete();
            }
        } else {
            fo = new File(fo.getPath() + "\\gif");
        }
        
        if (saveFile) {
            //File fo = folder;
            File fi = new File(fo.getAbsolutePath() + "\\rated.png");
            File fi2 = new File(fo.getAbsolutePath() + "\\original.png");
            File fi3 = new File(fo.getAbsolutePath() + "\\colors\\red.png");
            File fi4 = new File(fo.getAbsolutePath() + "\\colors\\green.png");
            File fi5 = new File(fo.getAbsolutePath() + "\\colors\\blue.png");
            File fi6 = new File(fo.getAbsolutePath() + "\\colors");
            File fi7 = new File(fo.getAbsolutePath() + "\\ratedLegacy.png");
            //fo.mkdir();
            //fo.mkdirs();
            //fi.mkdirs();
            //fi2.mkdirs();
            fi6.mkdirs();
            try {
                fi.createNewFile();
                ImageIO.write(ratedImg, "png", fi);
            } catch (IOException err) {
                System.out.println("1");
            }
            
            try {
                fi3.createNewFile();
                ImageIO.write(red, "png", fi3);
            } catch (IOException err) {
                System.out.println("1");
            }
            
            try {
                fi4.createNewFile();
                ImageIO.write(green, "png", fi4);
            } catch (IOException err) {
                System.out.println("1");
            }
            try {
                fi5.createNewFile();
                ImageIO.write(blue, "png", fi5);
            } catch (IOException err) {
                System.out.println("1");
            }
            try {
                fi7.createNewFile();
                ImageIO.write(img2, "png", fi7);
            } catch (IOException err) {
                System.out.println("1");
            }
        }
        return DifferenceInPxls;
    }
    
    public static File fold = null;
    
    public static BufferedImage rateGif(Image image, MessageReceivedEvent e) {
        float DifferenceInPxls = 0;
        
        BufferedImage reader = toBufferedImage(image);
        
        File fo = fold;
        
        BufferedImage readerCopy = toBufferedImage(image);
        img = reader;
        //BufferedImage imgCopy=readerCopy;
        //ratedImg=reader;
        
        ratedImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        BufferedImage origin = readerCopy;
        
        //reader=origin;
        
        for (int x = 0; x <= reader.getWidth() - 1; x += 1) {
            for (int y = 0; y <= reader.getHeight() - 1; y += 1) {
                Color[] cols = new Color[5];
                cols[0] = new Color(reader.getRGB(x, y));
                cols[1] = new Color(reader.getRGB(x, y));
                cols[2] = new Color(reader.getRGB(x, y));
                cols[3] = new Color(reader.getRGB(x, y));
                cols[4] = new Color(reader.getRGB(x, y));
    
                try {
                    cols[1] = new Color(reader.getRGB(x - 1, y));
                } catch (ArrayIndexOutOfBoundsException err) {
                }
                try {
                    cols[2] = new Color(reader.getRGB(x, y - 1));
                } catch (ArrayIndexOutOfBoundsException err) {
                }
                try {
                    cols[3] = new Color(reader.getRGB(x + 1, y));
                } catch (ArrayIndexOutOfBoundsException err) {
                }
                try {
                    cols[4] = new Color(reader.getRGB(x, y + 1));
                } catch (ArrayIndexOutOfBoundsException err) {
                }
    
                float CollorAvg = 0;
    
                for (int colo1 = 0; colo1 < cols.length; colo1++) {
                    for (int colo2 = colo1; colo2 < cols.length; colo2++) {
                        Color col1 = cols[colo1];
                        Color col2 = cols[colo2];
                        try {
                            if (col1.getRGB() >= col2.getRGB()) {
                                CollorAvg += (col2.getRGB() / col1.getRGB());
                            } else {
                                CollorAvg += (col1.getRGB() / col2.getRGB());
                            }
                        } catch (ArithmeticException err) {
                            CollorAvg += 1;
                        }
                        CollorAvg /= 2;
                    }
                }
    
                int ColorPixel = (int) ((1 - (1f / (CollorAvg))) * 100000);
    
                //textArea.append(ColorPixel);
                ratedImg.setRGB(x, y, ColorPixel);
                img.setRGB(x, y, ColorPixel);
                //origin.setRGB(x,y,reader.getRGB(x,y));
    
                //if(col1.getRGB()==(int)(139810.14f*600))
                //{
                //    DifferenceInPxls+=0;
                //} else {
                //float CollorAvg=((float)Math.abs(col1.getRGB()*col2.getRGB()*col3.getRGB()*col4.getRGB()*col5.getRGB()));
                DifferenceInPxls += CollorAvg;//((reader.getWidth()-1)*(reader.getHeight()-1));
                DifferenceInPxls = DifferenceInPxls;
                if (debug) {
                    textArea.append("Color:" + CollorAvg + "\n");
                    textArea.append("TotalDif:" + DifferenceInPxls + "\n");
                }
                //}
    
            }
        }
        
        //img=null;
        
        boolean toogood = false;
        DifferenceInPxls /= ((reader.getWidth() - 1) * (reader.getHeight() - 1));
        //DifferenceInPxls=(1-(1/DifferenceInPxls))*10;
        if (debug) {
            textArea.append("TotalRating:" + DifferenceInPxls + "\n");
        }
        DifferenceInPxls = Math.abs((DifferenceInPxls));
        if (DifferenceInPxls > 10) {
            DifferenceInPxls = (1 / DifferenceInPxls) * 1000;
            toogood = true;
        }
        if (DifferenceInPxls > 10) {
            DifferenceInPxls = 10;
        }
        
        if (DifferenceInPxls == 10) {
            //Emote emote = botBuilt.getEmoteById(Long.parseLong("641385084611723266"));
            //Message msgRate = e.getChannel().sendMessage("I rate that image a " + emote.getAsMention() + "/" + emote.getAsMention()).complete();
        } else {
            //Message msgRate = e.getChannel().sendMessage("I rate that image a " + DifferenceInPxls + "/10").complete();
        }
        
        GifRating += DifferenceInPxls;
        
        //textArea.append(""+GifRating+"\n");
        
        return ratedImg;
    }
    
    //https://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
    
        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
    
        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
    
        // Return the buffered image
        return bimage;
    }
    
    private BufferedImage iconToImage(Icon icon) {
        if (icon instanceof ImageIcon) {
            return toBufferedImage(((ImageIcon) icon).getImage());
        } else {
            BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
            icon.paintIcon(null, image.getGraphics(), 0, 0);
            return image;
        }
    }
}
