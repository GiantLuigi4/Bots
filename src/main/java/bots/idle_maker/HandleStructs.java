package bots.idle_maker;

import bots.BunchOBots;
import net.dv8tion.jda.core.EmbedBuilder;
import utils.Files;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class HandleStructs {
    public static BigInteger getAmt(String userid, BigInteger cps) {
        Date date = new Date();
        //SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        //SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        //SimpleDateFormat formatter2 = new SimpleDateFormat("MM");
        //SimpleDateFormat formatter3 = new SimpleDateFormat("dd");
        //System.out.println(formatter.format(date));
        //System.out.println(formatter2.format(date));
        //System.out.println(formatter3.format(date));
    
        File fi = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\" + "lastupdated.txt");
        File fi2 = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid + "\\" + "totalcoins.txt");
    
        BigInteger[] data = new BigInteger[2];
        data[0] = new BigInteger("0");
        data[1] = new BigInteger("0");
    
        Long time = date.getTime() / 1000;
        //System.out.println(time);
    
        BigInteger currency = new BigInteger("0");
        BigInteger coins = new BigInteger("0");
        try {
            Scanner sc = new Scanner(fi);
            Scanner sc2 = new Scanner(fi2);
            data[0] = new BigInteger(sc.nextLine());
            data[1] = new BigInteger(sc.nextLine());
            Date date2 = new Date(data[0].longValue());
            Long time2 = date2.getTime() / 1000;
    
            coins = new BigInteger("" + Long.parseLong(sc2.nextLine()));
    
            //System.out.println(time2);
            Long seconds = (time - time2);
    
            //System.out.println(seconds);
            try {
                FileWriter righter = new FileWriter(fi);
                FileWriter righter2 = new FileWriter(fi2);
    
                currency = new BigInteger("" + (new BigInteger("" + data[1].longValue()).add(new BigInteger("" + seconds).multiply(cps))));
                
                coins = coins.add(new BigInteger("" + (new BigInteger("" + seconds).multiply(cps))));
    
                //System.out.println(date.getTime()+"\n"+currency);
                righter.write(date.getTime() + "\n" + currency);
                righter2.write("" + coins);
                righter.close();
                righter2.close();
            } catch (IOException err) {
            }
        } catch (FileNotFoundException err) {
            //System.out.println("Err:FileNotFound");
            //System.out.println("err:"+err.getMessage());
            //Runtime.getRuntime().exit(-1);
        }
        return currency;
    }
    
    public static BigInteger getCPS(String userid) {
        //File fi = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\"+userid+"\\"+"purchases.txt");
        
        ArrayList<BigInteger> counts = new ArrayList<>();
        ArrayList<BigInteger> CPS = new ArrayList<>();
        
        //System.out.println(fi);
        
        try {
            int i = 0;
            File fi = new File(BunchOBots.drive+":\\bot\\idlemk\\userdata\\" + userid);
            for (i = 0; i < IdleMkr.structures.size(); i++) {
                Structure struct = IdleMkr.structures.get(i);
                Scanner sc = new Scanner(new File(fi.getPath() + "\\purchases\\" + struct.getID() + ".txt"));
                counts.add(new BigInteger(sc.nextLine()));
                String cps = struct.cps;
                String structCPS = "";
                if (cps.contains("rand(")) {
                    String test = cps.substring(cps.indexOf("rand("));
                    test = test.substring(0, test.indexOf(")"));
                    Random random = new Random();
                    String[] strings = test.split(",");
                    int val = random.nextInt(Integer.parseInt(strings[1])) + Integer.parseInt(strings[0].replace("rand(", ""));
                    structCPS = "" + val;
                } else {
                    structCPS = cps;
                }
                CPS.add(new BigInteger(structCPS).multiply(counts.get(i)));
                sc.close();
            }
        } catch (IOException err) {
            //System.out.println("IOException");
        }
        
        BigInteger cps = new BigInteger("0");
        
        for (int i = 0; i <= CPS.size() - 1; i++) {
            cps = cps.add(CPS.get(i));
            //System.out.println(cps);
        }
        
        return cps;
    }
    
    public static ArrayList<Structure> getStructs() {
        ArrayList<Structure> structs = new ArrayList<>();
        
        File fi = new File(Files.dir+"\\bots\\idlemk\\structures");
        
        if (!fi.exists()) {
            fi.mkdirs();
        }
        
        for (File fi2 : fi.listFiles()) {
            try {
                Scanner sc = new Scanner(fi2);
                String name = "";
                String afterdot = "";
                boolean reacheddot = false;
                for (int i = 0; i <= fi2.getName().length() - 5; i++) {
                    if (("" + fi2.getName().charAt(i)).equals(".") && !reacheddot) {
                        reacheddot = true;
                    } else if (("" + fi2.getName().charAt(i)).equals(".") && reacheddot) {
                        name += "." + afterdot;
                    } else if (!reacheddot) {
                        name += fi2.getName().charAt(i);
                    } else {
                        afterdot += fi2.getName().charAt(i);
                    }
                    if (i == 0) {
                        name = name.toUpperCase();
                    }
                }
                sc.nextLine();
                String cps = (sc.nextLine());
                sc.nextLine();
                String descriptor = sc.nextLine();
                sc.nextLine();
                int baseCost = Integer.parseInt(sc.nextLine());
    
                Structure struct = new Structure(name, descriptor, "" + cps, baseCost);
                structs.add(struct);
    
                sc.close();
                //System.out.println(name);
                //System.out.println(descriptor);
                //System.out.println(cps);
                //System.out.println(baseCost);
    
            } catch (IOException err) {
            }
        }
        
        Collections.sort(structs);
        for (Structure struct : structs) {
            System.out.println("name:" + struct.name);
            System.out.println("desc:" + struct.description);
            System.out.println("cost:" + struct.cost);
            System.out.println("_cps:" + struct.cps);
            System.out.println("__id:" + struct.getID());
        }
        
        return structs;
    }
    
    public static int findStruct(String name) throws NullPointerException {
        int number = -1;
        
        for (int i = 0; i < IdleMkr.structures.size(); i++) {
            if (IdleMkr.structures.get(i).name.toLowerCase().contains(name.toLowerCase()) && number == -1) {
                number = i;
            }
        }
        
        if (number == -1) {
            throw new NullPointerException("Failed to find structure containing the char sequence:" + name);
        }
        
        return number;
    }
    
    public static EmbedBuilder buyStruct(String userid, String mention, Structure struct, File fi, File fi2, Long count, String username) {
        ArrayList<String> purchases = new ArrayList<>();
        
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(username);
        embedBuilder.setTitle("purchased");
        embedBuilder.setColor(new Color(8, 124, 250));
        
        Long countDone = 0L;
        
        fi = new File(fi.getPath() + "\\purchases\\" + struct.getID() + ".txt");
        
        try {
            Scanner sc = new Scanner(fi);
            
            String strong = sc.nextLine();
            
            sc.close();
            
            boolean canBuyMore = true;
            boolean boughtanything = false;
            BigInteger currency = HandleStructs.getAmt(userid, HandleStructs.getCPS(userid));
            
            BigInteger spent = new BigInteger("0");
            
            BigInteger num = new BigInteger(strong).add(new BigInteger("" + countDone));
            
            BigInteger costfor1 = (new BigInteger("" + struct.cost).add(num.multiply(num)).add(new BigInteger("" + struct.cost)));
            if (currency.compareTo(costfor1) == 1) {
                while (countDone <= count && canBuyMore) {
                    num = new BigInteger(strong).add(new BigInteger("" + countDone));
                    BigInteger cost = (new BigInteger("" + struct.cost).add(num.multiply(num)).add(new BigInteger("" + struct.cost)));
                    costfor1 = cost;
    
                    //System.out.println("Curr:"+currency);
                    //System.out.println("Cost:"+cost);
    
                    if (currency.compareTo(cost) == 1) {
                        boughtanything = true;
        
                        Long[] data = new Long[2];
                        data[0] = 0L;
                        data[1] = 0L;
        
                        Scanner sc2 = new Scanner(fi2);
                        while (!sc2.hasNextLine()) {
                            try {
                                Thread.sleep(100);
                                sc2.close();
                            } catch (Throwable ignored) {
                            }
                            sc2 = new Scanner(fi2);
                        }
                        if (!sc2.hasNextLine()) {
                            try {
                                embedBuilder.setAuthor("");
                                embedBuilder.setTitle("Error:");
                                embedBuilder.setColor(new Color(255, 10, 10));
                                embedBuilder.addField("Failed to buy structure, please try again.", "", true);
                                return embedBuilder;
                            } catch (Throwable ignored) {
                            }
                        }
                        try {
                            data[0] = Long.parseLong(sc2.nextLine());
                            data[1] = Long.parseLong(sc2.nextLine());
                        } catch (Throwable ignored) {
    
                        }
                        sc2.close();
        
                        currency = (currency.subtract(cost));
        
                        FileWriter righter2 = new FileWriter(fi2);
                        righter2.write(data[0] + "\n" + currency);
                        righter2.close();
        
                        spent = spent.add(cost);
                    } else {
                        canBuyMore = false;
                    }
                    countDone += 1;
                }
            }
            if (boughtanything) {
                //strung+=(Long.parseLong(strong)+countDone)+"\n";
                embedBuilder.addField("You successfully purchased:", countDone + " `" + struct.name + "`, for: " + spent + " " + IdleMkr.currencyname, true);
                strong = new BigInteger(strong).add(new BigInteger("" + countDone)).toString();
                //msg = "You successfully purchased: "+countDone+" `"+struct.name+"`, for: "+spent+" "+ bots.idle_maker.Bot.currencyname;
            } else {
                embedBuilder.setColor(new Color(133, 26, 255));
                //strung+=strong+"\n";
                embedBuilder.addField("You cannot afford a `" + struct.name + "`", "One `" + struct.name + "` costs: " + costfor1 + ". You have: " + currency + " " + IdleMkr.currencyname, true);
                //msg += "You cannot afford a `"+struct.name+"`. One `"+struct.name+"` costs: "+costfor1+". You have: "+currency+" "+ bots.idle_maker.Bot.currencyname;
            }
            
            //String msg = "";
            /*try {
                Scanner sc = new Scanner(fi);
                while (sc.hasNextLine())
                {
                    purchases.add(sc.nextLine());
                }

                while (purchases.size()<bots.idle_maker.Bot.structures.size())
                {
                    purchases.add("0");
                }

                sc.close();

                String strung = "";

                int i = 0;
                for (String strong:purchases)
                {
                    if(i==bots.idle_maker.handlestructs.findStruct(struct.name.substring(1)))
                    {
                        boolean canBuyMore = true;
                        boolean boughtanything = false;
                        BigInteger currency = bots.idle_maker.handlestructs.getamt(userid,bots.idle_maker.handlestructs.getCPS(userid));

                        BigInteger spent = new BigInteger("0");

                        BigInteger num = new BigInteger(strong).add(new BigInteger(""+countDone));

                        BigInteger costfor1 = (new BigInteger(""+struct.cost).add(num.multiply(num)).add(new BigInteger(""+struct.cost)));
                        if (currency.compareTo(costfor1)==1)
                        {
                            while (countDone<=count&&canBuyMore)
                            {
                                num = new BigInteger(strong).add(new BigInteger(""+countDone));
                                BigInteger cost = (new BigInteger(""+struct.cost).add(num.multiply(num)).add(new BigInteger(""+struct.cost)));
                                costfor1=cost;

                                //System.out.println("Curr:"+currency);
                                //System.out.println("Cost:"+cost);

                                if (currency.compareTo(cost)==1)
                                {
                                    boughtanything=true;

                                    Long[] data = new Long[2];
                                    data[0]=0L;
                                    data[1]=0L;

                                    Scanner sc2 = new Scanner(fi2);
                                    data[0] = Long.parseLong(sc2.nextLine());
                                    data[1] = Long.parseLong(sc2.nextLine());
                                    sc2.close();

                                    currency=(currency.subtract(cost));

                                    FileWriter righter2 = new FileWriter(fi2);
                                    righter2.write(data[0]+"\n"+currency);
                                    righter2.close();

                                    spent=spent.add(cost);
                                } else {
                                    canBuyMore=false;
                                }
                                countDone+=1;
                            }
                        }
                        if (boughtanything)
                        {
                            strung+=(Long.parseLong(strong)+countDone)+"\n";
                            embedBuilder.addField("You successfully purchased:",countDone+" `"+struct.name+"`, for: "+spent+" "+ bots.idle_maker.Bot.currencyname,true);
                            //msg = "You successfully purchased: "+countDone+" `"+struct.name+"`, for: "+spent+" "+ bots.idle_maker.Bot.currencyname;
                        } else {
                            embedBuilder.setColor(new Color(133, 26, 255));
                            strung+=strong+"\n";
                            embedBuilder.addField("You cannot afford a `"+struct.name,"One `"+struct.name+"` costs: "+costfor1+". You have: "+currency+" "+ bots.idle_maker.Bot.currencyname,true);
                            //msg += "You cannot afford a `"+struct.name+"`. One `"+struct.name+"` costs: "+costfor1+". You have: "+currency+" "+ bots.idle_maker.Bot.currencyname;
                        }
                    } else {
                        strung+=strong+"\n";
                    }*/
            //msg=mention+". "+msg;
            
            //} catch (IOException err)
            //{
            //msg="An error occurred: `"+err.getMessage()+"`. Please contact GiantLuigi4#6616";
            //}
            //i++;
            FileWriter righter = new FileWriter(fi);
            righter.write(strong);
            righter.close();
        } catch (IOException err) {
            embedBuilder.setColor(new Color(250, 10, 10));
            embedBuilder.addField("An error occurred:", "`" + err.getMessage() + "`", true);
            embedBuilder.addField("Please report this to:", "GiantLuigi4#6616", false);
        }
        
        
        return embedBuilder;
    }
}
