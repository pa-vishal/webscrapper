package com.aviet.scrapping.entities;

import java.util.List;

public class City{
    private String city;
    private List<Office> offices;

    public City(String city, List<Office> offices) {
        this.city = city;
        this.offices = offices;
    }
}
