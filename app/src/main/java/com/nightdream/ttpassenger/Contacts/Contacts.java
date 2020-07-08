package com.nightdream.ttpassenger.Contacts;

public class Contacts {
    private String uId, name, userNumber, image;

    public Contacts(String uID, String name, String userNumber, String image) {
        this.uId = uID;
        this.name = name;
        this.userNumber = userNumber;
        this.image = image;
    }

    public Contacts() {

    }

    public String getuId() {
        return uId;
    }

    public String getName() {
        return name;
    }

    public String getUserNumber() {
        return userNumber;
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
}
