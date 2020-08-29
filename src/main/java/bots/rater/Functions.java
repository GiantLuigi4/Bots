package bots.rater;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Functions {
    public static ArrayList<BufferedImage> bimigs;
    
    public static void getPath() throws NullPointerException {
        String dirLook = String.valueOf(System.getProperty("user.dir")) + "\\memory";
        System.out.println("Start");
        z = 0;
        bimigs = walk(dirLook, "", false);
        
        System.out.println("Total File Count: " + z + " Files");
    }
    
    protected static int z;
    
    public static ArrayList<BufferedImage> walk(String path, String preFix, boolean includePreFixInList) {
        
        File root = new File(path);
        File[] list = root.listFiles();
        ArrayList<BufferedImage> x = new ArrayList<BufferedImage>();
        ArrayList<BufferedImage> p = new ArrayList<BufferedImage>();
        ArrayList<BufferedImage> c = new ArrayList<BufferedImage>();
        
        if (!includePreFixInList) {
            System.out.println(preFix + "----------Read Dir: " + path);
        }
        
        for (File f : list) {
            if (f.isDirectory()) {
                p = walk(f.getAbsolutePath(), preFix + "|   ", includePreFixInList);
                //System.out.println( "Dir:" + f.getAbsoluteFile() );
                if (includePreFixInList) {
                    for (BufferedImage nan : p) {
                        c.add(nan);
                    }
                }
    
                for (BufferedImage nan : p) {
                    x.add(nan);
                }
    
                //p.clear();
            } else {
                if (!includePreFixInList) {
                    String absPath = f.getAbsolutePath();
                    System.out.println(preFix + "|   " + "File: \"" + f.getAbsoluteFile() + "\"");
                    if (absPath.endsWith(".png") || absPath.endsWith(".bmp") || absPath.endsWith(".jpg")) {
                        try {
                            Image img = ImageIO.read(f);
                            BufferedImage bimig = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
                            bimig.getGraphics().drawImage(img, 0, 0, null);
                            //System.out.println("b");
                            p.add(bimig);
                        } catch (IOException err) {
                        }
                    }
                }
            }
            
            z += 1;
        }
        return p;
    }
    
    public static float getPercentOfAccuracy(Color[] cols, Color priorityRed, float percentCorrect, BufferedImage bimig) {
        Color colorIgnore = new Color(0, 0, 0);
        Color colorHigh = new Color(255, 255, 255);
        Color colorMid = new Color(127, 127, 127);
        Color colorLow1 = new Color(63, 63, 63);
        Color colorLow2 = new Color(31, 31, 31);
        
        //System.out.println(cols[0]);
        //System.out.println(cols[1]);
        
        float colR = 0;
        float colB = 0;
        float colG = 0;
        
        String color1 = cols[0].toString();
        String color2 = cols[1].toString();
        
        float color1R = Integer.parseInt(color1.substring(17).split(",", 2)[0]);
        float color2R = Integer.parseInt(color2.substring(17).split(",", 2)[0]);
        float color1G = Integer.parseInt(color1.substring(17).split(",", 3)[1].substring(2));
        float color2G = Integer.parseInt(color2.substring(17).split(",", 3)[1].substring(2));
        String color1BS = (color1.substring(17).split(",", 4)[2].substring(2));
        String color2BS = (color2.substring(17).split(",", 4)[2].substring(2));
        
        float color1B = Integer.parseInt(color1BS.substring(0, color1BS.length() - 1));
        float color2B = Integer.parseInt(color2BS.substring(0, color2BS.length() - 1));

        /*System.out.println(color1R);
        System.out.println(color2R);
        System.out.println(color1G);
        System.out.println(color2G);
        System.out.println(color1B);
        System.out.println(color2B);*/
        
        try {
            colR = 255f / ((color2R + color1R) / 2f);
            colR = 1 - (1 / colR);
            //System.out.println(colR);
        } catch (ArithmeticException err) {
            colR = 0;
        }
        try {
            colG = 255f / ((color2G + color1G) / 2f);
            colG = 1 - (1 / colG);
            //System.out.println(colR);
        } catch (ArithmeticException err) {
            colG = 0;
        }
        try {
            colB = 255f / ((color2B + color1B) / 2f);
            colB = 1 - (1 / colB);
            //System.out.println(colR);
        } catch (ArithmeticException err) {
            colB = 0;
        }
        
        //System.out.println("R:"+(colR));
        //System.out.println("G:"+(colG));
        //System.out.println("B:"+(colB));
        
        if (priorityRed.getBlue() == colorIgnore.getBlue()) {
            //System.out.println("ignored");
        } else if (priorityRed.getBlue() == colorHigh.getBlue()) {
            if ((colB) >= 1) ;
            {
                percentCorrect += (bimig.getHeight() * bimig.getWidth());
                //System.out.println("high");
            }
        } else if (priorityRed.getBlue() == colorMid.getBlue()) {
            if ((colB) >= 0.75) ;
            {
                percentCorrect += (bimig.getHeight() * bimig.getWidth()) / 2;
                //System.out.println("mid");
            }
        } else if (priorityRed.getBlue() == colorLow1.getBlue()) {
            if ((colB) >= 0.5) ;
            {
                percentCorrect += (bimig.getHeight() * bimig.getWidth()) / 3;
                //System.out.println("low1");
            }
        } else if (priorityRed.getBlue() == colorLow2.getBlue()) {
            if ((colB) >= 0.125) ;
            {
                percentCorrect += (bimig.getHeight() * bimig.getWidth()) / 4;
                //System.out.println("low2");
            }
        }
        
        if (priorityRed.getGreen() == colorIgnore.getGreen()) {
        } else if (priorityRed.getGreen() == colorHigh.getGreen()) {
            if ((colG) >= 1) ;
            {
                percentCorrect += (bimig.getHeight() * bimig.getWidth());
                //System.out.println("high");
            }
        } else if (priorityRed.getGreen() == colorMid.getGreen()) {
            if ((colG) / 255 >= 0.75) ;
            {
                percentCorrect += (bimig.getHeight() * bimig.getWidth()) / 2;
                //System.out.println("mid");
            }
        } else if (priorityRed.getGreen() == colorLow1.getGreen()) {
            if ((colG) / 255 >= 0.5) ;
            {
                percentCorrect += (bimig.getHeight() * bimig.getWidth()) / 3;
                //System.out.println("low1");
            }
        } else if (priorityRed.getGreen() == colorLow2.getGreen()) {
            if ((colG) / 255 >= 0.125) ;
            {
                percentCorrect += (bimig.getHeight() * bimig.getWidth()) / 4;
                //System.out.println("low2");
            }
        }
        
        if (priorityRed.getRed() == colorIgnore.getRed()) {
        } else if (priorityRed.getRed() == colorHigh.getRed()) {
            if ((colR) >= 1) ;
            {
                //percentCorrect += (bimig.getHeight() * bimig.getWidth());
                percentCorrect += colR / 2;
                //System.out.println("high");
            }
        } else if (priorityRed.getRed() == colorMid.getRed()) {
            if ((colR) >= 0.75) ;
            {
                //percentCorrect += (bimig.getHeight() * bimig.getWidth()) / 2;
                percentCorrect += colR / 2;
                //System.out.println("mid");
            }
        } else if (priorityRed.getRed() == colorLow1.getRed()) {
            if ((colR) >= 0.5) ;
            {
                //percentCorrect += (bimig.getHeight() * bimig.getWidth()) / 3;
                percentCorrect += colR / 2;
                //System.out.println("low1");
            }
        } else if (priorityRed.getRed() == colorLow2.getRed()) {
            if ((colR) >= 0.125) ;
            {
                //percentCorrect += (bimig.getHeight() * bimig.getWidth()) / 4;
                percentCorrect += colR / 2;
                //System.out.println("low2");
            }
        }
        percentCorrect /= 2;
        return percentCorrect;
    }
}
