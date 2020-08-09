package org.reactome.release.resourcechecker;

import java.io.IOException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.reactome.release.Resource;

public class WebPageResourceChecker implements HTTPResourceChecker {
	private Resource resource;

	public WebPageResourceChecker(Resource resource) {
		this.resource = resource;
	}

	@Override
	public Resource getResource() {
		return this.resource;
	}

	@Override
	public boolean resourceExists() {
		return HTTPResourceChecker.super.resourceExists() && hasExpectedContent();
	}

	private boolean hasExpectedContent() {
		try {
			// TODO: Work on parsing JSON instead of CSV
			// TODO: Check "Error Response Text" to see if an error occurred in the scraped content
			String scrapedContent = getContentByScrapingWebsite();
			if (
				!resource.getErrorResponseText().isEmpty() &&
				scrapedContent.contains(resource.getErrorResponseText())
			) {
				return false;
			} else if (
				!resource.getExpectedResponseText().isEmpty() &&
				!scrapedContent.contains(resource.getExpectedResponseText())
			) {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public String getReport() throws IOException {
		System.out.println(HTTPResourceChecker.super.getResponseCode());
		return getContentByScrapingWebsite();
	}

	private String getContentByScrapingWebsite() throws IOException {
		// Init chromedriver
		// TODO: Create instructions and/or script to install chromedriver
		String chromeDriverPath = "/usr/bin/chromedriver" ;
		System.setProperty("webdriver.chrome.driver", chromeDriverPath);
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
		WebDriver driver = new ChromeDriver(options);

		driver.get(getResourceURL().toString());
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String page = driver.getPageSource();
		driver.close();
		driver.quit();

//		Path exampleFile = Paths.get(".", "example.html");
//		Files.deleteIfExists(exampleFile);
//		Files.write(exampleFile, page.getBytes(), StandardOpenOption.CREATE);

		return page;
	}
}
