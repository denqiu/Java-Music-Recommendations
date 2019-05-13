package algorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import algorithms.SetUp.Files;

/**
 * Iterates through an entire database, 
 * sorting songs into various lists based 
 * on song data, then grabs from lists 
 * based on user input.
 * @author Dennis Qiu
 */
public class BruteForce {
	private static TreeMap<String, ArrayList<String>> artists = new TreeMap<String, ArrayList<String>>(), genres = new TreeMap<String, ArrayList<String>>(), tags = new TreeMap<String, ArrayList<String>>(), year = new TreeMap<String, ArrayList<String>>();
	private static ArrayList<String> songs = new ArrayList<String>(), recommendations = new ArrayList<String>();
	private static ArrayList<TreeMap<String, ArrayList<String>>> maps;
	private static long users = 1;
	private static String username = "User", com, textGenre;
	private static boolean isCheck = false;
	final private static boolean soundCloudUsers = true;
	
	public static void main(String[] args) throws IOException {
		File f = new File(MusicDatabase.musicDatabaseBlake);
		songs = SetUp.Files.arraylist(f);
		for (String s : songs) {
			String[] split = MusicDatabase.split(s);
			artists.putIfAbsent(split[0].trim(), new ArrayList<String>());
			genres.putIfAbsent(split[3].trim(), new ArrayList<String>());
			for (String t : MusicDatabase.tags(split))
				tags.putIfAbsent(t, new ArrayList<String>());
			year.putIfAbsent(split[2].trim(), new ArrayList<String>());
		}
		for (String s : songs) {
			String[] split = MusicDatabase.split(s);
			add(split[0].trim(), s, artists);
			add(split[3].trim(), s, genres);
			for (String t : MusicDatabase.tags(split))
				add(t, s, tags);	
			add(split[2].trim(), s, year);
		}
		deleteFile("BruteForce.txt");
		print("ARTISTS", artists);
		print("GENRES", genres);
		print("TAGS", tags);
		print("YEAR", year);
		users();
	}
	
	private static void add(String compare, String song, TreeMap<String, ArrayList<String>> arraylist) {
		for (Entry<String, ArrayList<String>> entry : arraylist.entrySet()) 
			if (compare.equals(entry.getKey())) 
				entry.getValue().add(song);	
	}
	
	public static void deleteFile(String file) {
		File f = new File(SetUp.Files.file(file));
		if (f.exists())
			f.delete();
	}
	
	private static void print(String title, TreeMap<String, ArrayList<String>> arraylist) throws IOException {
		System.out.println(title);
		new SetUp.Files("BruteForce", title) {
			@Override
			public void write() throws IOException {
				for (Entry<String, ArrayList<String>> a : arraylist.entrySet()) {
					ArrayList<String> print = new ArrayList<String>();
					print.add(a.getKey());
					for (String s : a.getValue()) 
						print.add(s);
					print.add("\t");
					print.set(0, print.get(0) + "(" + (print.size()-2) + "):");
					for (String p : print) {
						System.out.println(p);
						write(p);
					}
				}
			}
		};
	}
	
	private static String factorial(int n, int maxSize) {
		int[] result = new int[maxSize];
		result[0] = 1; 
		int size = 1;
		for (int i = 2; i <= n; i++) 
			size = multiply(i, result, size);
		StringBuffer sb = new StringBuffer();
		for (int i = size - 1; i >= 0; i--)
			sb.append(result[i]);
		return n + "! = " + sb.toString();
	}
	
	private static int multiply(int x, int[] result, int size) {
		int carry = 0; 
		for (int i = 0; i < size; i++) {
			int product = result[i] * x + carry;
			result[i] = product % 10; 
			carry = product / 10;  
		}
		while (carry != 0) {
			result[size] = carry % 10;
			carry /= 10;
			size++;
		}
		return size;
	}
	
