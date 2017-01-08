package com.example.gek.pb.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gek on 02.01.17.
 */

public class Contact implements Parcelable {
    private String name;
    private String position;
    private String photoUrl;
    private String email;
    private String phone;
    private String phone2;

    public Contact() {
    }

    public Contact(String name, String position, String photoUrl, String email, String phone, String phone2) {
        this.name = name;
        this.position = position;
        this.photoUrl = photoUrl;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.position);
        dest.writeString(this.photoUrl);
        dest.writeString(this.email);
        dest.writeString(this.phone);
        dest.writeString(this.phone2);
    }

    protected Contact(Parcel in) {
        this.name = in.readString();
        this.position = in.readString();
        this.photoUrl = in.readString();
        this.email = in.readString();
        this.phone = in.readString();
        this.phone2 = in.readString();
    }

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel source) {
            return new Contact(source);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}
