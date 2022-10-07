package com.web.scraper.webscraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.web.scraper.webscraper")
public class WebScraperApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebScraperApplication.class, args);
    }
}

