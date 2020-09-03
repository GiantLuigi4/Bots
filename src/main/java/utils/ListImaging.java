package utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ListImaging {
	public static void main(String[] args) {
		BufferedImage bimig = image("bots\\convo\\");
		try {
			File f = Files.get("test\\out_tree.png");
			if (!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			ImageIO.write(bimig, "png", f);
		} catch (Throwable ignored) {
		}
	}
	
	public static BufferedImage image(String file) {
		File f = Files.get(file);
		DrawingImage image = new DrawingImage();
		int y = 0;
		y = imageTree(f, 0, y, image);
		return image.render();
	}
	
	public static int imageTree(File f, int x, int y, DrawingImage image) {
		for (File fi : f.listFiles()) {
			if (fi.isDirectory()) {
				int color = 0;
				image.setColor(new Color(color));
			} else {
				try {
					int color = Math.abs(f.getName().substring(f.getName().lastIndexOf(".")).hashCode()) % 16777215;
					image.setColor(new Color(color));
				} catch (Throwable ignored) {
					int color = Math.abs(f.getName().hashCode()) % 16777215;
					image.setColor(new Color(color));
				}
			}
			int yPos = y + 14;
			image.drawLine(x, yPos, x, yPos + 14);
			image.drawLine(x + 1, yPos, x + 1, yPos + 14);
			image.drawLine(x + 2, yPos, x + 2, yPos + 14);
			image.drawText(x + 10, y + 12, fi.getName());
			if (fi.isDirectory()) {
				int color = 0;
				image.setColor(new Color(16777215 - color));
			} else {
				try {
					int color = Math.abs(f.getName().substring(f.getName().lastIndexOf(".")).hashCode()) % 16777215;
					image.setColor(new Color(16777215 - color));
				} catch (Throwable ignored) {
					int color = Math.abs(f.getName().hashCode()) % 16777215;
					image.setColor(new Color(16777215 - color));
				}
			}
			image.drawRect(0, y, 256, y + 14);
			y += 14;
			if (fi.isDirectory()) {
				int lastY = y;
				y = imageTree(fi, x + 10, y, image);
				if (fi.isDirectory()) {
					int color = 0;
					image.setColor(new Color(color));
				} else {
					try {
						int color = Math.abs(f.getName().substring(f.getName().lastIndexOf(".")).hashCode()) % 16777215;
						image.setColor(new Color(color));
					} catch (Throwable ignored) {
						int color = Math.abs(f.getName().hashCode()) % 16777215;
						image.setColor(new Color(color));
					}
				}
				image.drawLine(x, lastY, x, y + 14);
				image.drawLine(x + 1, lastY, x + 1, y + 14);
				image.drawLine(x + 2, lastY, x + 2, y + 14);
			}
		}
		return y;
	}
}