	private static void users() throws IOException {
		final int size = tags.size(); System.out.println(factorial(size, 500) + "\n");
		ArrayList<String> allTags = new ArrayList<String>(); int t[] = new int[size];
		Iterator<String> tagKeys = tags.keySet().iterator();
		int i = 0; 
		while (tagKeys.hasNext()) {
			allTags.add(tagKeys.next()); t[i] = i+1; i++;
		}
		File r = new File("Recommendations");
		deleteFile("Combinations");
		i = 0;
		if (r.exists()) {
			StringBuilder sb = new StringBuilder();
			Scanner sc = new Scanner(r);
			while (sc.hasNextLine()) {
				String s = sc.nextLine();
				if (s.matches("[0-9]+. .*")) {
					sb.append(s + "\n");
				} else {
					if (!sb.toString().isEmpty()) {
						recommendations.add(sb.toString().trim());
						sb = new StringBuilder();
					}
				}
				if (s.matches(".*-[0-9]+ likes the following song characteristics: .*")) {
					i = s.split(",").length;
					s = s.substring(0, s.indexOf("likes"));
					s = s.replaceAll("[^0-9]+", " ");
					List<String> list = Arrays.asList(s.trim().split(" "));
					users = Long.parseLong(list.get(list.size()-1))+1; 
 				}
			}
			sc.close();
		}
		ArrayList<TreeMap<String, ArrayList<String>>> maps = new ArrayList<TreeMap<String, ArrayList<String>>>();
		maps.add(artists); maps.add(genres); maps.add(tags);
		while (i <= size) {
			combinations(t, new int[i], 0, t.length-1, 0, i, allTags, maps); i++;
		}
	}
	
	public static String combo(String c, int[] t, ArrayList<String> allTags) {
		int i, j = 0;
		String[] split = c.split(" ");
		String com = "";
		int n[] = new int[split.length];
		for (i = 0; i < split.length; i++) 
			n[i] = Integer.valueOf(split[i].trim()); 
		for (i = 0; i < n.length; i++) {
			com = (i > 0) ? com += ", " : "";
			while (j < t.length) {
				if (t[j] == n[i])
					break;						
			}
			com += allTags.get(j);
		}
		return com;
	}
	
	private static void combinations(int t[], int combo[], int start, int end, int index, int k, ArrayList<String> allTags, ArrayList<TreeMap<String, ArrayList<String>>> maps) throws IOException {
		int i = 0;
		if (index == k) {
			String c = "", com = "";
			if (k > 0) {
				while (i < k) {
					c += ((i == k-1) ? "" + combo[i] : combo[i] + " "); i++;
				}
				new SetUp.Files("Combinations", c);
				com = combo(c, t, allTags);
			}		
			if (soundCloudUsers) {
				popularSongs(com, maps);
			} else {
				for (String s : songs) {
					String[] split = MusicDatabase.split(s); 
					popularSongs(new String[] {split[1].trim(), split[0].trim(), split[3].trim(), com}, maps);
				}
			}
			return;
		}
		if (k > 0) {
			for (i = start; i <= end && end-i+1 >= k-index; i++) {
				combo[index] = t[i];
				combinations(t, combo, i+1, end, index+1, k, allTags, maps);
			}
		}
	}
	
	public static String getCom() {
		return com;
	}
	
	public static ArrayList<TreeMap<String, ArrayList<String>>> getMaps() {
		return maps;
	}
	
	private static void popularSongs(String com, ArrayList<TreeMap<String, ArrayList<String>>> maps) throws IOException {
		BruteForce.com = com; BruteForce.maps = maps;
		File f = new File(Files.SoundCloud(null, null, false));
		if (!f.exists()) 
			f.mkdir();
		textGenre = Files.SoundCloud("txt", "Genres", false);
		f = new File(textGenre);
		if (!f.exists()) 
			new SetUp.Files(textGenre, genres.keySet().iterator());
		new AddUsers(textGenre, false); //comment this line out to check all tracks collected in the SoundCloudUsers folder
		soundCloudTracks(com, maps);
	}
	
	public static void soundCloudTracks(String com, ArrayList<TreeMap<String, ArrayList<String>>> maps) throws IOException {
		File f = new File(Files.SoundCloud(null, null, false));
		if (f.exists()) {
			File[] users = Files.files(f.toString());
			int i = 0;
			if (users != null) {
				Check check = null;
				ArrayList<String> tracks = new ArrayList<String>();
				while (i < users.length) {
					if (check != null) {
						tracks = SetUp.Files.arraylist(users[i]);
						for (String t : tracks)
							soundCloudTracks(t.split(","), com, maps);
						check = null; isCheck = false; tracks.clear(); i++; 
					} else {
						username = users[i].getName().replace(".txt", "");
						tracks = SetUp.Files.arraylist(users[i]);
						check = new Check(tracks, textGenre, username);
						if (!isCheck)
							check.checkTracks();
						isCheck = true;
					}
				}
			} 		
		} 
	}

	private static void soundCloudTracks(String[] s, String com, ArrayList<TreeMap<String, ArrayList<String>>> maps) throws IOException {
		String[] track = new String[s.length+1];
		int i;
		for (i = 0; i < s.length; i++) 
			track[i] = s[i]; 
		track[i] = com;
		if (genres.containsKey(track[2]))
			popularSongs(track, maps);
	}
	
