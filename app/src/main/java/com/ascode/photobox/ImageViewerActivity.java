package com.ascode.photobox;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ascode.photobox.security.Utils;
import com.google.android.material.snackbar.Snackbar;
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
    //public static final String select="Select A subFolder";
    String mainFolder="", myName, linkedUserName, selectedSubFolder=Utils.Companion.getSelect();
    Intent imgSelecter,gallery,welcome;
    private ArrayList<String> subfolders=new ArrayList<>();
    private TextView mainFolderText;
    DatabaseReference linkedUsersRef;
    //private Button imgViewButton;
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

    //start gallery view
    private void startGallery(ArrayList<String> links,ArrayList<String> imgNames, String selectedSubFolder){
        Intent galleryIntent = new Intent(this, GalleryViewActivity.class);
        galleryIntent.putExtra("imgLinks",links);
        galleryIntent.putExtra("imgNames",imgNames);
        galleryIntent.putExtra("currentFolder",selectedSubFolder);
        galleryIntent.putExtra("linkedFolder",linkedUserName);
        startActivity(galleryIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        //for loader
        final RelativeLayout custom= findViewById(R.id.customLoaderLayout);

        //create utils object
        final Utils utils= new Utils();
        utils.findMyLinkedFolder();

        //check internet
        if(new WelcomeActivity().isConnected(getApplicationContext())){
            //net connected
            Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_SHORT).show();
        }
        else{
            //not connected
            Toast.makeText(getApplicationContext(),"Not Connected",Toast.LENGTH_LONG).show();

            //snackBar
            final Snackbar connectionBar=Snackbar.make(findViewById(R.id.WelcomeScroll),"No Internet",Snackbar.LENGTH_INDEFINITE);
            connectionBar.setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("retry done");
                }
            });
            connectionBar.getView().setBackgroundColor(Color.RED);
            connectionBar.setTextColor(Color.BLACK);
            connectionBar.show();

            //set btn clicked to null if internet not available
        }

        //views
        subfolderDropdown = findViewById(R.id.spin);
        mainFolderText = findViewById(R.id.mainFolderText);
        //imgViewButton = findViewById(R.id.imgViewBtn);
        final Button viewGalleryBtn= findViewById(R.id.viewGalleryBtn);

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
        //imgViewButton.setAlpha(0);

        System.out.println("duration in onCreate="+duration);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                //checking utils
                System.out.println("utils--- "+Utils.Companion.getLinkedUserName());

                mainFolder=linkedUserName;
                mainFolderText.setText(mainFolder);
                //to fill subfolderList after linkedName is fetched
                if(linkedUserName != null)
                    fillSubFolders();
                else{
                    //Toast.makeText(ImageViewerActivity.this,"try again",Toast.LENGTH_SHORT).show();
                    System.out.println("data not loading");
                }

            }
        },800);

        //find toolbar object
        Toolbar toolbar = findViewById(R.id.toolbar);
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
        subfolders.add(Utils.Companion.getSelect());// to add this when nothing is added

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
                        utils.findAllLinks(selectedSubFolder);  //trying

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                final ArrayList<String> imgLinks= utils.getAllLinks();
                                final ArrayList<String> imgNames= utils.getImgNames();
                                System.out.println("links from util--- "+imgLinks);

                                //onClickListener
                                viewGalleryBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startGallery(imgLinks,imgNames,selectedSubFolder);
                                    }
                                });
                            }
                        },1500);


                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) { }
                });

                //show button
                //imgViewButton.setAlpha(1);
                custom.setAlpha(0);

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