package com.example.pia_claseordinaria;

public class AccessRecord {
    private String dateTime;
    private String accessKey;

    public AccessRecord(String dateTime, String accessKey) {
        this.dateTime = dateTime;
        this.accessKey = accessKey;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getAccessKey() {
        return accessKey;
    }
}
