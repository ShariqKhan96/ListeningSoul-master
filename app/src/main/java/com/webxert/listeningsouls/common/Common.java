package com.webxert.listeningsouls.common;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.webxert.listeningsouls.models.User;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class Common {

    public static String getPersonName(final String personID) {

        List<User> users = new ArrayList<>();
        users.addAll(Paper.book().read("users", new ArrayList<User>()));

        for (User user : users) {
            if (user.getId().equals(personID)) {
                return user.getName();
            }

        }
        return personID;
    }

    public static boolean checkBlockStatus(String id) {
        List<User> users = new ArrayList<>();
        Log.e("id", id);
        users.addAll(Paper.book().read("users", new ArrayList<User>()));

        for (User user : users) {
            Log.e("User", "id " + user.getId() + " block status " + user.blocked);
            if (user.getId().equals(id)) {
                if (user.isBlocked()) {

                    return true;
                }

            }
        }
        return false;
    }

}
