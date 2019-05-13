package algorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import algorithms.TextPopUp.Text;
import algorithms.SetUp.*;

/**
 * This class manages tracks that SoundCloud users liked.
 * @author Dennis Qiu
 */
public class AddUsers {
	final private static String[] answer = {"Yes", "No"};
	public static int confirm = 0;
	final static private String[] options = {"Search By Playlist Link", "Search By Playlist Name", "Difference Between Playlist Link and Playlist Name?"};		
	private static int playlistOption = 0, keepOption;
	private static boolean manage = false, search = false, emptyPlaylist = false, createDatabase = false;
	private static ArrayList<String> users = new ArrayList<String>(), checkDuplicatePlaylists = new ArrayList<String>(), allPlaylists = new ArrayList<String>(), deletedPlaylists = new ArrayList<String>();
	private static TreeMap<String, ArrayList<String[]>> tracks = new TreeMap<String, ArrayList<String[]>>();
	private static LinkedHashSet<Integer> manageStart = new LinkedHashSet<Integer>();
	private static String baseUrl = "https://soundcloud.com", userUrl = "", playlistUrl = "", user = "", isArtistCreate = "", isNameCreate = "", textGenre, checkGenre, playlists = Files.SoundCloud("txt", "Playlists", false), deleted = Files.SoundCloud("txt", "DeletedPlaylists", false);
	
	public AddUsers(String textGenre, boolean createDatabase) throws IOException {
		if (createDatabase) {
			new CreateMusicDatabase();
		} else {
			AddUsers.textGenre = textGenre; 
			searchPlaylistsOptions(false);
			addUsers();
		}
	}
	
