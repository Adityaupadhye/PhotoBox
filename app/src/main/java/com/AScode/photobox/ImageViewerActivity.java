package com.AScode.photobox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class ImageViewerActivity extends AppCompatActivity {
    private Toolbar toolbar;
    String mainFolder="";
    Intent imgSelecter,gallery,welcome;
    private ArrayList<String> subfolders=new ArrayList<>();
    private ArrayAdapter<String> stringArrayAdapter;
    private Spinner spinner;
    private TextView mainFolderText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        //views
        spinner=findViewById(R.id.spin);
        mainFolderText=findViewById(R.id.mainFolderText);

        mainFolderText.setText("MainFolder: "+mainFolder);

        //intents
        imgSelecter=new Intent(getApplicationContext(),ImageSelectorActivity.class);
        welcome=new Intent(getApplicationContext(),WelcomeActivity.class);
        gallery=new Intent(getApplicationContext(),GalleryActivity.class);


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
    }
}