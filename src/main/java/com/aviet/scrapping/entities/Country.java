package com.aviet.scrapping.entities;

import java.util.List;

public class Country {
    private String country;
    private List<City> cities;

    public Country(String country, List<City> cities) {
        this.country = country;
        this.cities = cities;
    }
}
