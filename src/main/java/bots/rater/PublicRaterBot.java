package bots.rater;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import utils.PropertyReader;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import javax.swing.*;
import javax.swing.Icon;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class PublicRaterBot extends ListenerAdapter {
    public static JFrame frame = new JFrame("Debug Window-output");
    public static JFrame confg = new JFrame("Confgs");
    public static JWindow frameIMG = new JWindow();
    
    public static JDA botBuilt;
    
    public static JComponent component = new JComponent() {
        @Override
        public void paint(Graphics g) {
            if (isShown && shown) {
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
                            try {
                                g.drawRect((int) (x), (int) (y), 100, 100);
                            } catch (ArithmeticException err) {
                            }
                        }
                    }
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
    
    private static final String id = "643612417695285253";
    
    public static File folder = null;
    public static File fileSave = null;
    public static File fileSave2 = null;
    public static Boolean saveFile = false;
    
    public static float GifRating = 0;
    
    public static void main(String[] a) throws LoginException {
        String grabbedToken = PropertyReader.read("bots.properties", "rbPublic");
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(grabbedToken);
        builder.setGame(Game.watching("for -rater:help"));
        PublicRaterBot bot = new PublicRaterBot();
        builder.addEventListener(bot);
        botBuilt = builder.buildAsync();
    }
    
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getJDA().getSelfUser().getId().equals(id)) {
            String msgId = event.getMessageId();
            String Chan = event.getTextChannel().getName();
            Message message = event.getChannel().getMessageById(msgId).complete();
            String messageText = message.getContentDisplay();
            if (Chan.toLowerCase().contains("bot")) {
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
                            if ((!chn.getName().startsWith("_") && !chn.getName().toLowerCase().contains("bot") && !chn.getParent().getName().startsWith("-") && !chn.getParent().getName().toLowerCase().contains("bot")) && chn.getGuild().equals(event.getGuild())) {
                                try {
                                    List<Message> msgs = chn.getHistory().retrievePast(100).complete();
                                    for (Message msg : msgs) {
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
                                                activity /= 2;
                                            messagesRead += 1f;
                                            if (debug)
                                                textArea.append("SentByUser!" + "\n");
                                        } else {
                                            messagesRead += 1f;
                                        }
                                    }
                                    if (debug)
                                        textArea.append("Finished A Message History" + "\n");
                                } catch (Throwable ignored) {
                                }
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
                    boolean emoteRate = messageText.startsWith("-rate:emote");
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
                        rateImage(ico, event);
                    } else {
                        List<Message.Attachment> attachments = event.getMessage().getAttachments();
                        for (Message.Attachment attach : attachments) {
                            //if (attach.isImage())
                            if (attach.isImage()) {
                                //if()
    
                                try {
                                    if (attach.getFileName().endsWith(".gif")) {
/*
                                    InputStream inputs = attach.getInputStream();
                                    ImageInputStream stream = ImageIO.createImageInputStream(inputs);

                                    ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
                                    reader.setInput(stream);

                                    //ImageReader gifImageReader = new GIFImageReader(new GIFImageReaderSpi());

                                    BufferedImage img = ImageIO.getImageReadersByFormatName(inputs);

                                    //gifImageReader.setInput(img);

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


                                        BufferedImage imge = rateGif(thisImg.getScaledInstance(gifImageReader.getWidth(image),gifImageReader.getHeight(image),0),event);
                                        GifRating/=1.5;

                                        if(saveFile)
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
                                    }

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
                                        rateImage(img, event);
                                    }
                                } catch (IOException err) {
                                    Message msgRate = event.getChannel().sendMessage("An error occurred, please try again.:" + err.getMessage()).complete();
                                }
                            } else {
                                Message msgRate = event.getChannel().sendMessage("That is not an image.").complete();
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void rateImage(Image image, MessageReceivedEvent e) {
        float DifferenceInPxls = 0;
        
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
            DifferenceInPxls = (1 / DifferenceInPxls) * 100;
        }
        if (DifferenceInPxls > 10) {
            DifferenceInPxls = 10;
        }
        
        if (DifferenceInPxls == 10) {
            Emote emote = botBuilt.getEmoteById(Long.parseLong("641385084611723266"));
            Message msgRate = e.getChannel().sendMessage("I rate that image a " + emote.getAsMention() + "/" + emote.getAsMention()).complete();
        } else {
            Message msgRate = e.getChannel().sendMessage("I rate that image a " + DifferenceInPxls + "/10").complete();
        }
        
        
        if (saveFile) {
            //File fo = folder;
            File fi = new File(fo.getAbsolutePath() + "\\rated.png");
            File fi2 = new File(fo.getAbsolutePath() + "\\original.png");
            //fo.mkdir();
            //fo.mkdirs();
            //fi.mkdirs();
            //fi2.mkdirs();
            try {
                fi.createNewFile();
                ImageIO.write(ratedImg, "png", fi);
            } catch (IOException err) {
                System.out.println("1");
            }
        }
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