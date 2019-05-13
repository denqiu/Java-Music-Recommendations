package algorithms;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class SetUp {
	final private static String path = "C:\\Users\\Dennis\\Downloads\\CSC 380\\AlgorithmsProject\\";
	final private static String selenium = path + "Selenium-WebDriver\\";
	
	public class Drivers {
		final private static String drivers = "Drivers\\";
		final private static String driverPath = selenium + drivers;
		final public static String chrome = driverPath + "chromedriver.exe";
		final public static String fireFox = driverPath + "geckodriver.exe";
	}
	
	/**
	 * This class contains methods for anything related to files.
	 * @author Dennis Qiu
	 */
	public static class Files {		
		final public static String splitRegex = "SIFJKSR3874dkffksdfjsdHSDSFjdkjfkwIJFIU847jdjl";
		private FileWriter fw = null;
		
		public Files(String file, String write) throws IOException {
			fw = new FileWriter(file(file), true);
			write(write); 
			write();
			fw.close();
		}
		
		public Files(String file, ArrayList<String> arraylist, int start) throws IOException {
			fw = new FileWriter(file(file));
			for (int i = start; i < arraylist.size(); i++)
				write(arraylist.get(i));
			fw.close();
		}
		
		@SuppressWarnings("unchecked")
		public Files(String file, Object list) throws IOException {
			fw = new FileWriter(file(file));
			if (list instanceof Iterator) {
				Iterator<String> iterator = (Iterator<String>) list;
				while (iterator.hasNext())
					write(iterator.next());
			} else if (list instanceof Set) {
				Set<String> set = (Set<String>) list;
				for (String s : set)
					write(s);
			}
			fw.close();
		}	
		
		public void write(String write) throws IOException {
			if (fw != null)
				fw.write(write + "\r\n");
			else
				System.out.println("Please declare FileWriter");
		}
		
		public void write() throws IOException {
		}
		
		public static String file(String fileName) {
			if (fileName.contains("C:\\")) {
				if (!fileName.endsWith(".txt"))
					fileName = fileName + ".txt";
				return fileName;
			} else {
				fileName = fileName.replace(".txt", "");
				return path + fileName + ".txt";	
			}
		}
		
		/**
		 * A String array of special characters that are not allowed when naming files.
		 * @return array of special characters not allowed when naming files.
		 */
		public static String input(String input) {
			if (input != null) {
				Character[] specials = {' ','/','?','<','>','\\','|',':','*','"'};
				String[] chars = new String[10];
				for (int i = 0; i < specials.length; i++)
					chars[i] = specials[i].toString();
				for (String s : chars)
					input = input.replace(s, "-");
				while (input.contains("--"))
					input = input.replace("--", "-");
			}
			return input;
		}
		
		public static File[] files(String folder) {
			File[] files = new File(folder).listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File f, String s) {
					String[] unaccept = {"Genres", "Playlists", "DeletedPlaylists"};
					for (String a : unaccept)
						if (s.equals(a + ".txt"))
							return false;
					return true;
				}
			});
			return files;
		}
		
		public static ArrayList<String> arraylist(Object items) {
			ArrayList<String> arraylist = new ArrayList<String>();
			if (items instanceof String) {
				try {
					String file = (String) items;
					Scanner sc = new Scanner(new File(file));
					while (sc.hasNextLine())
						arraylist.add(sc.nextLine());
					sc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (items instanceof File) {
				try {
					File file = (File) items;
					Scanner sc = new Scanner(file);
					while (sc.hasNextLine())
						arraylist.add(sc.nextLine());
					sc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (items instanceof String[]) {
				String[] s = (String[]) items;
				for (String item : s)
					arraylist.add(item);
			}
			return arraylist;
		}
		
		public static String[] toArray(ArrayList<String> arraylist) {
			return arraylist.toArray(new String[]{});
		}
		
		public static boolean contains(String region, String input) {
			char low = Character.toLowerCase(input.charAt(0)), up = Character.toUpperCase(input.charAt(0)), c;
		    for (int i = region.length() - input.length(); i >= 0; i--) {
		        c = region.charAt(i);
		        if (c != low && c != up)
		            continue;
		       return region.regionMatches(true, i, input, 0, input.length());
		    }
		    return false;
		}
		
		/**
		 * Creates user directories and filenames for Algorithms Project.
		 * @param fileType can be txt or html 
		 * @param user for making a folder containing files for this particular user
		 * @return directories or filenames created for this particular user.
		 * @throws IOException 
		 */
		public static String SoundCloud(String fileType, String user, boolean search) {
			user = input(user);
			String folder = path + "SoundCloudUsers";
			String file = folder + "\\" + user + "." + fileType;
			if (search) {
				String[] split = user.split(splitRegex);
				folder = folder.replace("Users\\" + user, split[0]);
				if (split.length == 2) 
					file = folder + "\\" + split[1] + "." + fileType;	
			}
			if (fileType == null)
				return folder;
			return file;
		}
	}
	
	public class WebDrivers {
		final public static String chrome = "webdriver.chrome.driver";
		final public static String fireFox = "webdriver.firefox.marionette";
	}
}
