package com.webxert.listeningsouls.models;

/**
 * Created by hp on 12/10/2018.
 */

public class User {
    public String id;
    public String email;
    public String name;
    public String password;
    public String ph_no;
    public boolean is_admin;


    public User(String id, String email, String name, String password, String ph_no, boolean is_admin) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.ph_no = ph_no;
        this.is_admin = is_admin;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPh_no() {
        return ph_no;
    }

    public void setPh_no(String ph_no) {
        this.ph_no = ph_no;
    }

    public boolean isIs_admin() {
        return is_admin;
    }

    public void setIs_admin(boolean is_admin) {
        this.is_admin = is_admin;
    }
}
