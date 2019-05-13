package algorithms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import algorithms.TextPopUp;
import algorithms.SetUp.*;
import algorithms.TextPrompt.Show;

public class SearchUsers extends JFrame implements ActionListener, DocumentListener, KeyListener, FocusListener {
	private static final long serialVersionUID = 1L;
	private static ArrayList<String> getSelections = new ArrayList<String>();
	private static ArrayList<String> getGenres = new ArrayList<String>();
	private static ArrayList<String> getSearch = new ArrayList<String>();
	private static ArrayList<Object> selectedSelections = new ArrayList<Object>();
	private static ArrayList<JComboBox<Object>> comboBoxes = new ArrayList<JComboBox<Object>>();
	private static TreeMap<Integer, String> playlistUrls = new TreeMap<Integer, String>();
	private static TreeMap<Integer, String> userUrls = new TreeMap<Integer, String>();
	private static JPanel search = new JPanel(), selects = new JPanel(), searchSelections = new JPanel();
	private static JLabel searchUser = new JLabel("What would you like to search? ");
	private static JLabel selectLabel = new JLabel("Selections: "), genreLabel = new JLabel("Genres: ");
	private static JLabel searchPlaylistLabel = new JLabel("Playlists: "), searchUserLabel = new JLabel("Users: ");
	private static JTextField enterSearch = new JTextField(25);
	private static TextPrompt textPrompt = new TextPrompt("Search for artists, bands, tracks, podcasts", enterSearch);
	private static JComboBox<Object> selections = new JComboBox<Object>(), genres = new JComboBox<Object>();
	private static JComboBox<Object> ifEmpty = new JComboBox<Object>();
	private static JComboBox<Object> getSearchPlaylist = new JComboBox<Object>(), getSearchUser = new JComboBox<Object>();
	private static JButton searchButton = new JButton("Search");
	private static JButton reset = new JButton("Reset");
	private static JButton refresh = new JButton("Refresh");
	private static JButton exit = new JButton("Exit");
	private static JButton tracks = new JButton("Search Tracks");
	private static JButton home = new JButton("Home");
	final private static String[] isEmpty = { "Find Selections", "Find Genres", "Find Trending" };
	private static WebDriver driver;
	private static JavascriptExecutor executor;
	private static WebDriverWait wait;
	private static SearchUsers searchUsers = new SearchUsers();
	private static Object selectedSelection = "", selectedUser = "";
	private static int selectedPlaylist = -1, selectedComboBox = -1;
	private static String getSearchText = "", baseUrl = "", homeUrl = "";
	final private static String splitRegex = Files.splitRegex;
	final private static String searchRegex = "Search Users" + splitRegex;
	final private static String searchMenuRegex = searchRegex + "Search Menu" + splitRegex;
	final private static String textSelections = Files.SoundCloud("txt", searchRegex + "Selections", true);
	final private static String textFolder = Files.SoundCloud(null, "Search Users" + splitRegex, true);
	final private static File[] textFiles = Files.files(textFolder);
	private static String textSearchMenu = "";
	private static boolean isSearchSelection = false, isSelectedComboBox = false, searchTracks = false, manage = false, isSearch = false;
	
	private SearchUsers() {
		enterSearch.getDocument().addDocumentListener(this);
		enterSearch.addKeyListener(this);
		Component[] focuses = new Component[] { enterSearch, selections, genres, ifEmpty, getSearchPlaylist,
				getSearchUser, searchButton, reset, tracks };
		JButton[] actions = new JButton[] { searchButton, refresh, exit, home };
		for (Component f : focuses)
			f.addFocusListener(this);
		for (JButton a : actions)
			a.addActionListener(this);
		enterSearch.addActionListener(this);
		enterSearch.addKeyListener(new SearchMenu());
		getContentPane().setLayout(new FlowLayout());
		getContentPane().add(enterSearch, BorderLayout.NORTH);
		pack();
	}
	
	private class SearchMenu extends KeyAdapter {
		@Override
		public void keyReleased(KeyEvent arg0) {
			menu();
		}

