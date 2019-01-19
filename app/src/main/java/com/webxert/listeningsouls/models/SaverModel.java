package com.webxert.listeningsouls.models;

import java.util.Map;

/**
 * Created by hp on 12/15/2018.
 */

public class SaverModel {
    String id;
    Map<String,String> map;

    public SaverModel(String id, Map<String, String> map) {
        this.id = id;
        this.map = map;
    }

    public SaverModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }
}
