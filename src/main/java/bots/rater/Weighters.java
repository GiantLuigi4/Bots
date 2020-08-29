package bots.rater;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Weighters {
    public static float rateIMGLegacy(Image image, MessageReceivedEvent e, File folder, JDA botBuilt, boolean outputImg) {
        float DifferenceInPxls = 0;
    
        BufferedImage reader = RaterBot.toBufferedImage(image);
    
        File fo = folder;
        if (RaterBot.saveFile) {
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
    
        BufferedImage readerCopy = RaterBot.toBufferedImage(image);
        BufferedImage img = reader;
        //BufferedImage imgCopy=readerCopy;
        //ratedImg=reader;
    
        BufferedImage ratedImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
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
                ratedImg.getGraphics().setColor(new Color(ColorPixel));
                ratedImg.setRGB(x, y, ColorPixel);
                //img.getGraphics().setColor(new Color(ColorPixel));
                //img.getGraphics().drawRect(x,y,1,1);
    
                //origin.setRGB(x,y,reader.getRGB(x,y));
    
                //if(col1.getRGB()==(int)(139810.14f*600))
                //{
                //    DifferenceInPxls+=0;
                //} else {
                //float CollorAvg=((float)Math.abs(col1.getRGB()*col2.getRGB()*col3.getRGB()*col4.getRGB()*col5.getRGB()));
                DifferenceInPxls += CollorAvg;//((reader.getWidth()-1)*(reader.getHeight()-1));
                DifferenceInPxls = DifferenceInPxls;
                if (RaterBot.debug) {
                    RaterBot.textArea.append("Color:" + CollorAvg + "\n");
                    RaterBot.textArea.append("TotalDif:" + DifferenceInPxls + "\n");
                }
                //}
    
            }
        }
    
        //img=null;
    
        boolean toogood = false;
        DifferenceInPxls /= ((reader.getWidth() - 1) * (reader.getHeight() - 1));
        //DifferenceInPxls=(1-(1/DifferenceInPxls))*10;
        if (RaterBot.debug) RaterBot.textArea.append("TotalRating:" + DifferenceInPxls + "\n");
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
    
    
        if (RaterBot.saveFile) {
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
        return DifferenceInPxls;
    }
}
