package com.example.pia_claseordinaria;

public class AccessRecord {
    public String userId, email, key, date, time, status;
    public long timestamp; // Campo para ordenamiento preciso

    public AccessRecord() {}

    public AccessRecord(String userId, String email, String key, String date, String time) {
        this.userId = userId;
        this.email = email;
        this.key = key;
        this.date = date;
        this.time = time;
        this.status = "pending";
        this.timestamp = System.currentTimeMillis();
    }
}
