package bots.idle_maker;

import java.math.BigInteger;
import java.util.ArrayList;

public class Structure implements Comparable
{
    protected String name = "";
    protected String description = "";
    protected int cps = 0;
    protected int cost = 0;

    public Structure(String name, String description, int cps, int cost) {
        this.name = name;
        this.description = description;
        this.cps = cps;
        this.cost = cost;
    }

    public BigInteger getID()
    {
        String number = "";

        number += ""+cps;
        number += ""+cost;

        String chars = "~!@#$%^&()_+/-QWERTYUIOP{}|+ASDFGHJKL:\"ZXCVBNM<>? `1234567890-=/*-qwertyuiop[]+asdfghjkl;'zxcvbnm,.";
        ArrayList<Character> charslist = new ArrayList<>();
        for (int i=0;i<=chars.length()-1;i++)
        {
            charslist.add(chars.charAt(i));
        }
        for (int i=0; i<=name.length()-1;i++)
        {
            if(charslist.contains(name.charAt(i)))
                number+=""+charslist.indexOf(name.charAt(i));
        }
        /*for (int i=0; i<=description.length()-1;i++)
        {
            if(charslist.contains(description.charAt(i)))
                number+=""+charslist.indexOf(description.charAt(i));
        }*/

        return new BigInteger(number);
    }

    public int compareTo(Object o) {
        if (!(o instanceof Structure))
            throw new ClassCastException();

        Structure e = (Structure) o;

        return this.compare(this,o);
    }

    //http://www.java2s.com/Code/Java/Collections-Data-Structure/UseCollectionssorttosortcustomclassanduserdefinedComparator.htm
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Structure) || !(o2 instanceof Structure))
            throw new ClassCastException();

        Structure e1 = (Structure) o1;
        Structure e2 = (Structure) o2;

        return (int) (e1.cost - e2.cost);
    }
}
