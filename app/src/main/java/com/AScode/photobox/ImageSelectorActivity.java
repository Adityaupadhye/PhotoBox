package com.AScode.photobox;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ImageSelectorActivity extends AppCompatActivity {
   ImageView imageView;
   final static int IMG_FOUND=1;
   private FirebaseAuth mAuth;
   Intent login,welcome;
   FirebaseStorage storage;
   FirebaseUser user;
   String myName;
   String date,time;
   String subfolderName=null,selectedItem_fromDropdown;
   ProgressDialog progressDialog;
   private Handler handler=new Handler();
   private Spinner dropdown;
   protected List<String> stringList=new ArrayList<>();//create a list to add it into sharedPref and adapter of dropdownList
   SharedPreferences sharedPreferences;
   Set<String> stringSet,retrievedStringSet;//for storing in sharedpref
   protected String downloadURL;
   String imageName;
   private DatabaseReference linkedUsersRef,linkPersonDBRef;
   private HashMap<String,String> userMapISA,email_UIDMapISA;//to get hashmaps from WelActivity i have used intent Extras

    ChildEventListener subfolderListener=new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            System.out.println(snapshot.getKey());
            String[] split=snapshot.getKey().split("AND");
            if(split[0].equals(myName) || split[1].equals(myName)){
                System.out.println("yesss");
                //linkedUsersRef.child(snapshot.getKey()).child("subfolders").push().setValue("sub1");
                System.out.println(snapshot.child("subfolders").getKey());

            }

            System.out.println(split[0]+"\n"+split[1]);

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

    //choose an image from gallery
    public void getImage(View view){
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMG_FOUND);
        }else{
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},IMG_FOUND);
        }

    }

    //upload image to firebase
    public void uploadImage(View view){

        imageName="IMG_"+date+"_"+time;

        if(imageView.getDrawable()!=null){
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            Bitmap bitmap=((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
            byte[] data=baos.toByteArray();
            final UploadTask uploadTask;

            //create storage refs to get download url
            final StorageReference ref;


            //create a subfolder String for final upload in it
            String uploadSubFolder;
            if(selectedItem_fromDropdown == null || selectedItem_fromDropdown.equals("Select an Item")){
                ref=storage.getReference().child(myName.trim()).child(imageName);
                uploadSubFolder="Nothing";
                uploadTask=ref.putBytes(data);
                System.out.println(uploadSubFolder);

            }
            else{
                uploadSubFolder="/"+selectedItem_fromDropdown;
                //actual upload
                //every image has different name using name as date and time
                ref=storage.getReference().child(myName.trim()).child(uploadSubFolder).child(imageName);
                uploadTask=ref.putBytes(data);
                //folder name is myName=user's display name
                System.out.println(uploadSubFolder);

            }

            //check uploadSubFOlder
            System.out.println("final SubFolder---"+uploadSubFolder);

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress =(100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    int currentProgress=(int) progress;
                    showProgressBarWithTitle(currentProgress);

                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    showProgressBarWithTitle(100);

                    //to get download url
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadURL=uri.toString();
                            System.out.println("downloadUrl="+downloadURL);

                            //store url in database
                            FirebaseDatabase.getInstance().getReference().child("urls").child("url1").setValue(downloadURL);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("error---"+e.getMessage());
                        }
                    });

                    //run this code after 1sec
                    //dismiss dialog after 1 sec
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Uploaded Successfully",Toast.LENGTH_LONG).show();
                            hideProgressDialog();
                        }
                    },1000);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"Upload Failed\n"+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });

        }
        else{
            //set an alertDialog if no image is selected
            AlertDialog alert=new AlertDialog.Builder(ImageSelectorActivity.this)
                    .setTitle("Warning")
                    .setIcon(R.drawable.ic_baseline_warning_24)
                    .setMessage("Image Not Selected\nPls Select an Image")
                    .setPositiveButton("OK",null)
                    .show();
            alert.setCanceledOnTouchOutside(false);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selector);
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},IMG_FOUND);

        //assigning firebaseAuth
        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        assert user != null;
        myName=user.getDisplayName();
        System.out.println("display name: "+myName);

        //Snackbar
        Snackbar snackbar=Snackbar.make(findViewById(R.id.ImageSelectorActivityLayout),"Welcome "+myName,Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundResource(R.drawable.round_corners);
        snackbar.setTextColor(getResources().getColor(android.R.color.black,null));

        Snackbar.SnackbarLayout snackbarLayout=(Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setMinimumHeight(150);
        snackbar.show();

        //init WelAc
        WelcomeActivity welcomeActivity=new WelcomeActivity();
        //initial both maps and give value from WelAc
        userMapISA=(HashMap<String, String>) getIntent().getSerializableExtra("userMap");
        email_UIDMapISA=(HashMap<String, String>) getIntent().getSerializableExtra("emailUIDMap");
        System.out.println(userMapISA);
        System.out.println(email_UIDMapISA);

        //initialize storage
        storage=FirebaseStorage.getInstance();
        //initializing DB Ref to linkedUsers
        linkedUsersRef=FirebaseDatabase.getInstance().getReference().child("linkedUsers");


        //initialize imageView
        imageView=findViewById(R.id.imageView);

        //get current date in string
        date=new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        System.out.println("Today's Date= "+date);
        //current time
        time=new SimpleDateFormat("HHmmss",Locale.getDefault()).format(new Date());

        //create a new progress dialog
        progressDialog=new ProgressDialog(ImageSelectorActivity.this);

        //create dropdown list using spinner
        dropdown=findViewById(R.id.spinner);

        //intents
        login=new Intent(getApplicationContext(),LoginActivity.class);//for signOut
        welcome=new Intent(getApplicationContext(),WelcomeActivity.class);//to go back to start

        //toolbar
        Toolbar toolbar=findViewById(R.id.selector_toolbar);
        this.setSupportActionBar(toolbar);

        //back button action
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(welcome);
                finish();
            }
        });

        //toobar menu actions
        //always use a toolbar MenuItemClicked method when using menu in toolbar
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){

                    case R.id.signOut: {
                        //for signOut menu item
                        mAuth.signOut();
                        Toast.makeText(getApplicationContext(), "Logged Out successfully", Toast.LENGTH_LONG).show();
                        startActivity(login);
                        finish();
                        return true;
                    }

                    case R.id.del:{
                        //call delete function from welcomeActivity
                        new WelcomeActivity().deleteAcc(ImageSelectorActivity.this);
                        return true;
                    }
                    default:
                        return false;

                }
            }
        });


        //rotate image when imageview clicked
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.animate().rotationBy(90).setDuration(500);
            }
        });

        //set Image to original position
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog alertDialog=new AlertDialog.Builder(ImageSelectorActivity.this)
                        .setTitle("RESET IMAGE")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                imageView.animate().rotation(0).setDuration(500);
                            }
                        })
                        .setNegativeButton("CANCEL",null)
                        .show();
                return false;
            }
        });

        //call sharedpref to initialize
        sharedPreferences=ImageSelectorActivity.this.getSharedPreferences("com.AScode.photobox", Context.MODE_PRIVATE);
        //CharSequence set to save in sharedPref
        stringSet = new HashSet<>(stringList);
        //get back the string set saved in sharedPref and add it to the arrayList of strings
        retrievedStringSet=sharedPreferences.getStringSet("SubFolderList",null);    //null is for the default value of stringSet
        System.out.println("retrievedStringSet in onCreate---"+retrievedStringSet);
        if(retrievedStringSet!=null){
            stringList.addAll(retrievedStringSet);
            Collections.reverse(stringList);
            System.out.println("stringList in OnCreate---"+stringList);
        }

        if(stringList.isEmpty())
            stringList.add("Select an Item");

        //set Adapter for dropdown
        ArrayAdapter<String> stringArrayAdapter=new ArrayAdapter<>(ImageSelectorActivity.this,R.layout.spinner_layout,stringList);
        dropdown.setAdapter(stringArrayAdapter);

        //set onItemSelectedListener for dropdown
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedItem_fromDropdown=dropdown.getItemAtPosition(i).toString();
                System.out.println("selected item= "+selectedItem_fromDropdown);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedItem_fromDropdown=null;
                Toast.makeText(ImageSelectorActivity.this,"No Item Is selected",Toast.LENGTH_SHORT).show();
            }
        });

        //set subFolder from DB
        linkedUsersRef.addChildEventListener(subfolderListener);

    }

    //method for setting subFolderList and sharedPref
    protected void setSubfolder(){
        //add into the string List and check if element we get form the dialog gets added
        stringList.add(subfolderName);
        System.out.println("inside setSubfolderName---" + subfolderName);
        System.out.println("updated list= " + stringList);

        //add the stringSet into sharedPref
        stringSet.addAll(stringList);
        sharedPreferences.edit().putStringSet("SubFolderList",stringSet).apply();
        System.out.println("stored Set---"+stringSet);
    }

    //for creating subFolder
    //create a method which opens a dialog to enter subFolderName and return the name
    public void createSubFolder(View view){

        View subFolderDialogView= getLayoutInflater().inflate(R.layout.create_sub_folder_dialog,null);
        //create editText obj from dialog
        final EditText subFolderText=subFolderDialogView.findViewById(R.id.createSubfolderText);
        //create dialog
        final AlertDialog subDialog=new AlertDialog.Builder(ImageSelectorActivity.this)
                .setTitle("Create A SubFolder")
                .setView(subFolderDialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        subfolderName=subFolderText.getText().toString().trim();
                        //extra conditions
                        if(!subfolderName.isEmpty()) {
                            setSubfolder();
                        }else {
                            Toast.makeText(ImageSelectorActivity.this, "No Folder Name Entered", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("CANCEL",null)
                .show();
        subDialog.setCanceledOnTouchOutside(false);

    }

    //method to show progressBar
    private void showProgressBarWithTitle(int currentProgress){
        progressDialog.setTitle("Image Uploading...");
        progressDialog.setMessage("Please wait your Image is uploading");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.show();
        progressDialog.setProgress(currentProgress);

    }

    //dismiss the progressDialog
    private void hideProgressDialog(){
        progressDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==IMG_FOUND && resultCode == RESULT_OK && data != null && data.getData() != null){
            try {
                Uri selectedImage=data.getData();
                Bitmap bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                imageView.setImageBitmap(bitmap);
                imageView.setBackgroundColor(Color.WHITE);
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    //create menu in activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);

        menu.findItem(R.id.notify).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

}
