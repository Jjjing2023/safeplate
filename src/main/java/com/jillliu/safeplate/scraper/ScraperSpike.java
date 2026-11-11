package com.jillliu.safeplate.scraper;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class ScraperSpike {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500));
            Page page = browser.newPage();
            page.navigate("https://example.com");
            System.out.println("Title: " + page.title());
        }
    }

}