	/**
	 * Sets up several options in finding user's playlists.
	 */
	public static void searchPlaylistsOptions(boolean search) {
		String option = (String) JOptionPane.showInputDialog(null, "Please choose an option:", "Playlist Options", JOptionPane.DEFAULT_OPTION, null, options, null);
		for (int i = 0; i < options.length; i++) {
			if (option != null) {
				if (option.equals(options[i])) {
					confirm = JOptionPane.showOptionDialog(null, "Are you sure you wish to continue?", options[i], JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
					if (confirm == JOptionPane.YES_OPTION) {
						playlistOption = i+1;
					} else {
						searchPlaylistsOptions(search);
					}
				}
			} else {
				confirm = JOptionPane.showOptionDialog(null, "Are you sure you wish to cancel?", "Options", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
				if (confirm == JOptionPane.YES_OPTION) {
					if (search) 
						return;
					else 
						System.exit(0);
				} else {
					option = "";
					searchPlaylistsOptions(search);
				}
			}
		}
		StringBuilder note = new StringBuilder("Searching through playlists by their links will always find the playlist.\n\n");
		note.append("Searching playlists by their names will not always find the playlists\nbut this will find out if the playlist has a misleading link,\n");
		note.append("i.e, playlist name: 'aaa' w / link: '" + baseUrl + "/user/sets/aa'.\n\n");
		note.append("If the playlist does have a misleading link PLEASE correct it.\n");
		note.append("Any person trying to find the playlist, besides using Selenium WebDriver,\nMAY have an issue with the playlist's misleading link as well.\n");
		if (playlistOption == 3) {
			playlistOption = 0;
			JOptionPane.showMessageDialog(null, note, "Playlist Options", JOptionPane.INFORMATION_MESSAGE);
			searchPlaylistsOptions(search);
		}
	}
	
	private static void addedUsers(ArrayList<String> addedUsers, String textAddUsers) throws IOException {
		Collections.sort(addedUsers, String.CASE_INSENSITIVE_ORDER);
		new SetUp.Files(textAddUsers, addedUsers, 0);
	}
	
	/**
	 * Sets up options in adding, editing, and removing users.
	 * @throws IOException
	 */
	private static void addUsers() throws IOException {
		JPanel getUsers = new JPanel();
		JLabel enterUser = new JLabel("Enter Users: ");
		JTextField addUser = new JTextField(15);
		String textAddUsers = "AddedUsers";
		File f = new File(SetUp.Files.file(textAddUsers));
		ArrayList<String> addedUsers = new ArrayList<String>(), alreadyAdded = new ArrayList<String>();
		ArrayList<ArrayList<String>> arraylists = new ArrayList<ArrayList<String>>();
		final Text t = Text.CONTAINS_INPUT;
		TextPopUp popup;
		if (f.exists()) {
			addedUsers = SetUp.Files.arraylist(f);
			arraylists.add(users); arraylists.add(addedUsers);
			popup = new TextPopUp(f.toString(), addUser) {
				@Override
				public void setText() {
					getUsers.validate();
					String[] split = text().getText().split(",");
					String setText = "";
					for (int i = 0; i < split.length-1; i++)
						setText += split[i].trim() + ", ";
					setText += selectedItem();
					text().setText(setText);
					/*text().setColumns(text().getText().length());*/
				}
			};
			popup.setUpdate(f.toString(), t);
		}
		getUsers.add(enterUser);
		getUsers.add(addUser);
		JPanel show = new JPanel();
		JLabel showUsers = new JLabel("Users: " + users.toString().replace("[", "").replace("]", ""));
		show.add(showUsers);
		Object[] layout = {getUsers, show};
		Object[] userOptions = {"Add Users", "Edit User", "Remove Users", "Done", "Search Users"};
		if (users.isEmpty()) {
			show.setVisible(false);
			userOptions = new Object[] {userOptions[0], userOptions[4]};
		}
		int userInput = JOptionPane.showOptionDialog(null, layout, options[playlistOption-1] + " : Add Users", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, userOptions, null);
		if (userInput != userOptions.length) {
			if (userInput == JOptionPane.YES_OPTION) {
				if (addUser.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Please enter a User", "Enter Users", JOptionPane.ERROR_MESSAGE);
				} else {
					String[] split = addUser.getText().split(",");
					for (int i = 0; i < split.length; i++) {
						String s = split[i].trim();
						if (!users.contains(s)) {
							users.add(s);
						} else {
							if (!alreadyAdded.contains(s))
								alreadyAdded.add(s);
						}
						if (!addedUsers.contains(s)) {
							addedUsers.add(s);
							addedUsers(addedUsers, textAddUsers);
						}
					}
					if (!alreadyAdded.isEmpty())
						JOptionPane.showMessageDialog(null, "You have already entered these users: " + alreadyAdded.toString().replace("[", "").replace("]", "") + ". Please enter a new User", "Enter Users", JOptionPane.ERROR_MESSAGE);
				}
			} else if (userInput == JOptionPane.NO_OPTION) {
				if (users.isEmpty()) {
					searchUsers();
				} else {
					JPanel edit = new JPanel();
					enterUser = new JLabel("Edit Which User: ");
					JComboBox<String> editUsers = new JComboBox<String>();
					for (String u : users)
						editUsers.addItem(u);
					new Edit(addUser, editUsers);
					edit.add(enterUser);
					edit.add(editUsers);
					JPanel newUser = new JPanel();
					showUsers = new JLabel("Enter new User: ");
					newUser.add(showUsers);
					newUser.add(addUser);
					layout = new Object[] {edit, newUser};
					userOptions = new Object[] {"Done", "Back"};
					popup = new TextPopUp(f.toString(), addUser);
					popup.setUpdate(f.toString(), t);
					userInput = JOptionPane.showOptionDialog(null, layout, "Edit User", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, userOptions, null);
					if (userInput == JOptionPane.YES_OPTION) {
						boolean userExists = false;
						if (!editUsers.getSelectedItem().toString().equalsIgnoreCase(addUser.getText())) {
							for (ArrayList<String> a : arraylists) {
								if (!a.contains(addUser.getText())) {
									a.set(a.indexOf(editUsers.getSelectedItem()), addUser.getText());
									addedUsers(arraylists.get(1), textAddUsers);
								} else {
									userExists = true;
								}
							}
						} else {
							userExists = true;
						}
						if (userExists)
							JOptionPane.showMessageDialog(null, "You already added this user: " + addUser.getText(), "Edit Users", JOptionPane.ERROR_MESSAGE);
					} 
				}
			} else if (userInput == JOptionPane.CANCEL_OPTION) {
				JPanel remove = new JPanel();
				enterUser = new JLabel("Remove Which Users: ");
				String[] removeArray = new String[users.size()];
				for (int i = 0; i < users.size(); i++)
					removeArray[i] = users.get(i);
				JList<String> removeUsers = SearchUsers.createJList(removeArray, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, 4, null);
				JScrollPane scroll = SearchUsers.getScroll(removeUsers, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				remove.add(enterUser);
				remove.add(scroll);
				userOptions = new Object[] {"Done", "Back"};
				userInput = JOptionPane.showOptionDialog(null, remove, "Remove User", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, userOptions, null);
				if (userInput == JOptionPane.YES_OPTION) {
					List<String> getRemove = removeUsers.getSelectedValuesList();
					for (String r : getRemove)
						for (ArrayList<String> a : arraylists)
							a.remove(a.indexOf(r));
					addedUsers(arraylists.get(1), textAddUsers);
				}
			} else if (userInput == JOptionPane.QUESTION_MESSAGE) {
				confirm = JOptionPane.showOptionDialog(null, "Are you sure you wish to continue?", showUsers.getText(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
				if (confirm == JOptionPane.YES_OPTION)
					return;
			} else if (userInput == JOptionPane.DEFAULT_OPTION){
				confirm = JOptionPane.showOptionDialog(null, "Are you sure you wish to cancel?", "Options", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
				if (confirm == JOptionPane.YES_OPTION)
					System.exit(0);
			} else {
				searchUsers();
			}
			addUsers();
		}
		setUpWebDriver();
	}
		
	/**
	 * Sets up a Web Driver.
	 * @throws IOException
	 */
	private static void setUpWebDriver() throws IOException {
		if (users.isEmpty())
			return;
		System.out.println(options[playlistOption-1] + "\nUsers: " + users.toString().replace("[", "").replace("]", ""));
		System.setProperty(WebDrivers.chrome, Drivers.chrome);
		WebDriver driver = new ChromeDriver();
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		try {
			driver.manage().window().maximize();
			driver.get(baseUrl);
			multipleAdsTabs(driver);
			mainAdTab(driver, baseUrl);
			cookies(driver, executor);
			findUsers(driver, executor);		
		}  catch (NoSuchWindowException n) {
			if (users.isEmpty())
				return;
			if (playlistUrl == "") {
				allPlaylists.clear(); checkDuplicatePlaylists.clear(); 
			}
			manageStart.clear(); 
			System.out.println("Chrome window closed unexpectedly");
			setUpWebDriver();
		} catch (WebDriverException w) {
			if (users.isEmpty())
				return;
			if (playlistUrl == "") {
				allPlaylists.clear(); checkDuplicatePlaylists.clear(); 
			}
			manageStart.clear(); 
			System.out.println("WebDriver closed unexpectedly");
			setUpWebDriver();
		}
	}

	/**
	 * Finds the url part containing the user's name.
	 * @param driver gets url to pull up the window of user's list of playlists
	 * @param executor allows click to go through successfully
	 * @throws IOException
	 */
	private static void findUsers(WebDriver driver, JavascriptExecutor executor) throws IOException {
		if (users.isEmpty()) {
			driver.close();
			return;
		}
		userUrl = "";
		user = users.get(0);
		String url = "sets";
		if (user.endsWith("_CREATE")) {
			createDatabase = true;
			user = user.replace("_CREATE", "");
			url = "albums";
			keepOption = playlistOption;
			playlistOption = 0;
		} else {
			createDatabase = false;
		}
		String usersUrlFormat = UrlFormats.formats(user);
		userUrl = baseUrl + "/" + usersUrlFormat + "/" + url;
		driver.get(userUrl);
		multipleAdsTabs(driver);
		if (mainAdTab(driver, userUrl))
			findUsers(driver, executor);
		cookies(driver, executor);
		try {
			WebElement error = driver.findElement(By.className("errorTitle"));
			if (error.isDisplayed()) {
				manage = true;
				JOptionPane.showMessageDialog(null, "This user does not exist. Please change your user.", user, JOptionPane.ERROR_MESSAGE);
				addUsers();
				System.out.println(options[playlistOption - 1] + "\nUsers: " + users.toString().replace("[", "").replace("]", ""));
				manage = false;
				findUsers(driver, executor);
			}
		} catch (NoSuchElementException n) {
			if (playlistUrl == "")
				searchPlaylists(driver, executor, 1, null);
		}
		if (emptyPlaylist)
			return; 
		eachPlaylist(driver, executor, null);
	}
	
	private static void searchUsers() throws IOException {
		confirm = JOptionPane.showOptionDialog(null, "Are you sure you wish to search users?", "Search Users", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
		if (confirm == JOptionPane.YES_OPTION) {
			String split = Files.splitRegex;
			String searchUsers = Files.SoundCloud(null, "Search Users" + split, true);
			File search = new File(searchUsers);
			if (!search.exists())
				search.mkdir(); 
			SearchUsers.setUpWebDriver(baseUrl);
		} else if (confirm == JOptionPane.NO_OPTION) {
			return;
		}
	}
	
	private static void addDeletedPlaylists(File[] check, String[] addFindings) throws IOException {
		JPanel find = new JPanel(), add = new JPanel();
		JLabel findLabel = new JLabel("Find Which Playlists: "), addLabel = new JLabel("Selected Playlists: ");
		deletedPlaylists = SetUp.Files.arraylist(check[1]);
		String[] findArray = new String[deletedPlaylists.size()];
		int i = 0;
		while (i < deletedPlaylists.size()) {
			findArray[i] = deletedPlaylists.get(i); i++;
		}
		JList<String> findPlaylists = SearchUsers.createJList(findArray, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, 8, null), getAdded = null;
		JScrollPane scroll = SearchUsers.getScroll(findPlaylists, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS), addScroll = null;
		find.add(findLabel);
		find.add(scroll);
		Object[] layout = {find, add}, buttons = {"Add", "Remove", "Ok"};
		if (addFindings != null) {
			getAdded = SearchUsers.createJList(addFindings, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, 8, null);
			addScroll = SearchUsers.getScroll(getAdded, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			add.add(addLabel);
			add.add(addScroll);
		} else {
			layout = new Object[] {layout[0]};
			buttons = new Object[] {buttons[0]};
		}
		int findings = JOptionPane.showOptionDialog(null, layout, "Add Deleted Playlists", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttons, null);
		if (findings != JOptionPane.CANCEL_OPTION) {
			List<String> getFindings; boolean options = false;
			if (findings == JOptionPane.YES_OPTION) {
				getFindings = findPlaylists.getSelectedValuesList();
				if (getAdded != null)
					for (String a : addFindings)
						getFindings.add(a);
				Collections.sort(getFindings, String.CASE_INSENSITIVE_ORDER);
				addFindings = new String[getFindings.size()];
				for (i = 0; i < getFindings.size(); i++)
					addFindings[i] = getFindings.get(i);
				allPlaylists = SetUp.Files.arraylist(check[0]);
				allPlaylists.addAll(getFindings);
				deletedPlaylists.removeAll(getFindings);
			} else if (findings == JOptionPane.NO_OPTION) {
				getFindings = getAdded.getSelectedValuesList();
				ArrayList<String> removeFindings = new ArrayList<String>();
				for (String a : addFindings)
					removeFindings.add(a);
				removeFindings.removeAll(getFindings);
				addFindings = new String[removeFindings.size()];
				for (i = 0; i < removeFindings.size(); i++)
					addFindings[i] = removeFindings.get(i);
				allPlaylists = SetUp.Files.arraylist(check[0]);
				allPlaylists.removeAll(getFindings);
				deletedPlaylists.addAll(getFindings);
			} else {
				options = true;
				JOptionPane.showMessageDialog(null, "Please choose an option.", "Options", JOptionPane.INFORMATION_MESSAGE);
			}
			Collections.sort(deletedPlaylists, String.CASE_INSENSITIVE_ORDER);
			new SetUp.Files(playlists, allPlaylists, 0);
			new SetUp.Files(deleted, deletedPlaylists, 0);
			allPlaylists.clear();
			deletedPlaylists.clear();
			if (options)
				addDeletedPlaylists(check, addFindings);
			else
				addDeletedPlaylists(check, (addFindings.length > 0) ? addFindings : null);
		}
	}
	
	/**
	 * Searches through all the playlists on the user's account.
	 * @param driver finds playlist elements
	 * @param executor scrolls down all playlists
	 * @param startPlaylist prevents the scroll function from scrolling upwards to a already visited playlist
	 * @throws IOException
	 */
	public static void searchPlaylists(WebDriver driver, JavascriptExecutor executor, int startPlaylist, String getSearchUrl) throws IOException {
		File[] check = {new File(playlists), new File(deleted)};
		if (!createDatabase) {
			if (check[1].exists()) { 
				confirm = JOptionPane.showOptionDialog(null, "Do you wish to find songs from a deleted playlist?", "Options", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
				if (confirm == JOptionPane.YES_OPTION) 
					addDeletedPlaylists(check, null);
			}	
		}
		if (!check[0].exists() || createDatabase) {
			if (createDatabase) {
				String artist = driver.findElement(By.cssSelector("h3.profileHeaderInfo__userName")).getText();
				if (artist.contains("Pro Unlimited")) {
					isArtistCreate = artist.replace("Pro Unlimited", "");
				} else {
					JOptionPane.showMessageDialog(null, "Please correct url to popular artist, " + user + ".", "Popular Artist Error", JOptionPane.ERROR_MESSAGE);
					multipleAdsTabs(driver);
					mainAdTab(driver, userUrl);
					cookies(driver, executor);
					driver.close(); 
					System.exit(0);
				}
			} else {
				if (check[1].exists()) 
					deletedPlaylists = SetUp.Files.arraylist(check[1]);
			}
			String ul = "lazyLoadingList__list";
			if (getSearchUrl != null) {
				ul = "soundList";
				checkDuplicatePlaylists.clear(); 
				allPlaylists.clear();
				tracks.clear();
				search = false; emptyPlaylist = false;
			}
			int i = 0;
			try {
				for (i = startPlaylist; i > 0; i++) {
					try {
						WebElement end = driver.findElement(By.className("paging-eof"));
						if (end.isDisplayed()) {
							WebElement container = driver.findElement(By.cssSelector("ul." + ul));
							List<WebElement> allItems = container.findElements(By.cssSelector("li.soundList__item"));
							for (i = manageStart.size()+1; i <= allItems.size(); i++) {
								if (emptyPlaylist) {
									playlistOption = 0;
									break;
								}
								getPlaylists(driver, executor, i, getSearchUrl);
								foundCorrectLink(driver, executor, i);
							}
							manageStart.clear();
							if (playlistOption == 1) {
								Collections.sort(allPlaylists);
							} else if (playlistOption == 2) {
								LinkedHashSet<String> uniquePlaylists = new LinkedHashSet<String>(checkDuplicatePlaylists);
								allPlaylists.addAll(uniquePlaylists);
								for (String p : uniquePlaylists) {
									if (allPlaylists.size() == checkDuplicatePlaylists.size()) {
										Collections.sort(allPlaylists);
										break;
									}
									for (i = 1; i < Collections.frequency(checkDuplicatePlaylists, p); i++) {
										p = p + "-" + i;
										allPlaylists.add(p);
									}
								}
							} 
							if (createDatabase) {
								playlistOption = keepOption;
							} else {
								if (!deletedPlaylists.isEmpty()) 
									allPlaylists.removeAll(deletedPlaylists);
								new SetUp.Files(playlists, allPlaylists, 0);	
							}
							return;
						}
					} catch (NoSuchElementException n) {
						getPlaylists(driver, executor, i, getSearchUrl);
						foundCorrectLink(driver, executor, i);
					}
				}
			} catch (NoSuchElementException s) {
				searchPlaylists(driver, executor, i, getSearchUrl);
			}
		} else {
			allPlaylists = SetUp.Files.arraylist(check[0]);
		}
	}
	
	private static void foundCorrectLink(WebDriver driver, JavascriptExecutor executor, int i) throws IOException {
		if (manage && i == checkDuplicatePlaylists.indexOf(allPlaylists.get(0))+1) {
			manageStart.clear();
			correctPlaylistLink(driver, executor);
			eachPlaylist(driver, executor, null);
		}
	}
		
	/**
	 * Gets the element of the playlist based on the playlist options.
	 * @param driver finds playlist elements, either link or name
	 * @param executor scrolls a playlist into view 
	 * @param i the number assigned to the playlist's li element
	 * @throws IOException 
	 */
	private static void getPlaylists(WebDriver driver, JavascriptExecutor executor, int i, String getSearchUrl) throws IOException {
		try {
			WebElement retry = driver.findElement(By.cssSelector("div.inlineError"));
			if (retry.isDisplayed()) 
				executor.executeScript("arguments[0].click();", retry);
			getPlaylists(driver, executor, i, getSearchUrl);
		} catch (NoSuchElementException n) {
			manageStart.add(i); 
			for (Integer m : manageStart) 
				i = m;
			String xPath = "/div/div/div[2]/", playlistPath = "div[1]/div/div/div[2]/a", name = "", genre = "";
			if (getSearchUrl != null)
				xPath = "/div" + xPath;
			WebElement playlist = null, keepPlaylist = null, container = null, checkEmpty;
			int end = (createDatabase) ? 5 : 1;
			for (int j = 0; j < end; j++) {
				if (j > 0) {
					if (j == 1) {
						playlistPath = playlistPath.replace("a", "span");
					} else {
						playlistPath = (j == 2) ? playlistPath.replace("2", "3") : "div[3]/div[1]/div/";
						playlistPath = (j == 2) ? playlistPath.replace("span", "div[2]/a/span") : (j == 3) ? playlistPath.concat("a") : playlistPath.concat("div/div/ul");	
					}
					keepPlaylist = playlist;
				}
				playlist = driver.findElement(By.xpath("//li[" + i + "]" + xPath + playlistPath));
				if (j == 0) {
					if (createDatabase) {
						name = playlist.findElement(By.tagName("span")).getText();
						if (name.equals(isNameCreate))
							break;
						else
							isNameCreate = name;
					}
				} else if (j > 1) {
					if (j == 2) 
						genre = playlist.getText();
					else if (j == 3) 
						playlist.click();				
					else 
						container = playlist;
					playlist = keepPlaylist;
				}
			}
			try {
				checkEmpty = driver.findElement(By.xpath("//li[" + i + "]" + xPath + "div[2]")).findElement(By.className("m-empty"));
				if (checkEmpty != null)
					return;
			} catch (NoSuchElementException e) { 
				if (playlistOption == 1) {
					allPlaylists.add(playlist.getAttribute("href"));	
				} else {
					playlist = playlist.findElement(By.tagName("span"));
					String getPlaylists = playlist.getText(), song;
					if (playlistOption == 2) {
						String playlistUrlFormat = UrlFormats.formats(getPlaylists);
						/*System.out.println("#" + i + " " + playlistUrlFormat);*/
						checkDuplicatePlaylists.add(playlistUrlFormat);
					} else if (createDatabase) {
						if (container != null) {
							List<WebElement> songs = container.findElements(By.cssSelector("li.compactTrackList__item"));
							for (WebElement s : songs) {
								song = s.findElement(By.cssSelector("div div.compactTrackListItem__content span")).getText();
								new SetUp.Files("Music_Database", isArtistCreate + " - " + song + ", " + getPlaylists + ", " + genre);
								new SetUp.Files("Music_Database_Copy", isArtistCreate + " - " + song + ", " + getPlaylists + ", " + genre);
							}
						}
					}
				}
				executor.executeScript("arguments[0].scrollIntoView();", playlist);
				multipleAdsTabs(driver);
				if (getSearchUrl != null)
					userUrl = getSearchUrl;
				if (mainAdTab(driver, userUrl))
					findUsers(driver, executor);
				cookies(driver, executor);
			}
		}
	}

	/**
	 * Goes through all playlists user has created.
	 * @param driver gets url of each playlist
	 * @param executor allows click to go through successfully
	 * @throws IOException
	 */
	public static void eachPlaylist(WebDriver driver, JavascriptExecutor executor, String getSearchUser) throws IOException {
		if (getSearchUser != null) {
			search = true;
			user = getSearchUser;
			users.clear();
			users.add(user);
			userUrl = userUrl + "/sets";
		}
		if (allPlaylists.isEmpty()) {
			playlistUrl = "";
			if (!search) {
				BruteForce.deleteFile(playlists);
				confirm = JOptionPane.showOptionDialog(null, "Are you sure you found all tracks from this user, " + user + "?", "Options", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, answer, null);
				if (confirm == JOptionPane.YES_OPTION) {
					if (createDatabase) {
						ArrayList<String> addedUsers = SetUp.Files.arraylist(new File(SetUp.Files.file("AddedUsers")));
						addedUsers.remove(user + "_CREATE");
						new SetUp.Files("AddedUsers", addedUsers, 0);
					}
					users.remove(0);
				}
				findUsers(driver, executor);
			} else {
				emptyPlaylist = true;
			}
			return;
		}
		try {
			String playlist = allPlaylists.get(0);
			if (playlistOption == 1) {
				playlistUrl = playlist;
			} else if (playlistOption == 2) {
				playlistUrl = userUrl;
				playlistUrl = playlistUrl + "/" + playlist;
			}
			/*System.out.println(allPlaylists);*/
			multipleAdsTabs(driver);
			driver.get(playlistUrl);
			if (mainAdTab(driver, playlistUrl)) 
				eachPlaylist(driver, executor, null);
			cookies(driver, executor);
			try {
				WebElement error = driver.findElement(By.className("errorTitle"));
				if (error.isDisplayed()) {
					correctPlaylistLink(driver, executor);
					eachPlaylist(driver, executor, null);
				}
			} catch (NoSuchElementException n) {
				stopPlay(driver, executor);
				searchTracks(driver, executor, 1, null);
				ArrayList<String> getPlaylists = SetUp.Files.arraylist(new File(playlists));
				new SetUp.Files(playlists, getPlaylists, 1);
				new SetUp.Files(deleted, getPlaylists.get(0));
				allPlaylists.remove(0);
			}
			eachPlaylist(driver, executor, null);
		} catch (NoSuchElementException n) {
			allPlaylists.remove(0);
			eachPlaylist(driver, executor, null);
		}
	}

	/**
	 * Finds the correct link to the playlist, either continues using it or finds a new one if user did not change it in SoundCloud.
	 * @param driver gets url of found playlist
	 * @param executor executes argument in playlist
	 * @throws IOException
	 */
	private static void correctPlaylistLink(WebDriver driver, JavascriptExecutor executor) throws IOException {
		String input = ""; String[] select = null;
		if (!manage) {
			StringBuilder error = new StringBuilder("This playlist, " + allPlaylists.get(0) + ", has a misleading link.");
			error.append("\nPLEASE ask this user, " + user + ", to change the link. ");
			error.append("\nIf this is not possible, finding the playlist's link will be necessary. Select 'Option 1' to find this playlist's link.");
			error.append("\nIf the playlist's link has been corrected, select 'Option 2'.");
			select = new String[] {"Option 1: Find " + allPlaylists.get(0) + "'s link", "Option 2: Continue using " + playlistUrl};
			input = (String) JOptionPane.showInputDialog(null, error.toString(), "Playlist Link", JOptionPane.ERROR_MESSAGE, null, select, null);
		}
		if (input != null) {
			if (manage) {
				String link = allPlaylists.get(allPlaylists.size() - 1);
				link = link.substring(link.lastIndexOf("/") + 1);
				allPlaylists.set(0, link);
				Collections.reverse(allPlaylists);
				ArrayList<String> removeHttp = new ArrayList<String>();
				for (String p : allPlaylists) {
					if (p.contains(baseUrl)) {
						removeHttp.add(p);
					} else {
						allPlaylists.removeAll(removeHttp);
						Collections.reverse(allPlaylists);
						manage = false;
						playlistOption = 2;
						return;
					}
				}
			} else if (input.equals(select[0])) {
				playlistOption = 1;
				playlistUrl = "";
				manage = true;
				findUsers(driver, executor);
			} else if (input.equals(select[1])) {
				return;
			}  
		} else {
			JOptionPane.showMessageDialog(null, "Please choose an option.", "Playlist Link Error", JOptionPane.ERROR_MESSAGE);
			correctPlaylistLink(driver, executor);
		}
	}
	
	/**
	 * Searches through all tracks in a given playlist.
	 * @param driver finds track's html characteristics
	 * @param executor executes argument to scroll down
	 * @param startTrack prevents scroll from scrolling upwards
	 * @throws IOException 
	 */
	public static void searchTracks(WebDriver driver, JavascriptExecutor executor, int startTrack, String searchPlaylistUrl) throws IOException {
		checkGenre = null;
		if (searchPlaylistUrl != null)
			tracks.clear();
		int i = 0;
		try {
			try {
				WebElement empty = driver.findElement(By.cssSelector("div.emptyNetworkPage"));
				if (empty.isDisplayed())
					return;
			} catch (NoSuchElementException n) {
				for (i = startTrack; i > 0; i++) {
					try {
						WebElement end = driver.findElement(By.className("paging-eof"));
						if (end.isDisplayed()) {
							WebElement container = driver.findElement(By.cssSelector("ul.trackList__list"));
							List<WebElement> allItems = container.findElements(By.cssSelector("li.trackList__item"));
							for (i = manageStart.size()+1; i <= allItems.size(); i++) 
								getTracks(driver, executor, i, searchPlaylistUrl);
							manageStart.clear();
							return;
						}
					} catch (NoSuchElementException s) {
						getTracks(driver, executor, i, searchPlaylistUrl);
					}
				}
			}
		} catch (NoSuchElementException e) {
			searchTracks(driver, executor, i, searchPlaylistUrl);
		}
	}
	
	/**
	 * Gets the user's track characteristics found in each playlist.
	 * @param driver finds track's html characteristics
	 * @param executor executes a scroll into view 
	 * @param i the li element of each track
	 * @throws IOException 
	 */
	private static void getTracks(WebDriver driver, JavascriptExecutor executor, int i, String searchPlaylistUrl) throws IOException  {
		String s = "", artist = "", genre = ""; WebElement song = null; boolean refresh = false;
		try {
			manageStart.add(i); 
			for (Integer m : manageStart) 
				i = m;
			s = "//li[" + i + "]/div/div[3]/a"; 
			try {
				song = driver.findElement(By.xpath(s + "[2]"));
			} catch (NoSuchElementException e) {
				song = driver.findElement(By.xpath(s));
			}
			s = song.getText();
			artist = driver.findElement(By.cssSelector("span.soundTitle__title span")).getText(); 
			genre = (checkGenre != null) ? checkGenre : driver.findElement(By.cssSelector("div.fullHero__info a span")).getText();
			if (checkGenre(s, artist, genre)) {
				multipleAdsTabs(driver);
				if (mainAdTab(driver, playlistUrl))
					eachPlaylist(driver, executor, null);
				checkGenre = (checkGenre != null) ? checkGenre : new Check(textGenre, playlistUrl).playlistGenre();
				if (checkGenre == null) {
					driver.get(playlistUrl);
					refresh = true;
				} else {
					if (!genre.equals(checkGenre))
						genre = checkGenre;
				}
			}
			if (refresh) {
				getTracks(driver, executor, i, searchPlaylistUrl);
			} else {
				addTracks(s, artist, genre, null);
				WebDriverWait wait = new WebDriverWait(driver, 30);
				wait.until(ExpectedConditions.visibilityOf(song));
				executor.executeScript("arguments[0].scrollIntoView();", song);
				multipleAdsTabs(driver);
				if (searchPlaylistUrl != null) 
					playlistUrl = searchPlaylistUrl;
				if (mainAdTab(driver, playlistUrl))
					eachPlaylist(driver, executor, null);
				cookies(driver, executor);
			}
		} catch (NoSuchElementException n) {
			return;
		}
	}
	
	private static boolean checkGenre(String song, String artist, String genre) {
		boolean isEmpty = genre.isEmpty(), hipHop = (artist.contains("Hip Hop") && !genre.equals("Hip Hop")), world = genre.equals("World"), 
				blackViolin = (artist.contains("Violin") && !genre.equals("Classical"));
		for (boolean b : new boolean[] {isEmpty, hipHop, world, blackViolin})
			if (b)
				return b;
		return false;
	}
	
	/**
	 * Goes through all tracks inside each playlist if the genre has not been found.
	 * @param driver gets url of each track
	 * @param executor allows click to go through successfully
	 * @throws IOException
	 */
	/*public static void eachTrack(WebDriver driver, JavascriptExecutor executor) throws IOException {
		if (searchTracks.isEmpty())
			return;
		String track = searchTracks.get(0)[0], song, artist, genre, playlist;
		multipleAdsTabs(driver);
		driver.get(track);
		if (mainAdTab(driver, track)) 
			eachTrack(driver, executor);
		stopPlay(driver, executor);
		cookies(driver, executor);
		song = driver.findElement(By.cssSelector("span.soundTitle__title span")).getText(); 
		artist = driver.findElement(By.cssSelector("a.soundTitle__username")).getText();
		genre = driver.findElement(By.cssSelector("div.fullHero__info a span")).getText();
		playlist = driver.findElement(By.cssSelector("div.inPlaylist__body span")).getText();
		if (genre.contains("&")) {
			String[] genres = genre.split("&");
			for (String g : genres)
				addTracks(song, artist, g.trim(), playlist);
		} else {
			addTracks(song, artist, genre, playlist);
		}
		searchTracks.remove(0);
		eachTrack(driver, executor);
	}*/

	private static void addTracks(String s, String artist, String genre, String playlist) throws IOException {
		tracks.putIfAbsent(user, new ArrayList<String[]>());
		ArrayList<String[]> userTracks = tracks.get(user);
		String[] track = check(s, artist, genre, playlist);
		if (!userTracks.contains(track)) {
			userTracks.add(track);
			new SetUp.Files(Files.SoundCloud("txt", user, false), track[0] + ", " + track[1] + ", " + track[2]);
		}
	}
	
	private static String[] check(String song, String artist, String genre, String playlist) {
		String[] oldArtist = {"anime_ost", "BrokenHopes"};
		String[] newArtist = {"Taro Umebayashi", "Team Grimoire"};
		for (int i = 0; i < oldArtist.length; i++) 
			artist = (artist.equals(oldArtist[i])) ? newArtist[i] : artist;				
		return new String[] {song, artist, genre};
	}
	
	/**
	 * Prevents ads popping up in new tabs from breaking code.
	 * @param driver gets window of ad
	 */
	public static void multipleAdsTabs(WebDriver driver) {
		String mainWindow = driver.getWindowHandle();
		Set<String> s = driver.getWindowHandles();
		Iterator<String> i = s.iterator();
		while (i.hasNext()) {
			String adTabs = i.next();
			if (!mainWindow.equalsIgnoreCase(adTabs)) {
				driver.switchTo().window(adTabs);
				driver.close();
			}
		}
		driver.switchTo().window(mainWindow);
	}
	
	/**
	 * Brings back url of current SoundCloud Window if the window suddenly becomes an ad tab.
	 * @param driver compares current url of tab against recorded url and gets url if true
	 * @param url can either be baseUrl, userUrl, or playlistUrl
	 * @return true if ad appears, otherwise false
	 */
	public static boolean mainAdTab(WebDriver driver, String url) {
		if (!driver.getCurrentUrl().equals(url)) {
			manageStart.clear();
			driver.get(url);
			return true;
		}
		return false;
	}
	
	/**
	 * Clicks the cookie button.
	 * @param driver finds element of cookie
	 * @param executor allows click to go through successfully
	 */
	public static void cookies(WebDriver driver, JavascriptExecutor executor) {
		try {
			WebElement cookie = driver.findElement(By.className("announcement__ack"));
			executor.executeScript("arguments[0].click();", cookie);
		} catch (NoSuchElementException n) {
			return;
		}
	}

	/**
	 * Stops the music playing while looking for downloadable tracks inside a playlist.
	 * @param driver finds element of play button
	 * @param executor allows click to go through successfully
	 */
	public static void stopPlay(WebDriver driver, JavascriptExecutor executor) {
		WebElement play = driver.findElement(By.className("playControl"));
		executor.executeScript("arguments[0].click();", play);
	}
}
