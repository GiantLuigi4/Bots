package utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class GourceLikeImaging {
	
	public static void main(String[] args) {
		BufferedImage bimig = imageFiles(Files.get("bots\\convo\\"));
		try {
			File f = Files.get("test\\out_gource_like.png");
			if (!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			ImageIO.write(bimig, "png", f);
		} catch (Throwable ignored) {
		}
	}
	
	public static BufferedImage imageFiles(File file) {
		DrawingImage image = new DrawingImage();
		return image(new File[]{file}, 0, image, 0, 0).render();
	}
	
	private static DrawingImage image(File[] files, int baseRot, DrawingImage image, int x, int y) {
		int amt = 0;
		for (File f : files) {
			if (f.isDirectory()) {
				amt++;
			}
		}
		int fileCount = 0;
		int dirCount = 0;
		for (File f : files) {
			if (f.isDirectory()) {
				image.setColor(new Color(255, 255, 255));
				double xPos = Math.cos(Math.toRadians((dirCount * 45) + baseRot));
				double yPos = Math.sin(Math.toRadians((dirCount * 45) + baseRot));
				image.drawLine(
						x, y,
						(int) (x + (xPos * 20)),
						(int) (y + (yPos * 20))
				);
				image(f.listFiles(), dirCount, image,
						(int) (x + (xPos * 20)),
						(int) (y + (yPos * 20))
				);
				dirCount++;
			} else {
				int color = Math.abs(f.getName().substring(f.getName().lastIndexOf(".")).hashCode()) % 16777215;
				image.setColor(new Color(color));
				int num = 1;
				int mul = fileCount;
				int number = 0;
				while (mul > num * num * num) {
					num++;
					number = mul % num;
				}
				image.drawPoint(
						(int) (x + (Math.cos(Math.toRadians(number * 45)) * (num * 5))),
						(int) (y + (Math.sin(Math.toRadians(number * 45)) * (num * 5)))
				);
				fileCount++;
			}
		}
		return image;
	}
}
