package com.aviet.scrapping.entities;

import java.util.List;

public class WeWorkData {

    private final String description;
    private List<Country> countries;

    public WeWorkData(List<Country> countries) {
        this.countries = countries;
        this.description = "Pricing data (country wise) for all WeWork locations";
    }
}

