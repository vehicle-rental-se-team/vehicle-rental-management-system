package com.vehiclerental.domain;

public class Manager {

    private final String username;
    private final String password;

    public Manager(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
