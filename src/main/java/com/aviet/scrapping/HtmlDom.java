package com.aviet.scrapping;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class HtmlDom {

    private final Logger logger;

    HtmlDom() {
        logger = LoggerFactory.getLogger(ScrappingStartup.class);
    }

    Document getDom(String url){
        Document doc = null;
        try {
            logger.info("Connecting to url: '{}'", url);
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com").get();

        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Connected to url: '{}'", url);
        return doc;
    }
}