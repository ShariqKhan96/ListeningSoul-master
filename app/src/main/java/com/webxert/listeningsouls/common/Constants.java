package com.webxert.listeningsouls.common;

import com.webxert.listeningsouls.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hp on 12/10/2018.
 */

public class Constants {
    public enum Authentication {
        CUSTOMER, ADMIN
    }



    public static final String CUSTOMER_AUTH = "CUSTOMER";
    public static final String ADMIN_AUTH = "ADMIN";
    public static final String DOMAIN_NAME = "listening_souls";
    public static final String AUTH_ = "user_auth";
    public static final String LOGIN_ = "is_login";
    public static final String USER_EMAIL = "email";
    public static final String USER_NAME = "name";
    public static final String EXCPETION = "Exception";
    public static final int ADMIN_TYPE = 0;
    public static final int CUSTOMER_TYPE = 1;
    public static List<User> userList = new ArrayList<>();


    public static final int SPLASH_TIME = 2000;
    public static final String SH_PREFS = "SharedPreferences";

}
