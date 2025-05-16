package com.example.ais777.data;

import javax.crypto.SecretKey;

public class Session {
    private static Session instance;
    private int userId;
    private SecretKey key;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setUser(int userId, SecretKey key) {
        this.userId = userId;
        this.key    = key;
    }

    public int getUserId() {
        return userId;
    }

    public SecretKey getKey() {
        return key;
    }
}