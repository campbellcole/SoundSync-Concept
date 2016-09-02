package com.tachestudios.soundsync.soundcloud;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.json.JSONObject;

import de.voidplus.soundcloud.SoundCloud;
import de.voidplus.soundcloud.Track;

public class SoundCloudHandler {

	private static SoundCloud soundcloud;
	private static String publicKey = "1a36ef537213ab9167a3a563954423bf";
	private static String privateKey = "dcc3970b63f1637ae1c2d990db269718";
	private static Thread playThread;

	public static void init() {
		soundcloud = new SoundCloud(publicKey, privateKey);
	}

	@SuppressWarnings("deprecation")
	public static void playSong(Track toPlay) {
		if (playThread != null) {
			playThread.stop();
		}
		Thread pThread = new Thread() {
			public void run() {
				testPlay(toPlay.getStreamUrl());
			}
		};
		pThread.start();
		playThread = pThread;
	}
	
	public static void playSong(String url) {
		playSong(getTrackFromUrl(url));
	}

	public static Track getTrackFromUrl(String url) {
		try {
			String getUrl = "https://api.soundcloud.com/resolve.json?url=";
			getUrl += URLEncoder.encode(url, "UTF-8");
			getUrl += "&client_id=" + publicKey;
			String s = getHTML(getUrl);
			JSONObject obj = new JSONObject(s);
			int id = obj.getInt("id");
			System.out.println(id);
			Track t = soundcloud.get("tracks/"+id);
			return t;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getHTML(String urlToRead) throws Exception {
		StringBuilder result = new StringBuilder();
		URL url = new URL(urlToRead);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		rd.close();
		return result.toString();
	}

	// TEMPORARY FUNCTIONS -- ONLY LEAVE IF USED MULTIPLE TIMES

	public static void testPlay(String url) {
		try {
			AudioInputStream in = AudioSystem.getAudioInputStream(new URL(url));
			AudioInputStream din = null;
			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
					baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
					false);
			din = AudioSystem.getAudioInputStream(decodedFormat, in);
			// Play now.
			rawplay(decodedFormat, din);
			in.close();
		} catch (Exception e) {
			// Handle exception.
		}
	}

	@SuppressWarnings("unused")
	private static void rawplay(AudioFormat targetFormat, AudioInputStream din)
			throws IOException, LineUnavailableException {
		byte[] data = new byte[4096];
		SourceDataLine line = getLine(targetFormat);
		if (line != null) {
			// Start
			line.start();
			int nBytesRead = 0, nBytesWritten = 0;
			while (nBytesRead != -1) {
				nBytesRead = din.read(data, 0, data.length);
				if (nBytesRead != -1)
					nBytesWritten = line.write(data, 0, nBytesRead);
			}
			// Stop
			line.drain();
			line.stop();
			line.close();
			din.close();
		}
	}

	private static SourceDataLine getLine(AudioFormat audioFormat)
			throws LineUnavailableException {
		SourceDataLine res = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				audioFormat);
		res = (SourceDataLine) AudioSystem.getLine(info);
		res.open(audioFormat);
		return res;
	}

}
