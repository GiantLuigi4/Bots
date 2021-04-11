import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.Format;
import utils.Files;

import java.io.*;
import java.net.URL;

public class DownloadVideo {
//	private static final String query = "https://www.youtube.com/ptracking";
	private static final String query = "https://www.youtube.com/embed/";
	
	public static void main(String[] args) throws IOException {
		try {
			YoutubeDownloader downloader = new YoutubeDownloader();
			YoutubeVideo video = downloader.getVideo("xKtga8vbQRY");
			video.download(video.findFormats((format)->format.type().equals(Format.AUDIO)).get(0), new File("test"));
		} catch (Throwable ignored) {
		}
		String embed = getEmbed("https://www.youtube.com/watch?v=xKtga8vbQRY").replace("\\","/");
		System.out.println(embed);
		Files.write("test/youtube.html", new String(download("https://www.youtube.com/watch?v=xKtga8vbQRY")));
		Files.write("test/embed.html", new String(download(embed)));
//		Files.write("test/video.wav", new String(download(embed)));
	}
	
	public static String getEmbed(String url) throws IOException {
		String embed = url.replace("watch?v=", "embed/"); // only works if it is a complete url (starts with "https://www."), and I'm not sure if there's a weird edge case where it winds up not working
		if (embed.startsWith(query)) {
			return embed;
		} else {
			String html = new String(download(url));
			String substring = html.substring(html.indexOf(query));
			substring = substring.substring(0, substring.indexOf("\""));
			return substring;
		}
	}
	
	private static byte[] download(String url) throws IOException {
		BufferedInputStream stream1 = new BufferedInputStream(new URL(url).openStream());
		int b;
		ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		while ((b = stream1.read()) != -1) stream2.write(b);
		stream1.close();
		return stream2.toByteArray();
	}
}
