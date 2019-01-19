package com.webxert.listeningsouls.models;

/**
 * Created by hp on 12/10/2018.
 */


public class MessageModel {

    String email;
    String view_type;
    String message;
    String is_admin;
    String id;


    public MessageModel() {
    }

    public MessageModel(String email, String view_type, String message, String is_admin, String id) {
        this.email = email;
        this.view_type = view_type;
        this.message = message;
        this.is_admin = is_admin;
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getView_type() {
        return view_type;
    }

    public void setView_type(String view_type) {
        this.view_type = view_type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIs_admin() {
        return is_admin;
    }

    public void setIs_admin(String is_admin) {
        this.is_admin = is_admin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
