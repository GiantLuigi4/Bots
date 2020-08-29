package bots.rater;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class TextComponent {
    public static JTextArea textArea = new JTextArea();
    public static ArrayList<String> text = new ArrayList<String>();
    public static JScrollPane scrollPane;
    
    public static JComponent componentText = new JComponent() {
        @Override
        public void paint(Graphics g) {
            int x = 0;
            int y = 0;
            for (String line : text) {
                for (int cha = 0; cha < line.length(); cha++) {
                    g.drawString("" + line.charAt(cha), x, y);
                }
            }
        }
    };
}
