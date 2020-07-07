package com.AScode.photobox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class GalleryActivity extends AppCompatActivity {

    ImageView imgView;
    private ProgressDialog progressDialog;
    private String URL;
    Intent imgViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        //all views
        imgView=findViewById(R.id.loadImg);

        //intents
        imgViewer=new Intent(GalleryActivity.this,ImageViewerActivity.class);

        //progressdialog
        progressDialog=new ProgressDialog(this);

        //get url from DB
        FirebaseDatabase.getInstance().getReference().child("urls").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                URL=snapshot.getValue().toString();
                System.out.println(URL);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


        setProgressDialog(1);   //start loading
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadImage();
            }
        },4000);

        //toolbar
        Toolbar toolbar=findViewById(R.id.galleryToolbar);
        this.setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(imgViewer);
                finish();
            }
        });

    }

    private void setProgressDialog(int code){
        progressDialog.setProgressStyle(android.R.style.Widget_DeviceDefault_ProgressBar);
        progressDialog.setMessage("Loading");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        if (code == 0) {
            progressDialog.dismiss();
        }
    }

    protected void loadImage(){
        Glide.with(GalleryActivity.this)
                .load(URL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        setProgressDialog(0);
                        Toast.makeText(GalleryActivity.this,"failed"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        setProgressDialog(0);
                        Toast.makeText(GalleryActivity.this,"Image is Loaded",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                })
                .into(imgView);
        imgView.setBackgroundColor(Color.TRANSPARENT);
    }

}
