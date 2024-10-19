package com.example.module03_basicgui_db_interface;

/**
 * Model class representing a person/user in the application.
 */
public class Person {

    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String profilePicture;

    // Constructors
    public Person() {}

    public Person(Integer id, String name, String email, String phone, String address, String profilePicture) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.profilePicture = profilePicture;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // Similarly for other fields

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getters and setters for email, phone, address, profilePicture

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
