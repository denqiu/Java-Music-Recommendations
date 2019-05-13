package algorithms;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Scanner;

import algorithms.SetUp.Files;

public class MusicDatabase {
	final public static String musicDatabaseBlake = SetUp.Files.file("Music_Database_Blake"), musicDatabase = SetUp.Files.file("Music_Database"), musicDatabaseCopy = SetUp.Files.file("Music_Database_Copy");

	public static void main(String[] args) throws IOException {
		File f = new File(musicDatabase);
		ArrayList<String> songs = SetUp.Files.arraylist(f);
		Collections.sort(songs, String.CASE_INSENSITIVE_ORDER);
		String textGenre = Files.SoundCloud("txt", "Genres", false);
		new Check(songs, textGenre).checkTracks();
		f = new File(musicDatabase);
		songs = SetUp.Files.arraylist(f);
		Collections.sort(songs, String.CASE_INSENSITIVE_ORDER);
		new SetUp.Files(musicDatabaseCopy, songs, 0);
		/*FileWriter fw = new FileWriter(musicDatabase); //sorts all songs and tags and overwrites file
		for (String s : songs) {
			ArrayList<String> tags = tags(split(s));
			String getTags = ", (", comma = ", "; int i = 0;
			for (String t : tags) {
				i++;
				if (i == tags.size())
					comma = "";
				getTags += t + comma;
			}
			getTags += ")";
			String[] p = s.split(", \\(");
			s = p[0] + getTags;
			fw.write(s + "\r\n");
		}
		fw.close();*/
	}
	
	public static String[] split(String track) {
		String[] artist = track.split(" - "), song = artist[1].split(", "), split = new String[song.length+1];
		split[0] = artist[0].trim();
		int i = 1, j = 0;
		while (j < song.length) {
			split[i] = song[j].trim(); i++; j++;
		}
		return split;
	}
	
	public static ArrayList<String> tags(String[] split) {
		ArrayList<String> tags = new ArrayList<String>();
		for (int i = split.length-1; i > 0; i--) {
			tags.add(split[i]);
			if (split[i].startsWith("("))
				break;
		}
		tags.set(0, tags.get(0).replace(")", ""));
		tags.set(tags.size()-1, tags.get(tags.size()-1).replace("(", ""));
		Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
		LinkedHashSet<String> uniqueTags = new LinkedHashSet<String>(tags);
		tags.clear();
		tags.addAll(uniqueTags);
		return tags;
	}
}
