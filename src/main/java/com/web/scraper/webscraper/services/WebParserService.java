package com.web.scraper.webscraper.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public interface WebParserService {
    String parse(String url, String keyWord, List<String> keyWordList) throws Exception;

    File parseBulk(MultipartFile file) throws Exception;
}
