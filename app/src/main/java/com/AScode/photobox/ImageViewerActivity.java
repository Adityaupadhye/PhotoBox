package com.AScode.photobox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ImageViewerActivity extends AppCompatActivity {
    private Toolbar toolbar;
    String mainFolder="",myName,linkedUserName;
    Intent imgSelecter,gallery,welcome;
    private ArrayList<String> subfolders=new ArrayList<>();
    private ArrayAdapter<String> stringArrayAdapter;
    private Spinner spinner;
    private TextView mainFolderText;
    FirebaseUser currentUser;
    DatabaseReference linkedUsersRef;
    Button imgViewButton;


    //to find correct linkedUserName
    ChildEventListener linkedUsers=new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            if(snapshot.getKey() != null){
                String[] split =snapshot.getKey().split("AND");
                //to find linkedUserName
                if(split[0].equals(myName) || split[1].equals(myName)){
                    linkedUserName=snapshot.getKey().trim();
                    System.out.println("linkedName is "+linkedUserName);
                }

            }
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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        //views
        spinner=findViewById(R.id.spin);
        mainFolderText=findViewById(R.id.mainFolderText);
        imgViewButton=findViewById(R.id.imgViewBtn);

        //intents
        imgSelecter=new Intent(getApplicationContext(),ImageSelectorActivity.class);
        welcome=new Intent(getApplicationContext(),WelcomeActivity.class);
        gallery=new Intent(getApplicationContext(),GalleryActivity.class);

        //databaseRef
        linkedUsersRef= FirebaseDatabase.getInstance().getReference().child("linkedUsers");
        //add childEventListener to linkedUserRef
        linkedUsersRef.addChildEventListener(linkedUsers);

        //firebase Auth and user
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null)
            myName=currentUser.getDisplayName();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mainFolder=linkedUserName;
                mainFolderText.setText(String.format("MainFolder: %s", mainFolder));
            }
        },3000);

        //animate button until linkedUserName is found
        imgViewButton.setAlpha(0);
        imgViewButton.animate().alpha(1).setDuration(4000);
        mainFolderText.setAlpha(0);
        mainFolderText.animate().alpha(1).setDuration(4000);


        //find toolbar object
        toolbar=findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        //toolbar menu options
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.uplpad){
                    startActivity(imgSelecter);
                }
                return false;
            }
        });

        //for back button on toolbar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(welcome);
                finish();
            }
        });

        //arraylist and adapter
        stringArrayAdapter=new ArrayAdapter<>(ImageViewerActivity.this,R.layout.spinner_layout,subfolders);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.add_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void viewImg(View view){
        startActivity(gallery);
        gallery.putExtra("linkedUserName",linkedUserName);
    }
}