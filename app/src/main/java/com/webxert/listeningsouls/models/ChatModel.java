package com.webxert.listeningsouls.models;

/**
 * Created by hp on 12/18/2018.
 */

public class ChatModel {
    String with;
    String id;

    public ChatModel() {
    }

    public ChatModel(String with, String id) {
        this.with = with;
        this.id = id;
    }

    public String getWith() {
        return with;
    }

    public void setWith(String with) {
        this.with = with;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
