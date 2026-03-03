package com.example.pia_claseordinaria;

public class User {
    public String fullName, email, phone, address, status, role;

    public User() {}

    public User(String fullName, String email, String phone, String address) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.status = "pending";
        this.role = "USER"; // Por defecto todos son usuarios
    }
}
