package algorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import algorithms.SetUp.*;

public class CreateMusicDatabase {
	final private String baseUrl = "https://www.billboard.com/charts/artist-100";
	final private boolean update = false;
	
	public static void main(String[] args) throws IOException {
		new CreateMusicDatabase();
	}
	
	public CreateMusicDatabase() throws IOException {
		File f = new File(SetUp.Files.file("PopularArtists"));
		if (!f.exists() || update)
			setUpWebDriver();
		ArrayList<String> popularArtists = SetUp.Files.arraylist(f), existingUsers;
		for (int i = 0; i < popularArtists.size(); i++)
			popularArtists.set(i, popularArtists.get(i) + "_CREATE");
		f = new File(SetUp.Files.file("AddedUsers"));
		existingUsers = SetUp.Files.arraylist(f);
		if (!existingUsers.isEmpty()) {
			popularArtists.addAll(existingUsers);
			Collections.sort(popularArtists, String.CASE_INSENSITIVE_ORDER);	
		}
		new SetUp.Files("AddedUsers", popularArtists, 0);
	}
	
	/**
	 * Sets up a Web Driver.
	 * @throws IOException
	 */
	private void setUpWebDriver() throws IOException {
		System.setProperty(WebDrivers.chrome, Drivers.chrome);
		WebDriver driver = new ChromeDriver();
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		try {
			driver.manage().window().maximize();
			driver.get(baseUrl);
			AddUsers.multipleAdsTabs(driver);
			AddUsers.mainAdTab(driver, baseUrl);
			AddUsers.cookies(driver, executor);
			popularArtists(driver);	
			driver.close();
		}  catch (NoSuchWindowException n) {
			System.out.println("Chrome window closed unexpectedly");
			setUpWebDriver();
		} catch (WebDriverException w) {
			System.out.println("WebDriver closed unexpectedly");
			setUpWebDriver();
		}
	}

	private void popularArtists(WebDriver driver) throws IOException {
		ArrayList<String> popularArtists = new ArrayList<String>();
		WebElement chart = driver.findElement(By.cssSelector("div.chart-details"));
		List<WebElement> list = chart.findElements(By.cssSelector("div.chart-list")), artists;
		for (WebElement c : list) {
			artists = c.findElements(By.cssSelector("div.chart-list-item"));
			for (WebElement a : artists) 
				popularArtists.add(a.getAttribute("data-title"));
		}
		Collections.sort(popularArtists, String.CASE_INSENSITIVE_ORDER);
		new SetUp.Files("PopularArtists", popularArtists, 0);
	}
}
