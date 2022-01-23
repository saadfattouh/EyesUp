package com.example.eyesup.model;

public class User {

    private int id;
    private String userName;
    private String address;
    private String phone;
    private String birthDate;


    public User(int id, String userName, String address, String phone, String birthDate) {
        this.id = id;
        this.userName = userName;
        this.address = address;
        this.phone = phone;
        this.birthDate = birthDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
}