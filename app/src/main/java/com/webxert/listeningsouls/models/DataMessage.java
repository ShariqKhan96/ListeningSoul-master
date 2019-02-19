package com.webxert.listeningsouls.models;

import java.util.Map;

/**
 * Created by hp on 2/19/2019.
 */

public class DataMessage {
    String to;
    Map<String, String> data;

    public DataMessage() {
    }

    public DataMessage(String to, Map<String, String> data) {
        this.to = to;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
