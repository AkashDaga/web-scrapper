package com.web.scraper.webscraper.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.web.scraper.webscraper.services.WebParserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class WebParserServiceImpl implements WebParserService {
    private final String SCRIPT = "script";
    private final String NEXT_DATA = "__NEXT_DATA__";
    private final String PROPS = "props";
    private final String PAGE_PROPS = "pageProps";
    private final String PROPS_REQ = "propsReq";
    private final String ARTICLE_INFO = "articleInfo";
    private final String ARTICLE = "article";
    private final String CONTENT = "content";
    private final String START_READING_FROM_CONTENT = ":::section{.abstract}";
    private final String TAGS = "tags";


    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public File parseBulk(MultipartFile multipartFile) throws Exception {
        String filePath = new Date().getTime() + ".csv";
        File file = new File(filePath);
        if (file.exists()) {
            log.info("deleting existing file : {}", file.delete());
        } else {
            log.info("creating new file : {} ", file.createNewFile());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(file)); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()))) {
            String line = Strings.EMPTY;
            int rowsToSkip = 1;
            while ((line = bufferedReader.readLine()) != null) {
                //Excluding the header rows to be skipped for Mis Report
                if (rowsToSkip > 0) {
                    rowsToSkip--;
                    log.info("This row is required to be skipped!!!: {}", line);
                    continue;
                }

                //Splitting Components
                String[] row = line.split(",");
                String url = row[0];
                String tag = row[1];
                List<String> keyWordList = new ArrayList<>();
                keyWordList.addAll(Arrays.asList(row).subList(2, row.length));
                String parsedUrl = parse(url, tag, keyWordList);
                if (StringUtils.hasLength(parsedUrl)) {
                    writer.writeNext(row);
                }
            }
            writer.flush();
        } catch (Exception e) {
            log.error("error ", e);
        }

        return file;
    }

    @Override
    public String parse(String url, String tag, List<String> keyWordList) throws Exception {
        Document doc = Jsoup.connect(url).get();
        Elements scriptElements = doc.getElementsByTag(SCRIPT);

        Element scriptElement = scriptElements.stream().filter(element -> Objects.nonNull(element.getElementById(NEXT_DATA))).findFirst().get();
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
        };

        HashMap<String, Object> dataMap = objectMapper.readValue(scriptElement.data(), typeRef);
        LinkedHashMap<String, Object> props = (LinkedHashMap<String, Object>) dataMap.get(PROPS);
        LinkedHashMap<String, Object> pageProps = (LinkedHashMap<String, Object>) props.get(PAGE_PROPS);
        LinkedHashMap<String, Object> propsReq = (LinkedHashMap<String, Object>) pageProps.get(PROPS_REQ);
        LinkedHashMap<String, Object> articleInfo = (LinkedHashMap<String, Object>) propsReq.get(ARTICLE_INFO);
        LinkedHashMap<String, Object> articleMap = (LinkedHashMap<String, Object>) articleInfo.get(ARTICLE);
        String content = (String) articleMap.get(CONTENT);
        final String strippedContent = content.substring(content.indexOf(START_READING_FROM_CONTENT));
        LinkedHashMap<String, Object> tagMap = (LinkedHashMap<String, Object>) ((List<Object>) articleMap.get(TAGS)).get(0);

        AtomicBoolean isTagMatched = new AtomicBoolean(false);
        for (Map.Entry<String, Object> entry : tagMap.entrySet()) {
            String tagFound = (String) entry.getValue();
            if (tagFound.equalsIgnoreCase(tagFound)) {
                isTagMatched.set(true);
                break;
            }
        }
        AtomicBoolean isKeyWordMatched = new AtomicBoolean(false);
        if (isTagMatched.get()) {
            for (String keyWord : keyWordList) {
                if (strippedContent.contains(keyWord)) {
                    isKeyWordMatched.set(true);
                    break;
                }
            }
        }

        if (isTagMatched.get() && isKeyWordMatched.get()) {
            return url;
        } else {
            return null;
        }
    }
}
