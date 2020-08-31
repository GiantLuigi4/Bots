package bots.idle_maker;

import java.math.BigInteger;
import java.util.ArrayList;

public class Structure implements Comparable<Structure> {
    protected String name = "";
    protected String description = "";
    protected String cps = "0";
    protected int cost = 0;
    
    public Structure(String name, String description, String cps, int cost) {
        this.name = name;
        this.description = description;
        this.cps = cps;
        this.cost = cost;
    }
    
    public BigInteger getID() {
        StringBuilder number = new StringBuilder();
        
        try {
            number.append(Integer.parseInt(cps));
        } catch (Throwable err) {
            for (char c : cps.toCharArray()) {
                number.append((int) c);
            }
        }
//        number.append(cps);
        number.append(cost);
        
        //Thank you Chicken_Man370#1333, aka 400105769674211330
        String chars = "~!@#$%^&()_+/-QWERTYUIOP{}|+ASDFGHJKL:\"ZXCVBNM<>? `1234567890-=/*-qwertyuiop[]+asdfghjkl;'zxcvbnm,.";
        
        ArrayList<Character> charslist = new ArrayList<>();
        for (int i = 0; i <= chars.length() - 1; i++) {
            charslist.add(chars.charAt(i));
        }
        for (int i = 0; i <= name.length() - 1; i++) {
            if (charslist.contains(name.charAt(i)))
                number.append(charslist.indexOf(name.charAt(i)));
        }
        /*for (int i=0; i<=description.length()-1;i++)
        {
            if(charslist.contains(description.charAt(i)))
                number+=""+charslist.indexOf(description.charAt(i));
        }*/

//        System.out.println(number.toString());
        return new BigInteger(number.toString());
    }
    
    @Override
    public int compareTo(Structure o) {
        if (o != null) return this.compare(this, o);
        throw new NullPointerException();
    }
    
    //http://www.java2s.com/Code/Java/Collections-Data-Structure/UseCollectionssorttosortcustomclassanduserdefinedComparator.htm
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Structure) || !(o2 instanceof Structure))
            throw new ClassCastException();
    
        Structure e1 = (Structure) o1;
        Structure e2 = (Structure) o2;
    
        return e1.cost - e2.cost;
    }
}
