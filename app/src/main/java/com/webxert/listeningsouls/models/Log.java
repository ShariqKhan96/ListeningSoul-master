package com.webxert.listeningsouls.models;

/**
 * Created by hp on 1/27/2019.
 */

public class Log {
    String note;
    String by;
    String time;

    public Log(String note, String by, String time) {
        this.note = note;
        this.by = by;
        this.time = time;
    }

    public Log() {
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
