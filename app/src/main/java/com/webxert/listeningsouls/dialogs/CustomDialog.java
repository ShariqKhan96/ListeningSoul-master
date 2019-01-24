package com.webxert.listeningsouls.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.webxert.listeningsouls.R;

/**
 * Created by hp on 1/24/2019.
 */

public class CustomDialog extends Dialog {

    public Activity c;
    public Dialog d;
    public Button yes, no;
   public EditText edtAuth;

    public CustomDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.auth_dialog);
        yes = (Button) findViewById(R.id.yes);
        no = (Button) findViewById(R.id.no);
        edtAuth = findViewById(R.id.edtAuth);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
