package com.example.gek.pb.data;

/**
 * Created by gek on 02.01.17.
 */

public class User {
    private String email;
    private String description;
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }



    public User() { }

    public User(String email, String description) {
        this.email = email;
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
