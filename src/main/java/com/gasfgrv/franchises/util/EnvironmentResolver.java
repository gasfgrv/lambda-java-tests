package com.gasfgrv.franchises.util;

public class EnvironmentResolver {

    private EnvironmentResolver() {
    }

    public static String envOrDefault(String key, String defaultValue) {
        var value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

}