		private void menu() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					searchMenu();
				}
			});
		}
	}

	private static void interact() throws IOException {
		try {
			WebElement error = driver.findElement(By.cssSelector("div.inlineError"));
			WebElement retry = error.findElement(By.cssSelector("a.sc-button"));
			executor.executeScript("arguments[0].click();", retry);
		} catch (NoSuchElementException n) {
			Scanner sc = null;
			File textSelect = new File(textSelections);
			if (textSelect.exists()) {
				sc = new Scanner(textSelect);
				while (sc.hasNextLine())
					getSelections.add(sc.nextLine());
				sc.close();
			}
			for (String s : getSelections)
				selections.addItem(s);
			for (String g : getGenres)
				genres.addItem(g);
			for (String e : isEmpty)
				ifEmpty.addItem(e);
			search.add(searchUser);
			search.add(enterSearch);
			selects.add(selectLabel);
			selects.add(selections);
			selects.add(genreLabel);
			selects.add(genres);
			selects.add(ifEmpty);
			Object[] layout = new Object[] { search, selects, searchSelections };
			int i = 0;
			if (isSearch) {
				layout = new Object[] { layout[0] };
			} else {
				String textSearch = Files.SoundCloud("txt", searchRegex + selectedSelection, true);
				File getTextSearch = new File(textSearch);
				if (getTextSearch.exists() || !enterSearch.isEnabled()) {
					isSearchSelection = true;
					sc = new Scanner(getTextSearch);
					while (sc.hasNextLine())
						getSearch.add(sc.nextLine());
					sc.close();
					Collections.sort(getSearch, String.CASE_INSENSITIVE_ORDER);
					String[] s = null;
					sortSearch(i, s, getSearchPlaylist, playlistUrls);
					for (String search : getSearch) {
						s = search.split(splitRegex);
						String users = "";
						for (i = 1; i < s.length; i++)
							users += s[i] + splitRegex;
						getSearch.set(getSearch.indexOf(search), users);
					}
					Collections.sort(getSearch, String.CASE_INSENSITIVE_ORDER);
					sortSearch(i, s, getSearchUser, userUrls);
					searchSelections.add(searchPlaylistLabel);
					searchSelections.add(getSearchPlaylist);
					searchSelections.add(searchUserLabel);
					LinkedHashSet<String> uniqueUsers = new LinkedHashSet<String>();
					for (i = 0; i < getSearchUser.getItemCount(); i++)
						uniqueUsers.add(getSearchUser.getItemAt(i).toString());
					getSearchUser.removeAllItems();
					for (String u : uniqueUsers)
						getSearchUser.addItem(u);
					String soundCloudUrl = homeUrl.replace("discover", "");
					String append = "";
					for (String u : uniqueUsers) {
						i = 0;
						String user = UrlFormats.formats(u);
						String userUrl = soundCloudUrl + user;
						for (String urls : userUrls.values()) {
							if (i > 0)
								append = "-" + i;
							if (urls.equals(userUrl + append)) {
								i++;
								if (i > 1) {
									for (Map.Entry<Integer, String> entry : userUrls.entrySet()) {
										if (entry.getValue().equals(userUrl + "-" + (i - 1))) {
											int index = entry.getKey();
											getSearchUser.insertItemAt(u, index);
										}
									}
								}
							}
						}
					}
					searchSelections.add(getSearchUser);
				} else {
					isSearchSelection = false;
					layout = new Object[] { layout[0], layout[1] };
					if (!getSearchPlaylist.isEnabled() && !getSearchUser.isEnabled()) {
						getSearchPlaylist.setEnabled(true);
						getSearchUser.setEnabled(true);
					}
				}
			}
			checkEmpty(getSelections, selections, selectLabel, 0);
			checkEmpty(getGenres, genres, genreLabel, 1);
			JButton[] buttons = { searchButton, reset, refresh, exit };
			buttons[0].setEnabled(!enterSearch.getText().isEmpty());
			buttons[1].setEnabled(false);
			selectLabel.setEnabled(enterSearch.getText().isEmpty());
			if (textFiles == null)
				refresh.setVisible(false);
			else
				refresh.setVisible(true);
			checkAds();
			if (isSearchSelection)
				selections.setEnabled(false);
			else
				selections.setEnabled(true);
			findSelectedItems(false);
			if (isSelectedComboBox) {
				if (!comboBoxes.get(selectedComboBox).isEnabled())
					comboBoxes.get(selectedComboBox).setEnabled(true);
				if (selectedComboBox >= 3) {
					if (!getSearchPlaylist.isEnabled())
						getSearchPlaylist.setEnabled(true);
				} else {
					if (selectedComboBox == 3)
						getSearchPlaylist.setEnabled(false);
					else if (selectedComboBox == 4)
						getSearchUser.setEnabled(false);
				}
				for (i = 0; i < comboBoxes.size(); i++)
					if (i != selectedComboBox)
						comboBoxes.get(i).setEnabled(false);
				isSelectedComboBox = false;
			}
			if (selectedComboBox == 3 && !selections.isEnabled() && getSearchPlaylist.isEnabled()
					&& getSearchUser.isEnabled())
				selections.setEnabled(true);
			if (getSearchText != "")
				enterSearch.setText(getSearchText);
			urls(playlistUrls, null);
			urls(userUrls, null);
			if (searchTracks)
				buttons = new JButton[] { tracks, buttons[0], buttons[1], home };
			textPrompt.setShow(Show.ALWAYS);
			if (isSearch) {
				buttons = new JButton[] { buttons[0], home };
				buttons[0].setEnabled(true);
			}
			checkAds();
			int interact = JOptionPane.showOptionDialog(searchUsers, layout, "Search Users", JOptionPane.YES_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, buttons, null);
			checkAds();
			if (interact == JOptionPane.DEFAULT_OPTION) {
				clear();
				interact();
			}
		}
	}

	public static void setUpWebDriver(String getBase) throws IOException {
		System.setProperty(WebDrivers.chrome, Drivers.chrome);
		driver = new ChromeDriver();
		executor = (JavascriptExecutor) driver;
		wait = new WebDriverWait(driver, 30);
		try {
			driver.manage().window().maximize();
			baseUrl = getBase + "/discover";
			homeUrl = baseUrl;
			driver.get(baseUrl);
			checkAds();
			interact();
		} catch (NoSuchWindowException n) {
			System.out.println("Chrome window closed unexpectedly");
			setUpWebDriver(getBase);
		} catch (WebDriverException w) {
			if (!manage) {
				System.out.println("WebDriver closed unexpectedly");
				setUpWebDriver(getBase);
			} else {
				manage = false;
			}
		}
	}

	private static void clear() {
		selections.removeAllItems();
		genres.removeAllItems();
		ifEmpty.removeAllItems();
		getSearchPlaylist.removeAllItems();
		getSearchUser.removeAllItems();
		getSelections.clear();
		getGenres.clear();
		getSearch.clear();
		playlistUrls.clear();
		userUrls.clear();
	}

	private static void checkAds() {
		AddUsers.multipleAdsTabs(driver);
		AddUsers.mainAdTab(driver, baseUrl);
		AddUsers.cookies(driver, executor);
	}

	private static void sortSearch(int i, String[] s, JComboBox<Object> comboBox, TreeMap<Integer, String> urls) {
		for (i = 0; i <= getSearch.size() - 1; i++) {
			s = getSearch.get(i).split(splitRegex);
			comboBox.addItem(item(s[0]));
			urls.put(i, s[2]);
		}
	}

	private static Object item(String item) {
		return new Object() {
			public String toString() {
				return item;
			}
		};
	}

	private static void urls(TreeMap<Integer, String> urls, JComboBox<Object> comboBox) {
		if (comboBox == null) {
			for (String u : urls.values()) {
				if (baseUrl.equals(u)) {
					searchTracks = true;
					break;
				}
			}
		} else {
			if (!tracks.isEnabled())
				tracks.setEnabled(true);
			if (urls.isEmpty())
				return;
			int index = comboBox.getSelectedIndex();
			if (index > -1) {
				if (!selections.isEnabled() || !genres.isEnabled() || !ifEmpty.isEnabled()) {
					baseUrl = urls.get(index);
					driver.get(baseUrl);
				}
			}
		}
	}

	private static void checkEmpty(ArrayList<String> arrayList, JComboBox<Object> comboBox, JLabel label, int index) {
		if (arrayList.isEmpty()) {
			comboBox.setVisible(false);
			label.setVisible(false);
		} else {
			ifEmpty.removeItemAt(index);
			comboBox.setVisible(true);
			comboBox.setEnabled(true);
			label.setVisible(true);
			label.setEnabled(true);
		}
	}
	
	@Override
	public void insertUpdate(DocumentEvent d) {
		search();
	}

	@Override
	public void removeUpdate(DocumentEvent d) {
		search();
	}

	private void search() {
		if (enterSearch.getText().trim().isEmpty()) {
			ifEmpty.setEnabled(true);
			setSelections(true);
			searchButton.setEnabled(false);
		} else {
			ifEmpty.setEnabled(false);
			setSelections(false);
			searchButton.setEnabled(true);
			reset.setEnabled(false);
		}
	}
	
	@Override
	public void keyPressed(KeyEvent k) {
		WebElement enterText = driver.findElement(By.name("q"));
		if (k.getKeyCode() == KeyEvent.VK_LEFT) 
			enterText.sendKeys(Keys.ARROW_LEFT);
		else if (k.getKeyCode() == KeyEvent.VK_RIGHT) 
			enterText.sendKeys(Keys.ARROW_RIGHT);
	}

	@Override
	public void keyReleased(KeyEvent k) {	
		if (k.getKeyCode() == KeyEvent.VK_ENTER) 
			searchButton.doClick();
	}

	@Override
	public void keyTyped(KeyEvent k) {
		if (k.getSource() == enterSearch) {
			checkAds();
			Character typedChar = k.getKeyChar();
			WebElement enterText = driver.findElement(By.name("q"));
			if (k.getKeyCode() == KeyEvent.VK_BACK_SPACE)
				enterText.sendKeys(Keys.BACK_SPACE);
			else 
				enterText.sendKeys(typedChar.toString());
		}
	}
	
	private void searchMenu() {// use katalon to find element
		String searchMenuFolder = Files.SoundCloud(null, searchMenuRegex, true);
		File folder = new File(searchMenuFolder);
		if (!folder.exists())
			folder.mkdir();
		textSearchMenu = Files.SoundCloud("txt", searchMenuRegex + enterSearch.getText(), true);
		File f = new File(textSearchMenu);
		try {
			System.out.println("============");
			WebElement container = driver.findElement(By.cssSelector("ul.lazyLoadingList__list"));
			List<WebElement> allItems = container.findElements(By.tagName("li"));
			WebElement searchFor = driver.findElement(By.cssSelector("div.searchMenu__searchFor a"));
			if (!f.exists()) {
				FileWriter fw = new FileWriter(textSearchMenu);
				if (searchFor.getText().split("Search for ")[1].equals(enterSearch.getText())) {
					fw.write(searchFor.getText() + "\r\n");
					System.out.println(searchFor.getText());
				} else {
					searchMenu();
				}
				int i = 1, last = allItems.size();
				while (i <= last) {
					try {
						WebElement menuList = driver.findElement(By.cssSelector("li:nth-child(" + i + ") div.autocompleteItem a"));
						fw.write(menuList.getText() + "\r\n");
						System.out.println(menuList.getText());
						i++;
					} catch (StaleElementReferenceException s) {
					}
				}
				fw.close();
				searchMenuList();
			} else {
				Scanner sc = new Scanner(f);
				while (sc.hasNextLine())
					if (!sc.nextLine().split("Search for ")[1].equals(f.getName()))
						f.delete();
				sc.close();
			}
		} catch (NoSuchElementException n) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void searchMenuList() {
		TextPopUp popup = new TextPopUp(textSearchMenu, enterSearch) {
			@Override
			public void set() {
				WebElement enterText = driver.findElement(By.name("q"));
				String getText = text().getText();
            	for (int i = 0; i < getText.length(); i++)
            		enterText.sendKeys(Keys.BACK_SPACE.toString());
            	enterText.sendKeys(text().getText());
			}
		};
		popup.setUpdate(Files.SoundCloud("txt", searchMenuRegex + popup.text().getText(), true));
	}

	@Override
	public void focusGained(FocusEvent f) {
		if (ifEmpty.hasFocus()) {
			enterSearch.setEnabled(false);
			setSelections(false);
			setButtons(true);
		} else if (selections.hasFocus()) {
			enterSearch.setEnabled(false);
			ifEmpty.setEnabled(false);
			genres.setEnabled(false);
			genreLabel.setEnabled(false);
			boolean checkSearch = getSearchPlaylist.getSelectedItem() != null
					&& getSearchUser.getSelectedItem() != null;
			setButtons(true);
			if (manage && checkSearch) {
				manage = false;
				setSearch(false);
			}
		} else if (genres.hasFocus()) {
			enterSearch.setEnabled(false);
			ifEmpty.setEnabled(false);
			selections.setEnabled(false);
			selectLabel.setEnabled(false);
			setButtons(true);
		} else if (getSearchPlaylist.hasFocus()) {
			enterSearch.setEnabled(false);
			ifEmpty.setEnabled(false);
			selections.setEnabled(false);
			genres.setEnabled(false);
			getSearchUser.setEnabled(false);
			selectLabel.setEnabled(false);
			genreLabel.setEnabled(false);
			searchUserLabel.setEnabled(false);
			setButtons(true);
			if (tracks.isVisible())
				tracks.setEnabled(true);
		} else if (getSearchUser.hasFocus()) {
			enterSearch.setEnabled(false);
			ifEmpty.setEnabled(false);
			selections.setEnabled(false);
			genres.setEnabled(false);
			getSearchPlaylist.setEnabled(false);
			selectLabel.setEnabled(false);
			genreLabel.setEnabled(false);
			searchPlaylistLabel.setEnabled(false);
			setButtons(true);
			if (tracks.isVisible())
				tracks.setEnabled(true);
		} else if (searchButton.hasFocus()) {
			if (enterSearch.isEnabled()) {
				System.out.println(enterSearch.getText());
			} else {
				if (selections.isEnabled()) {
					searchSelections(selections.getSelectedItem());
				} else if (genres.isEnabled()) {

				} else if (ifEmpty.isEnabled()) {
					Object empty = ifEmpty.getSelectedItem();
					System.out.println(empty);
					if (empty.equals(isEmpty[0])) {// find selections
						try {
							findSelections(1, 10, false);
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else if (empty.equals(isEmpty[1])) {// find genres

					} else if (empty.equals(isEmpty[2])) {// find trending

					}
				}
			}
		} else if (reset.hasFocus()) {
			Component[] components = { enterSearch, selectLabel, genreLabel, searchPlaylistLabel, searchUserLabel,
					selections, genres, ifEmpty, getSearchPlaylist, getSearchUser };
			if (searchTracks)
				components = new Component[] { components[3], components[4], components[8], components[9] };
			for (Component c : components)
				if (!c.isEnabled())
					c.setEnabled(true);
			if (searchTracks) {
				tracks.setEnabled(false);
				searchButton.setEnabled(false);
			} else {
				setButtons(false);
				manage = true;
			}
		} else if (tracks.hasFocus()) {
			String user = "";
			if (driver.getCurrentUrl().contains("/sets/")) {
				try {
					tracks.setEnabled(false);
					AddUsers.searchTracks(driver, executor, 1, baseUrl);
					tracks();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					tracks.setEnabled(false);
					AddUsers.searchPlaylistsOptions(true);
					int confirm = AddUsers.confirm;
					if (confirm != JOptionPane.YES_OPTION) {
						AddUsers.searchPlaylists(driver, executor, 1, baseUrl);
						user = driver.findElement(By.cssSelector("h3.profileHeaderInfo__userName")).getText();
						AddUsers.eachPlaylist(driver, executor, user);
					}
					tracks();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void tracks() throws IOException {
		executor.executeScript("window.scrollTo(0, 0);");
		BruteForce.soundCloudTracks(BruteForce.getCom(), BruteForce.getMaps());
	}

	private void setButtons(boolean enabled) {
		searchButton.setEnabled(enabled);
		reset.setEnabled(enabled);
	}

	private void searchSelections(Object select) {
		try {
			WebElement searchSelect = driver.findElement(By.xpath("//h2[text() = '" + select + "']"));
			wait.until(ExpectedConditions.visibilityOf(searchSelect));
			executor.executeScript("arguments[0].scrollIntoView();", searchSelect);
			String textSearch = Files.SoundCloud("txt", searchRegex + select, true);
			File getSearchSelections = new File(textSearch);
			if (getSearchSelections.exists())
				return;
			else
				getSearchSelections(1, select, textSearch);
			manage = false;
		} catch (NoSuchElementException n) {
			searchSelections(select);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getSearchSelections(int startItem, Object select, String textSearch) throws IOException {
		int i = getSelections.indexOf(select) + 1;
		String getContainer = "//li[" + i + "]/div/div[2]/div/div[1]/div/div";
		try {
			WebElement container = driver.findElement(By.xpath(getContainer));
			List<WebElement> allItems = container.findElements(By.cssSelector("div.tileGallery__sliderPanelSlide"));
			for (int j = startItem; j <= allItems.size(); j++) {
				if (manage)
					return;
				try {
					WebElement playlist;
					WebElement user;
					try {
						playlist = driver.findElement(By.xpath(getContainer + "/div[" + j + "]/div/div[2]/div[1]/a"));
						user = driver.findElement(By.xpath(getContainer + "/div[" + j + "]/div/div[2]/div[2]/a"));
					} catch (NoSuchElementException e) {
						playlist = driver.findElement(By.xpath(getContainer + "/div[" + j + "]/div/div[2]/a"));
						user = driver.findElement(By.xpath(getContainer + "/div[" + j + "]/div/div[2]/div/span"));
					}
					wait.until(ExpectedConditions.visibilityOf(playlist));
					new SetUp.Files(textSearch, playlist.getText() + splitRegex + user.getText() + splitRegex + playlist.getAttribute("href") + splitRegex + user.getAttribute("href"));
					if (j == allItems.size()) {
						WebElement leftButton = driver
								.findElement(By.xpath("//li[" + i + "]/div/div[2]/div/div[3]/button"));
						while (leftButton.isDisplayed())
							executor.executeScript("arguments[0].click();", leftButton);
						manage = true;
					}
				} catch (NoSuchElementException s) {
					WebElement rightButton = driver
							.findElement(By.xpath("//li[" + i + "]/div/div[2]/div/div[2]/button"));
					executor.executeScript("arguments[0].click();", rightButton);
					getSearchSelections(j, select, textSearch);
				} catch (TimeoutException t) {
					getSearchSelections(j, select, textSearch);
				}
			}
		} catch (NoSuchElementException n) {
			getSearchSelections(startItem, select, textSearch);
		}
	}

	private void findSelections(int start, int last, boolean click) throws IOException {
		for (int i = start; i <= last; i++) {
			try {
				WebElement selections = driver.findElement(By.xpath("//li[" + i + "]/div/div[1]/h2"));
				new SetUp.Files(textSelections, selections.getText());
				wait.until(ExpectedConditions.visibilityOf(selections));
				executor.executeScript("arguments[0].scrollIntoView();", selections);
				if (i == last) {
					WebElement container = driver.findElement(By.cssSelector("ul.lazyLoadingList__list"));
					List<WebElement> allItems = container.findElements(By.cssSelector("li.modularHome__item"));
					if (allItems.size() > last) {
						last = allItems.size();
						findSelections(i, last, click);
					}
				}
			} catch (NoSuchElementException n) {
				findSelections(i, last, click);
			}
		}
		executor.executeScript("window.scrollTo(0, 0);");
	}

	private void findGenres() {

	}

	private void setSelections(boolean enabled) {
		selectLabel.setEnabled(enabled);
		genreLabel.setEnabled(enabled);
		selections.setEnabled(enabled);
		genres.setEnabled(enabled);
		setSearch(enabled);
	}

	private static void setSearch(boolean enabled) {
		searchPlaylistLabel.setEnabled(enabled);
		searchUserLabel.setEnabled(enabled);
		getSearchPlaylist.setEnabled(enabled);
		getSearchUser.setEnabled(enabled);
	}

	@Override
	public void actionPerformed(ActionEvent a) {
		if (a.getSource() == refresh) {
			refresh();
		} else {
			findSelectedItems(true);
			if (a.getSource() == searchButton) {
				try {
					if (enterSearch.isEnabled()) {
						baseUrl = homeUrl.replace("discover", "search?q=" + enterSearch.getText().replaceAll(" ", "%20"));
						driver.get(baseUrl);
						isSearch = true;
					} else if (!getSearchPlaylist.isEnabled() && !getSearchUser.isEnabled())
						setSearch(true);
					if (checkFileContents()) {
						if (getSearchPlaylist.isEnabled())
							urls(playlistUrls, getSearchPlaylist);
						else if (getSearchUser.isEnabled())
							urls(userUrls, getSearchUser);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (a.getSource() == exit) {
				clear();
				selectedSelection = "";
				driver.close();
				manage = true;
			} else if (a.getSource() == home) {
				searchTracks = false;
				isSearch = false;
				baseUrl = homeUrl;
				driver.get(baseUrl);
			}
		}
		searchUsers.dispose();
	}

	private void refresh() {
		for (int i = 0; i < comboBoxes.size(); i++) {
			int countEnabled = 0, countVisible = 0;
			if (comboBoxes.get(i).isEnabled() || getSearchPlaylist.isEnabled() || getSearchUser.isEnabled()) {
				selectedComboBox = i;
				if (getSearchPlaylist.isEnabled())
					selectedComboBox = 3;
				else if (getSearchUser.isEnabled())
					selectedComboBox = 4;
				countEnabled++;
			}
			if (comboBoxes.get(i).isVisible() || getSearchPlaylist.isVisible() || getSearchUser.isVisible())
				countVisible++;
			if (countEnabled < countVisible)
				isSelectedComboBox = true;
		}
		String[] files = new String[textFiles.length];
		for (int i = 0; i < textFiles.length; i++)
			files[i] = textFiles[i].getName().replace(".txt", "");
		JLabel refreshLabel = new JLabel("Refresh which files: ");
		JPanel refreshFiles = new JPanel();
		JList<String> list = createJList(files, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, 4, null);
		JScrollPane scroll = getScroll(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		refreshFiles.add(refreshLabel);
		refreshFiles.add(scroll);
		int interactRefresh = JOptionPane.showOptionDialog(null, refreshFiles, "Refresh Files",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		if (interactRefresh == JOptionPane.YES_OPTION) {
			List<String> selectedFiles = list.getSelectedValuesList();
			for (String s : selectedFiles) {
				String getFile = Files.SoundCloud("txt", searchRegex + s, true);
				File f = new File(getFile);
				f.delete();
			}
		}
	}

	private static void findSelectedUrls(boolean find) {
		if (find) {
			selectedUser = getSearchUser.getSelectedItem();
			selectedPlaylist = 0;
			if (selectedUser == null)
				selectedUser = "";
			if (getSearchPlaylist.getSelectedIndex() > 0)
				selectedPlaylist = getSearchPlaylist.getSelectedIndex();
		} else {
			if (!getSearch.isEmpty()) {
				getSearchUser.setSelectedItem(selectedUser);
				getSearchPlaylist.setSelectedIndex(selectedPlaylist);
			}
		}
	}

	public static JList<String> createJList(String[] array, int listSelectionModel, int visibleRowCount, String before) {
		JList<String> list = new JList<String>(array);
		list.setSelectionMode(listSelectionModel);
		list.setVisibleRowCount(visibleRowCount);
		if (before != null) {
			list.setCellRenderer(new DefaultListCellRenderer() {
				private static final long serialVersionUID = 1L;
				@Override
                public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                     Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                     if (!before.contains(value.toString())) {
                    	 setBackground(Color.GREEN.brighter());
                    	 if (isSelected) 
                             setBackground(getBackground().darker());
                     }                    	 
                     return c;
                }
           });
		}
		return list;
	}

	public static JScrollPane getScroll(JList<String> list, int scrollDirection) {
		JScrollPane scroll = new JScrollPane(list);
		scroll.setVerticalScrollBarPolicy(scrollDirection);
		return scroll;
	}

	private static void findSelectedItems(boolean find) {
		findSelectedUrls(find);
		comboBoxes.clear();
		comboBoxes.add(selections);
		comboBoxes.add(genres);
		comboBoxes.add(ifEmpty);
		for (int i = 0; i < comboBoxes.size(); i++) {
			JComboBox<Object> c = comboBoxes.get(i);
			if (find) {
				if (selectedSelections.size() == comboBoxes.size())
					selectedSelections.set(i, c.getSelectedItem());
				else
					selectedSelections.add(c.getSelectedItem());
				selectedSelection = selectedSelections.get(0);
			} else if (!selectedSelections.isEmpty()) {
				for (Object s : selectedSelections) {
					if (s == null)
						continue;
					else
						c.setSelectedItem(s);
				}
			}
		}
	}

	private static boolean checkFileContents() throws IOException {
		String checkSearch = Files.SoundCloud("txt", searchRegex + selectedSelection, true);
		File f = new File(checkSearch);
		if (f.exists()) {
			Scanner s = new Scanner(f);
			ArrayList<String> checkContents = new ArrayList<String>();
			while (s.hasNextLine())
				checkContents.add(s.nextLine());
			s.close();
			for (int i = 0; i < getSearchPlaylist.getItemCount(); i++) {
				for (int j = 0; j < getSearchUser.getItemCount(); j++) {
					Object[] getItem = { getSearchPlaylist.getItemAt(i), getSearchUser.getItemAt(j) };
					for (Object k : getItem) {
						if (checkContents.toString().contains(k.toString()))
							continue;
						else
							return false;
					}
				}
			}
		} else {
			return false;
		}
		return true;
	}
	
	@Override
	public void changedUpdate(DocumentEvent d) {
	}
	@Override
	public void focusLost(FocusEvent f) {
	}
}
