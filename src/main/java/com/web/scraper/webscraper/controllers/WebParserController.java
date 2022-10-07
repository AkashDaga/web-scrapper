package com.web.scraper.webscraper.controllers;

import com.web.scraper.webscraper.services.WebParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/web-parser/v1")
public class WebParserController {

    @Autowired
    private WebParserService webParserService;

    @PostMapping("/parse")
    public String parse(@RequestParam("url") String url, @RequestParam("tag") String tag, @RequestParam("key-word-list") List<String> keyWordList) throws Exception {
        return webParserService.parse(url, tag, keyWordList);
    }

    @PostMapping(value = "/parse/bulk/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> parseBulk(@RequestBody MultipartFile multipartFile) throws Exception {
        File file = webParserService.parseBulk(multipartFile);
        InputStreamResource inputStreamResource = new InputStreamResource(file.toURL().openStream());

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName()).contentType(MediaType.parseMediaType("application/csv")).body(inputStreamResource);
    }
}
