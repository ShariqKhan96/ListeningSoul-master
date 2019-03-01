package com.webxert.listeningsouls;

import android.Manifest;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class PhotoActivity extends AppCompatActivity {

    ImageView down_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_dialog);
        overridePendingTransition(0, 0);

        down_image = findViewById(R.id.down_image);

        final String url = getIntent().getStringExtra("url");
        down_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(PhotoActivity.this)
                        .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    downloadImage(url);
                                    Toast.makeText(PhotoActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(PhotoActivity.this, "Some permissions not granted!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                            }
                        }).check();


            }
        });
        Log.e("Url", url);
        ImageView imageView = findViewById(R.id.photo_view);
        GlideApp.with(this).load(url).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                Log.e("GlideException", e.getMessage());
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                Log.e("onResourceReady", "onResourceReady");
                return false;
            }
        }).into(imageView);
    }

    public void downloadImage(final String path) {
        AndroidNetworking.initialize(getApplicationContext());
        final File imageRoot = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Listening Souls");
        imageRoot.mkdirs();
        final File image = new File(imageRoot, UUID.randomUUID().toString() + ".jpg");
        //Folder Creating Into Phone Storage
        // dirPath = Environment.getExternalStorageDirectory() + "/ListeningSouls";
        // fileName = "image.jpeg";

        //file Creating With Folder & Fle Name
        //file = new File(getGalleryPath(), randomId);

        //Click Listener For DownLoad Button

        AndroidNetworking.download(path, imageRoot.getAbsolutePath(), UUID.randomUUID().toString() + ".jpg")
                .build()
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        new Handler()
                                .postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(PhotoActivity.this, "Downloaded!", Toast.LENGTH_SHORT).show();
                                    }
                                }, 2000);
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(PhotoActivity.this, "" + anError.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Error", anError.getMessage());
                        Log.e("ErrorCode", anError.getErrorCode() + "");
                    }
                });


    }

    private static String getGalleryPath() {
        return Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/" + "Listening Souls";
    }

}

