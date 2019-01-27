package com.webxert.listeningsouls.models;

import java.util.Date;

/**
 * Created by hp on 12/18/2018.
 */

public class ChatModel {
    String with;
    String id;
    boolean seen;
    Date date;
    long timestamp;
    String assignedTo;


    public ChatModel() {
    }

    public ChatModel(String with, String id, boolean seen, Date date, long timestamp, String assignedTo) {
        this.with = with;
        this.id = id;
        this.seen = seen;
        this.date = date;
        this.timestamp = timestamp;
        this.assignedTo = assignedTo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}

