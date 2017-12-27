package com.example.enrico.myapplication;

/**
 * Created by enrico on 28/12/17.
 */

public class Users {

    public String name;
    public String image;
    public String status;
    public String username;

    public Users(){}

    public Users(String name, String image, String status, String username) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