	private static void popularSongs(String[] user, ArrayList<TreeMap<String, ArrayList<String>>> maps) throws IOException {
		ArrayList<PopularSongs> popularSongs = new ArrayList<PopularSongs>();
		ArrayList<String> inputArtist = new ArrayList<String>(), inputGenre = new ArrayList<String>(), otherGenres = new ArrayList<String>(), inputTags = new ArrayList<String>(); 
		ArrayList<ArrayList<String>> arraylists = new ArrayList<ArrayList<String>>();
		arraylists.add(inputArtist); arraylists.add(inputGenre); arraylists.add(inputTags); arraylists.add(otherGenres);
		String song = user[0]; inputArtist.add(user[1]); inputGenre.add(user[2]); 
		StringBuilder p = new StringBuilder(), fav = new StringBuilder();
		for (String tag : user[3].split(","))
			inputTags.add(tag.trim());
		int i = 0;
		while (i < maps.size()) {
			for (Entry<String, ArrayList<String>> entry : maps.get(i).entrySet()) 
				for (String a : arraylists.get(i)) 
					popularSongs = addPopularSongs(a, song, arraylists, popularSongs, entry); i++;
		}
		fav.append(username + "-" + users + "'s favorite song is " + user[0] + " by " + user[1] + ", Genre is " + user[2] + "\n");
		if (!user[3].isEmpty()) 
			fav.append(username + "-" + users + " likes the following song characteristics: " + user[3] + "\n\n");
		else 
			fav.append("\n");
		fav.append("Here are some songs we think " + username + "-" + users + " will like:\n");
		Comparator<PopularSongs> sortPopular = Comparator.comparing(PopularSongs :: inputs).thenComparing(PopularSongs :: otherGenres).thenComparing(PopularSongs :: numbers).reversed();
		Collections.sort(popularSongs, sortPopular);
		otherGenres.add(inputGenre.get(0));
		popularSongs = mostPopular(popularSongs, otherGenres);
		for (i = 0; i < popularSongs.size(); i++) 
			p.append((i+1) + ". " + popularSongs.get(i).song() + "\n");
		if (recommendations.contains(p.toString())) {
			return;
		} else {
			recommendations.add(p.toString());
			System.out.println(fav.toString() + p.toString());
			new SetUp.Files("Recommendations", fav.toString().replaceAll("\n", "\r\n") + p.toString().replaceAll("\n", "\r\n"));
			users++;
		}
	}
	
	private static ArrayList<PopularSongs> addPopularSongs(String input, String song, ArrayList<ArrayList<String>> arraylists, ArrayList<PopularSongs> popularSongs, Entry<String, ArrayList<String>> entry) {
		if (entry.getKey().equalsIgnoreCase(input)) {
			for (String e : entry.getValue()) {
				String g = MusicDatabase.split(e)[3].trim();
				if (!g.equals(arraylists.get(1).get(0)))
					if (!arraylists.get(3).contains(g))
						arraylists.get(3).add(g);
				PopularSongs popular = new PopularSongs(e, arraylists);
				if (!popularSongs.contains(popular))
					if (!popular.song().contains(arraylists.get(0).get(0) + " - " + song))
						popularSongs.add(popular);
			}
		}
		return popularSongs;
	}
	
	private static ArrayList<PopularSongs> mostPopular(ArrayList<PopularSongs> popularSongs, ArrayList<String> genre) {
		TreeMap<String, ArrayList<String>> genres = new TreeMap<String, ArrayList<String>>();
		ArrayList<PopularSongs> lessPopularSongs = new ArrayList<PopularSongs>();
		for (String g : genre) 
			genres.put(g, new ArrayList<String>());
		for (PopularSongs p : popularSongs) 
			add(MusicDatabase.split(p.song())[3].trim(), p.song(), genres);
		for (Entry<String, ArrayList<String>> entry : genres.entrySet()) 
			for (int i = entry.getValue().size()/2; i < entry.getValue().size(); i++) 
				for (PopularSongs p : popularSongs) 
					if (p.song().equals(entry.getValue().get(i)))
						lessPopularSongs.add(p);
		final int setPopular = 10; //this means the user will see a list of the top 10 most popular songs
		for (PopularSongs p : lessPopularSongs)
			if (popularSongs.size() > setPopular) 
				popularSongs.remove(p);
		if (popularSongs.size() > setPopular)
			mostPopular(popularSongs, genre);
		return popularSongs;
	}
}