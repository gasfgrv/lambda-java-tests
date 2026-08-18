package com.gasfgrv.franchises.model;

public record Franchise(
        String id,
        String name,
        Integer foundationYear,
        String city,
        Integer titles,
        Integer conferenceTitles,
        Conference conference) {

}
