package com.webxert.listeningsouls.models;

/**
 * Created by hp on 12/18/2018.
 */

public class ChatModel {
    String with;
    String id;
    boolean seen;

    public ChatModel() {
    }

    public ChatModel(String with, String id, boolean seen) {
        this.with = with;
        this.id = id;
        this.seen = seen;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
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
