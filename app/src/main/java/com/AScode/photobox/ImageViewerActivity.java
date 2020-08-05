package com.AScode.photobox;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ImageViewerActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private static final String select="Select A subFolder";
    String mainFolder="", myName, linkedUserName, selectedSubFolder=select;
    Intent imgSelecter,gallery,welcome;
    private ArrayList<String> subfolders=new ArrayList<>();
    private TextView mainFolderText;
    DatabaseReference linkedUsersRef;
    private Button imgViewButton;
    private Spinner subfolderDropdown;
    private ArrayAdapter<String> subFolderAdapter;
    private long start,duration=0;


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
                    duration=System.currentTimeMillis()-start;
                    System.out.println("end time="+duration);
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

    //to fill subfolders List
    private void fillSubFolders(){
        linkedUsersRef.child(linkedUserName)
                .child("subfolders")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String subValue;
                for(DataSnapshot subs:snapshot.getChildren()){
                    if(subs.getValue() != null){ //null check
                        subValue=subs.getValue().toString();
                        System.out.println("subs is "+subValue);
                        if( !subfolders.contains(subValue) ){
                            subfolders.add(subValue);
                            subFolderAdapter.notifyDataSetChanged();
                        }
                    }

                }
                duration=System.currentTimeMillis()-start;
                System.out.println("duration of fillSubFolder="+duration);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        //views
        subfolderDropdown = findViewById(R.id.spin);
        mainFolderText = findViewById(R.id.mainFolderText);
        imgViewButton = findViewById(R.id.imgViewBtn);

        //intents
        imgSelecter = new Intent(getApplicationContext(), ImageSelectorActivity.class);
        welcome = new Intent(getApplicationContext(), WelcomeActivity.class);
        gallery = new Intent(getApplicationContext(), GalleryActivity.class);

        //databaseRef
        linkedUsersRef = FirebaseDatabase.getInstance().getReference().child("linkedUsers");

        //measure time taken
        start=System.currentTimeMillis();
        System.out.println("start="+start);
        //add childEventListener to linkedUserRef
        linkedUsersRef.addChildEventListener(linkedUsers);

        //getting myName form intent
        myName = getIntent().getStringExtra("myName");
        System.out.println("myName from intent is "+myName);

        //animate button until linkedUserName is found
        imgViewButton.setAlpha(0);

        System.out.println("duration in onCreate="+duration);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mainFolder=linkedUserName;
                mainFolderText.setText(mainFolder);
                //to fill subfolderList after linkedName is fetched
                if(linkedUserName != null)
                    fillSubFolders();
                else
                    Toast.makeText(ImageViewerActivity.this,"try again",Toast.LENGTH_SHORT).show();
            }
        },700);

        //find toolbar object
        toolbar=findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        //toolbar menu options
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.uplpad){
                    startActivity(imgSelecter);
                    overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                }
                return false;
            }
        });

        //for back button on toolbar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                finish();
            }
        });

        //arraylist and adapter
        subFolderAdapter=new ArrayAdapter<>(ImageViewerActivity.this,R.layout.spinner_layout,subfolders);
        subfolders.add(select);// to add this when nothing is added

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //set adapter after 2.5secs after list is filled
                subfolderDropdown.setAdapter(subFolderAdapter);

                // to set itemClickListener for dropdown
                subfolderDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        selectedSubFolder=subfolderDropdown.getItemAtPosition(i).toString();
                        System.out.println("selectedSubFolder is "+selectedSubFolder);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) { }
                });

                //show button
                imgViewButton.setAlpha(1);

            }
        },1500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.add_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void viewImg(View view){
        gallery.putExtra("myNameFromIntent",myName);
        gallery.putExtra("linkedUserName",linkedUserName);
        gallery.putExtra("subFolder",selectedSubFolder);
        startActivity(gallery);
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
    }
}