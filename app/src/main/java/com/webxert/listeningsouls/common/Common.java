package com.webxert.listeningsouls.common;

import com.webxert.listeningsouls.models.User;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class Common {

    public static String getPersonName(String personID) {

        List<User> users = new ArrayList<>();
        users.addAll(Paper.book().read("users", new ArrayList<User>()));

        for (User user : users) {
            if (user.getId().equals(personID))
                return user.getName();
        }

        return "Anonymous";
    }

}
