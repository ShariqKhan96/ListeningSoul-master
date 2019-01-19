package com.webxert.listeningsouls.utils;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by hp on 1/15/2019.
 */

public class Utils {
    public static ProgressDialog getChatProgressDialog(Context context)
    {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle("Fetching Chats");
        dialog.setMessage("Please wait");
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
    public static ProgressDialog getMessageProgressDialog(Context context)
    {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle("Fetching Messages");
        dialog.setMessage("Please wait");
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
    public static ProgressDialog getRegisterationDialog(Context context)
    {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle("Signing Up");
        dialog.setMessage("Please wait");
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
