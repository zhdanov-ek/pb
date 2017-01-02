package com.example.gek.pb.data;

/**
 * Created by gek on 02.01.17.
 */

public class Contact {
    private String name;
    private String position;
    private String photo;
    private String email;
    private String phone;
    private String phone2;

    public Contact() {
    }

    public Contact(String name, String position, String photo, String email, String phone, String phone2) {
        this.name = name;
        this.position = position;
        this.photo = photo;
        this.email = email;
        this.phone = phone;
        this.phone2 = phone2;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }
}
