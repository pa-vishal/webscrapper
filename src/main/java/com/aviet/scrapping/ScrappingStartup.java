package com.aviet.scrapping;

import com.aviet.scrapping.entities.City;
import com.aviet.scrapping.entities.Country;
import com.aviet.scrapping.entities.Office;
import com.aviet.scrapping.entities.WeWorkData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.BasicConfigurator;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScrappingStartup {

    private static final String WEWORK_LOCATION_PAGE_URL = "https://www.wework.com/locations";
    private static final String FILE_NAME = "Output.json";

    private static final String COUNTRY_NODE_SELECTOR = "div.country__countryList__2mgyq";
    private static final String COUNTRY_NAME_SELECTOR = "div.country__countryList__2mgyq span.countryName__countryList__33Eme";
    private static final String CITY_NODE_SELECTOR = "div.country__countryList__2mgyq ul li";
    private static final String OFFICE_NODE_SELECTOR = "div.marketCard__CardWrapper-jVCAkY a[href]";
    private static final String PRICE_TABLE_NODE_SELECTOR = "div.price-table";

    private static final Logger logger;

    static {
        logger = LoggerFactory.getLogger(ScrappingStartup.class);
    }

    public static void main(String[] args) {

        BasicConfigurator.configure();

        try (Writer writer = new FileWriter(FILE_NAME)) {

            Document doc = new HtmlDom().getDom(WEWORK_LOCATION_PAGE_URL);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            logger.info("Writing data to json");
            WeWorkData weWorkData = new WeWorkData(buildCountryWiseData(doc));
            gson.toJson(weWorkData , writer);
            logger.info("Date written to file named '{}'", FILE_NAME);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Country> buildCountryWiseData(Document doc) {
        List<Country> countries = new ArrayList<>();

        doc
            .select(COUNTRY_NODE_SELECTOR)
            .parallelStream()

            .filter(f -> f.select(COUNTRY_NAME_SELECTOR).text().contains("Germ") || f.select(COUNTRY_NAME_SELECTOR).text().contains("India")) //for debugging

            .forEach(
                    e -> {
                        String countryName = e.select(COUNTRY_NAME_SELECTOR).text();
                        logger.info("Fetching city-wise data in country: '{}'", countryName);
                        Elements cityElements = e.select(CITY_NODE_SELECTOR);
                        List<City> cities = buildCities(cityElements);

                        Country country = new Country(countryName, cities);
                        countries.add(country);

                    }
            );

        return countries;
    }

    private static List<City> buildCities(Elements cityElements){
        List<City> cities = new ArrayList<>();
        cityElements.parallelStream()
                .forEach(
                        c-> {
                            List<Office> offices = new ArrayList<>();
                            c.select("a[href]")
                                    .forEach(e -> {
                                        String cityUrl = toCityUrl(e.baseUri(), e.attr("href"));
                                        Document doc = new HtmlDom().getDom(cityUrl);
                                        doc.select(OFFICE_NODE_SELECTOR)
                                                .parallelStream()
                                                .map(o -> toOfficeUrl(o.baseUri() + o.attr("href")))
                                                .map(ScrappingStartup::buildOffice)
                                                .forEach(offices::add);
                                    });

                            String cityName = c.text();
                            logger.info("Fetched prices data for all offices in city: '{}'", cityName);
                            City city = new City(cityName, offices);
                            cities.add(city);
                        }
                );
        return cities;
    }

    private static Office buildOffice(String officeUrl){
        Document doc = new HtmlDom().getDom(officeUrl);
        String officeName = doc.title();
        logger.info("Fetching prices for building: '{}'", officeName);
        HashMap<String, String> officePrices = new HashMap<>();

        doc.select(PRICE_TABLE_NODE_SELECTOR)
                .forEach(e->
                {
                    String assetTitle = e.select("div.price-title").text();
                    String assetPrice = e.select("div.price-title-right span").text();
                    officePrices.put(assetTitle, assetPrice);

                    e.select("div.price-row")
                            .forEach(
                                p -> {
                                    String asset =  p.select("div div").first().text();
                                    String price = p.select("div.price-value span").text();
                                    officePrices.put(asset, price);
                                }
                            );
                }
        );
        logger.info("Prices fetched for building: '{}'", officeName);
        return new Office(officeName, officePrices);
    }

    private static String toOfficeUrl(String fullString){
        Pattern pattern = Pattern.compile("l/.*?-*.*?/");
        Matcher matcher = pattern.matcher(fullString);
        if(matcher.find()){
            return fullString.replace(matcher.group(),"");
        }
        return "";
    }

    private static String toCityUrl(String baseUri, String targetUri) {
        return baseUri.substring(0, baseUri.indexOf("/locations"))
                + targetUri;
    }

}
