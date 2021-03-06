package utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DrawingImage {
	
	//<color,<<x,y>,<x,y>>>
	private final ArrayList<BiObject<Color, BiObject<BiObject<Integer, Integer>, BiObject<Integer, Integer>>>> lines = new ArrayList<>();
	//<color,<<x,y>,<x,y>>>
	private final ArrayList<BiObject<Color, BiObject<BiObject<Integer, Integer>, BiObject<Integer, Integer>>>> rects = new ArrayList<>();
	//<color,<x,y>>
	private final ArrayList<BiObject<Color, BiObject<Integer, Integer>>> dots = new ArrayList<>();
	//<color,<<x,y>,text>>
	private final ArrayList<BiObject<Color, BiObject<BiObject<Integer, Integer>, String>>> strings = new ArrayList<>();
	
	Color c = Color.BLACK;
	
	public void setColor(Color c) {
		this.c = c;
	}
	
	public void drawLine(int minX, int minY, int maxX, int maxY) {
		lines.add(new BiObject<>(
				c, new BiObject<>(new BiObject<>(
				minX, minY), new BiObject<>(
				maxX, maxY)))
		);
	}
	
	public void drawPoint(int x, int y) {
		dots.add(new BiObject<>(c, new BiObject<>(x, y)));
	}
	
	public void drawText(int x, int y, String text) {
		strings.add(new BiObject<>(c, new BiObject<>(new BiObject<>(x, y), text)));
	}
	
	public void drawRect(int minX, int minY, int maxX, int maxY) {
		rects.add(new BiObject<>(
				c, new BiObject<>(new BiObject<>(
				minX, minY), new BiObject<>(
				maxX, maxY)))
		);
	}
	
	public BufferedImage render() {
		AtomicInteger minX = new AtomicInteger();
		AtomicInteger minY = new AtomicInteger();
		AtomicInteger maxX = new AtomicInteger();
		AtomicInteger maxY = new AtomicInteger();
		lines.forEach(line -> {
			minX.set(Math.min(line.getObj2().getObj1().getObj1(), minX.get()));
			maxX.set(Math.max(line.getObj2().getObj2().getObj1(), maxX.get()));
			minY.set(Math.min(line.getObj2().getObj1().getObj2(), minY.get()));
			maxY.set(Math.max(line.getObj2().getObj2().getObj2(), maxY.get()));
		});
		rects.forEach(rect -> {
			minX.set(Math.min(rect.getObj2().getObj1().getObj1(), minX.get()));
			maxX.set(Math.max(rect.getObj2().getObj2().getObj1(), maxX.get()));
			minY.set(Math.min(rect.getObj2().getObj1().getObj2(), minY.get()));
			maxY.set(Math.max(rect.getObj2().getObj2().getObj2(), maxY.get()));
		});
		dots.forEach(dot -> {
			minX.set(Math.min(dot.getObj2().getObj1(), minX.get()));
			maxX.set(Math.max(dot.getObj2().getObj1(), maxX.get()));
			minY.set(Math.min(dot.getObj2().getObj2(), minY.get()));
			maxY.set(Math.max(dot.getObj2().getObj2(), maxY.get()));
		});
		BufferedImage image1 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_BGR);
		image1.getGraphics().setFont(Font.getFont(Font.MONOSPACED));
		FontMetrics metrics = image1.getGraphics().getFontMetrics();
		strings.forEach(str -> {
			int x = str.getObj2().getObj1().getObj1();
			int y = str.getObj2().getObj1().getObj2();
			int width = metrics.stringWidth(str.getObj2().getObj2());
			int height = metrics.getHeight();
			minX.set(Math.min(x, minX.get()));
			maxX.set(Math.max(x + width, maxX.get()));
			minY.set(Math.min(y, minY.get()));
			maxY.set(Math.max(y + height, maxY.get()));
		});
		int size = 10;
		BufferedImage image = new BufferedImage((maxX.get() - minX.get()) + size, (maxY.get() - minY.get()) + size, BufferedImage.TYPE_INT_ARGB);
		minX.getAndAdd(size);
		minY.getAndAdd(size);
		Graphics g = image.getGraphics();
		g.setFont(Font.getFont(Font.MONOSPACED));
		int offset = size / 2;
		lines.forEach(line -> {
			g.setColor(line.getObj1());
			BiObject<Integer, Integer> min = line.getObj2().getObj1();
			BiObject<Integer, Integer> max = line.getObj2().getObj2();
			g.drawLine(
					min.getObj1() + minX.get() - offset,
					min.getObj2() + minY.get() - offset,
					max.getObj1() + minX.get() - offset,
					max.getObj2() + minY.get() - offset
			);
		});
		dots.forEach(dot -> {
			g.setColor(dot.getObj1());
			BiObject<Integer, Integer> pos = dot.getObj2();
			g.fillArc(
					pos.getObj1() + minX.get() - offset,
					pos.getObj2() + minY.get() - offset,
					10, 10, 0, 360
			);
		});
		rects.forEach(rect -> {
			g.setColor(rect.getObj1());
			BiObject<Integer, Integer> min = rect.getObj2().getObj1();
			BiObject<Integer, Integer> max = rect.getObj2().getObj2();
			g.fillRect(
					min.getObj1() + minX.get() - offset,
					min.getObj2() + minY.get() - offset,
					max.getObj1() + minX.get() - offset,
					max.getObj2() + minY.get() - offset
			);
		});
		strings.forEach(string -> {
			g.setColor(string.getObj1());
			BiObject<Integer, Integer> pos = string.getObj2().getObj1();
			g.drawString(
					string.getObj2().getObj2(),
					pos.getObj1() + minX.get() - offset,
					pos.getObj2() + minY.get() - offset
			);
		});
		return image;
	}
}
