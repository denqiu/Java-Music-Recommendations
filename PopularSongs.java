package algorithms;

import java.util.ArrayList;

public class PopularSongs {
	private String song;
	private int[] numbers = new int[2];
	private String[] inputs = new String[2];
	private ArrayList<String> tags;
	private ArrayList<ArrayList<String>> arraylists;
	
	public PopularSongs(String song, ArrayList<ArrayList<String>> arraylists) {
		this.song = song;
		this.arraylists = arraylists;
		tags = tags(song);
		boolean split = true;
		for (int i = 0; i < 2; i++) {
			inputs[i] = (String) popular(song, true, split);
			numbers[i] = (int) popular(song, false, split);
			split = false;
		}
	}
	
	public String song() {
		return song;
	}
	
	public boolean inputs() {
		int i = -1;
		while (i < inputs.length) {
			i++; return inputs[i].equals(arraylists.get(i).get(0));
		}
		return false;
	}
	
	public boolean otherGenres() {
		for (String g : arraylists.get(3))
			return inputs[1].equals(g);
		return false;
	}
	
	public int numbers() {
		int i = numbers.length;
		while (i > -1) {
			i--; return numbers[i];
		}
		return 0;
	}
	
	public boolean inputTags() {
		for (String t : arraylists.get(2))
			return tags.toString().contains(t);
		return false;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PopularSongs) {
			PopularSongs comparePopular = (PopularSongs) o;
			return this.song.equals(comparePopular.song);
		}
		return false;
	}
	
	private ArrayList<String> tags(String song) {
		return MusicDatabase.tags(MusicDatabase.split(song));
	}
	
	private Object popular(String song, boolean isString, boolean split) {
		song = MusicDatabase.split(song)[(isString) ? (split) ? 0 : 3 : (split) ? 4 : 2].trim();
		return (isString) ? song : Integer.valueOf(song);
	}
}
