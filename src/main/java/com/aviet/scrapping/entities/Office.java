package com.aviet.scrapping.entities;

import java.util.HashMap;

public class Office{
    private String office;
    private HashMap<String, String> officePrices;

    public Office(String office, HashMap<String, String> officePrices) {
        this.office = office;
        this.officePrices = officePrices;
    }
}
