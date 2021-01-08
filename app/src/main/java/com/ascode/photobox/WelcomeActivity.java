package com.ascode.photobox;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ascode.photobox.models.LinkModel;
import com.ascode.photobox.models.User;
import com.ascode.photobox.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    String myName,linkedName="",selectedName,selectedEmail;
    Intent login,imgSelecter,imgViewer;
    AlertDialog dialog;
    private TextView linkText,showLinkedPerson;
    protected EditText searchNameEditText;
    Button request;
    ArrayList<String> nameArrayList=new ArrayList<>();
    ArrayList<String> nameArrayListClone=new ArrayList<>();
    ArrayList<User> allUsers=new ArrayList<>();
    ArrayAdapter<String> nameArrayAdapter;
    ListView searchListView;
    protected HashMap<String,String> userMap=new HashMap<>();//map to store username and email---name=email
    protected HashMap<String,String> email_uidMap=new HashMap<>();//map to store email and uid---email=UID
    Menu myMenu;//to access menu in this activity
    DatabaseReference userRef;
    boolean checkMes=false;
    String fromName,fromEmail;
    Button viewBtn,uploadImgBtn;
    boolean alreadylinked=false,linkedFolderCreated=false;// to check if the person is already linked to someone else
    //ProgressDialog loadingDiaog;
    AlertDialog loaderDialog;
    private final String error="To View or Upload Images you have to link to a person";
    boolean isNetworkAvailable=true;

    //ChildEvent listener for firebase Database
    ChildEventListener childEventListener=new ChildEventListener() {    //called in TextWatcher which is called in onCreate
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            //runs when new data Child is added into users
            String getName=snapshot.child("displayName").getValue().toString();
            String getEmail=snapshot.child("email").getValue().toString();
            userMap.put(getName,getEmail);//email,name
            email_uidMap.put(getEmail,snapshot.getKey());
            System.out.println("map value in onChildAdded---"+userMap);
            System.out.println("UIDmap value in onChildAdded---"+email_uidMap);

            if(!nameArrayList.contains(getName) && !getName.equals(myName)){
                nameArrayList.add(getName);
            }
            nameArrayAdapter.notifyDataSetChanged();
            System.out.println("onChildAdded---"+nameArrayList);

        }
        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {        }
        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {        }

    };

    //textwatcher to execute the code when text is typed into editText
    TextWatcher textWatcher=new TextWatcher() {   //called in onCreate
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            System.out.println(charSequence);//just to check
            if(charSequence.length()>0){
                //nameArrayList is already filled up in onCreate so no need to call it here
                //clear cloned list before starting
                nameArrayListClone.clear();
                int c;
                boolean isPresent = false;

                System.out.println("total size of nameArrayList= "+nameArrayList.size());//just to check size
                for(c=0;c<nameArrayList.size();c++){
                    //to check the arrayList element by element if it contains input charSequence
                    String listNames=nameArrayList.get(c);
                    System.out.println(c+" : "+nameArrayList.get(c));

                    //first convert to lowercase then check each value of nameArrayList if it contains charSequence
                    if(nameArrayList.get(c).toLowerCase().contains(charSequence)){

                        if( !nameArrayListClone.contains(listNames) )
                            nameArrayListClone.add(listNames);

                        isPresent=true;
                        System.out.println("arrayList contains this "+isPresent);
                    }
                }
                System.out.println("cloned List= "+nameArrayListClone);
                if(isPresent)
                    searchListView.setAdapter(nameArrayAdapter);
                else{
                    searchListView.setAdapter(null);
                    System.out.println("NO matches found");
                }

            }
            else if(charSequence.length()==0){
                searchListView.setAdapter(null);
                System.out.println("adapter not set");
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

            searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    selectedName=searchListView.getItemAtPosition(i).toString();
                    System.out.println("item selected---"+selectedName);
                    selectedEmail=userMap.get(selectedName);
                    System.out.println("email of item selected---"+selectedEmail);

                    //creating link dialog
                    linkDialog(WelcomeActivity.this,selectedName,selectedEmail);

                }
            });
        }
    };

    //splash dialog
    private void welcomeDialog(){

        //view for splash dialog
        LayoutInflater layoutInflater=getLayoutInflater();
        View dialogView=layoutInflater.inflate(R.layout.welcome_dialog,null);

        dialog=new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        //view textView
        TextView viewText=dialogView.findViewById(R.id.customTitle);
        viewText.setText("Welcome\n" +myName);

        //animation for dialog box
        if(dialog.getWindow() != null){
            dialog.getWindow().getAttributes().windowAnimations=R.style.dialogAnimation;  //view animation in styles.xml>>dialogAnimation
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        //dialog.getWindow().setLayout(600,500);

        //dismiss dialog after 2sec
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        },2000);

    }

    //dialog for deleting
    protected void deleteAcc(final Context context){
        AlertDialog delDialog=new AlertDialog.Builder(context)
                .setTitle("Delete Account")
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setMessage("Are you sure you want to delete this Account?\n" +
                        "You will lose all your data after deleting\n" +
                        "However your linked person can still access all photos")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();

                        showLoading(1,"Deleting account please wait..."); //start loading

                        if(firebaseUser != null){
                            firebaseUser.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                userRef.child(firebaseUser.getUid()).child("delete").setValue(null);
                                                Toast.makeText(context,"Account Successfully Deleted",Toast.LENGTH_SHORT).show();
                                                showLoading(0,null); //stop loading
                                                startActivity(login);
                                            }else{
                                                showLoading(0,null); //stop loading
                                                Toast.makeText(context,"Precess Failed\nLog IN again before Rquesting Again",Toast.LENGTH_LONG).show();
                                                if(task.getException() != null) //null check
                                                    System.out.println("failure reason---"+task.getException().toString());
                                            }
                                        }
                                    });
                        }
                    }
                })
                .setNegativeButton("CANCEL",null)
                .show();
        delDialog.setCanceledOnTouchOutside(false);
    }

    //dialog for confirming and selecting link person
    private void linkDialog(Context context, final String Name, String Email){
        View sub_item_list=getLayoutInflater().inflate(R.layout.sub_item_list,null);
        //find textviews from the view
        TextView confirmName=sub_item_list.findViewById(R.id.mainListItem);
        TextView confirmEmail=sub_item_list.findViewById(R.id.subListItem);

        //set text in those textviews
        confirmName.setText(Name);
        confirmEmail.setText(Email);

        //create an alertDialog to confirm link person
        AlertDialog linkDialog=new AlertDialog.Builder(context,R.style.linkDialog)
                .setView(sub_item_list)
                .setTitle("DETAILS")
                .setPositiveButton("SELECT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        searchNameEditText.setText(Name);
                    }
                })
                .setNegativeButton("CANCEL",null)
                .show();
        linkDialog.setCanceledOnTouchOutside(false);
        linkDialog.getButton(AlertDialog.BUTTON_POSITIVE);
    }

    //to show loading whenever needed
    protected void showLoading(int code, String msg){
        /*loadingDiaog.setProgressStyle(android.R.style.Widget_DeviceDefault_ProgressBar);
        loadingDiaog.setMessage("Sending Request..");
        loadingDiaog.setCancelable(false);
        loadingDiaog.setCanceledOnTouchOutside(false);
        if(code == 1){
            loadingDiaog.show();
        }
        if(code==0){
            loadingDiaog.dismiss();
        }*/

        /*RelativeLayout welcomeLoader= findViewById(R.id.welcomeLoader);
        TextView textView=welcomeLoader.findViewById(R.id.loaderTextView);
        textView.setText(msg);

        welcomeLoader.setAlpha(code);*/
        if(loaderDialog != null){
            if(loaderDialog.isShowing()){
                loaderDialog.dismiss();
                return;
            }

        }

        //2nd approach
        View loader= getLayoutInflater().inflate(R.layout.custom_loader,null);
        loaderDialog= new AlertDialog.Builder(this)
                .setView(loader)
                .create();

        TextView text= loader.findViewById(R.id.loaderTextView);
        text.setText(msg);

        if(loaderDialog.getWindow()!=null){
            loaderDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        loaderDialog.setCancelable(false);
        loaderDialog.setCanceledOnTouchOutside(false);
        if(code==1)
            loaderDialog.show();
        else{
            loaderDialog.dismiss();
            System.out.println("loader dismiss");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        setContentView(R.layout.activity_welcome);

        //all views
        linkText=findViewById(R.id.linkTxt);
        searchNameEditText=findViewById(R.id.searchName);
        request=findViewById(R.id.requestBtn);
        showLinkedPerson=findViewById(R.id.showLikedPerson);
        searchListView=findViewById(R.id.searchListView);
        viewBtn=findViewById(R.id.viewBtn);
        uploadImgBtn=findViewById(R.id.uploadImg);

        //stop loading
        //showLoading(0,null);

        //check internet connection
        if(isConnected(WelcomeActivity.this)){
            //net connected
            Toast.makeText(WelcomeActivity.this,"Connected",Toast.LENGTH_SHORT).show();
        }
        else{
            //not connected
            Toast.makeText(WelcomeActivity.this,"Not Connected",Toast.LENGTH_LONG).show();

            isNetworkAvailable=false;

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
            viewBtn.setOnClickListener(null);
            uploadImgBtn.setOnClickListener(null);
            linkText.setOnClickListener(null);
        }

        //for testing
        allUsers= new UserModel().getAllUsers();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("user list "+allUsers);
            }
        },3000);

        //databaseRef
        userRef=FirebaseDatabase.getInstance().getReference().child("users");
        //firebase auth
        mAuth=FirebaseAuth.getInstance();
        //firebase user
        user = mAuth.getCurrentUser();

        //intents
        login=new Intent(getApplicationContext(),LoginActivity.class);
        imgSelecter=new Intent(getApplicationContext(),ImageSelectorActivity.class);
        imgViewer=new Intent(getApplicationContext(),ImageViewerActivity.class);

        //loading dialog
        //loadingDiaog=new ProgressDialog(WelcomeActivity.this);

        //getting current user display name
        if(user!=null)
            myName= user.getDisplayName();

        //myName could be null if directly getting from signIn
        if(myName == null)
            myName=getIntent().getStringExtra("nameFromSignUp");

        ActionBar bar=getSupportActionBar();
        if(bar!=null)
            bar.setTitle("Welcome "+myName);

        //show dialog
        welcomeDialog();

        nameArrayAdapter=new ArrayAdapter<>(WelcomeActivity.this,R.layout.mylist,nameArrayListClone);//to adapt single item listView

        //showLinkedPerson.setMovementMethod(new ScrollingMovementMethod());// to scroll textView in scrollView
        showLinkedPerson.setText("You are Linked to: "+linkedName);

        //to make request section invisible in the start
        searchNameEditText.setVisibility(View.GONE);
        request.setVisibility(View.GONE);
        searchListView.setVisibility(View.GONE);
        linkText.setAlpha(0);

        //to check if my request is accepted
        isRequestAccepted();

        //to set text if linked
        checkIfLinked();

        //execute this code after 5sec so that linkedName is displayed till then
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /*String[] split=showLinkedPerson.getText().toString().split(":");
                linkedName=split[1];*/
                System.out.println("linkedName is "+linkedName+" length of linkedName="+linkedName.length());

                linkText.setAlpha(1);
                //add onClickListener for link TextView to make request section visible
                if(isNetworkAvailable) {
                    linkText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (linkedName.isEmpty()) {
                                searchNameEditText.setText(null);
                                searchNameEditText.setVisibility(View.VISIBLE);
                                request.setVisibility(View.VISIBLE);
                                searchListView.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(WelcomeActivity.this, "You are Already Linked To a Person", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        },3000);

        //Listener when text is changed
        searchNameEditText.addTextChangedListener(textWatcher);

        //to check if I got any request
        System.out.println(checkRequest());

        //to fill the email_uidMap
        userRef.addChildEventListener(childEventListener);


        //to start animation afer 2 sec after welcomeDialog is dismissed
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //to animate buttons
                viewBtn.setTranslationX(1500);
                viewBtn.animate().translationX(0).setDuration(1500);
                uploadImgBtn.setTranslationX(-1500);
                uploadImgBtn.animate().translationX(0).setDuration(1500);
            }
        },2000);

        //to test db
        System.out.println("dbRef = "+FirebaseDatabase.getInstance().getReference().child("test1").child("test11").setValue("yes"));
        FirebaseDatabase.getInstance().getReference().child("test").setValue("hello").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                System.out.println("task completed: "+task );

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("task failed: "+e.getMessage());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);

        //hide notification icon
        menu.findItem(R.id.notify).setVisible(false);
        menu.findItem(R.id.notifyAccept).setVisible(false);
        myMenu=menu;
        return super.onCreateOptionsMenu(menu);
    }

    //use this to select menu options when menu is directly from app and not from toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.signOut:{
                //if signOut option is selected from menu
                signOutDialog(WelcomeActivity.this);
                return true;
            }
            case R.id.del:{
                deleteAcc(WelcomeActivity.this);
                return true;
            }
            case R.id.notify:{
                linkInfo(WelcomeActivity.this);
                return true;
            }
            case R.id.notifyAccept:{
                Toast.makeText(WelcomeActivity.this,"Link Request Accepted by "+linkedName,Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.reload:{
                recreate();
                return true;
            }
            case R.id.unlink:{
                if(!linkedName.isEmpty())
                    showUnlinkDialog(WelcomeActivity.this);
                else{
                    Toast.makeText(this,"You are not linked to anyone",Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            default: return super.onOptionsItemSelected(item);
        }
    }

    //to signout
    private void signOutDialog(Context context){
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Confirm Sign Out")
                .setMessage("Do you want to sign out?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        Toast.makeText(getApplicationContext(), "Logged Out successfully", Toast.LENGTH_LONG).show();
                        startActivity(login);
                        finish();
                    }
                })
                .setNegativeButton("NO", null)
                .setNeutralButton("CANCEL",null)
                .show();

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
    }

    //view button action
    public void viewer(View view){
        if(linkedName.length() >1){
            imgViewer.putExtra("myName",myName);
            startActivity(imgViewer);
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }
        else{
            System.out.println("not linked");
            Toast.makeText(WelcomeActivity.this,error,Toast.LENGTH_LONG).show();
        }

    }

    //upload button action
    public void selecter(View view){

        if(linkedName.length()>1){
            imgSelecter.putExtra("userMap",userMap);
            imgSelecter.putExtra("emailUIDMap",email_uidMap);
            imgSelecter.putExtra("myNameFromIntent",myName);
            startActivity(imgSelecter);
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }
        else{
            System.out.println("not linked");
            Toast.makeText(WelcomeActivity.this,error,Toast.LENGTH_LONG).show();
        }

    }

    //to check is particular user is already linked
    private void isAlreadyLinked(){
        String linkUID=email_uidMap.get(selectedEmail);  //to get UID of that person by using selectedEmail
        System.out.println("linkUID is "+linkUID);
        if(linkUID != null){

            userRef.child(linkUID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot data: snapshot.getChildren()){
                        if(data.getKey() != null){ // null check
                            System.out.println("data keys= "+data.getKey());
                            if(data.getKey().equals("linkedTo") || data.getKey().equals("RequestFrom") ){
                                // if that person's DB has "linkedTo" or "RequestFrom" then he is already linked or has requested
                                alreadylinked=true;
                                System.out.println("user is already  linked(in checking progress)---"+alreadylinked);
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });

        }
        else{
            System.out.println("linkUID not found");
        }
    }

    //when request button is clicked---sender
    public void linkRequest(View view){     //to send request to selected person
        final String getNameFromSearch=searchNameEditText.getText().toString();//get text from searchEditText
        isAlreadyLinked();
        //start loading
        showLoading(1,"Sending Request...");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("before calling method---"+alreadylinked);
                showLoading(0,null);
                //first check if that person is already linked or requested
                if( alreadylinked ){
                    System.out.println("in send request---"+alreadylinked);
                    System.out.println("that person is already linkedTo someone else");
                    Toast.makeText(WelcomeActivity.this,"Cannot send request to "+selectedName+" \nThis person is already linked to someone else",Toast.LENGTH_SHORT).show();
                    alreadylinked=false;// reset alreadyLinked so that user can try to link others
                }
                else{
                    if(nameArrayList.contains(getNameFromSearch)) {
                        DatabaseReference toLinkPerson=userRef.child(Objects.requireNonNull(email_uidMap.get(selectedEmail)));
                        //creating a map for this will delete the existing data and override that with this info
                        toLinkPerson.child("RequestFrom").setValue(myName);//add from whom the request is(name)
                        toLinkPerson.child("RequestFromEmail").setValue(userMap.get(myName));//add from whom the request is(email)

                        //to check
                        System.out.println("message sent to: "+userMap.get(selectedName));

                        //also in my DB write that i have sent a linkRequest to this person
                        userRef.child(user.getUid()).child("Link Request sent to").setValue(selectedEmail);

                        //show a dialog giving message "request sent"
                        AlertDialog alertDialog=new AlertDialog.Builder(WelcomeActivity.this)
                                .setTitle("SUCCESS")
                                .setMessage("Request sent Successfully to "+selectedName)
                                .setPositiveButton("OK",null)
                                .setCancelable(false)
                                .show();
                        alertDialog.setCanceledOnTouchOutside(false);

                        searchListView.setAdapter(null);
                        searchNameEditText.setText(null);
                        searchListView.setVisibility(View.GONE);
                        searchNameEditText.setVisibility(View.GONE);
                        request.setVisibility(View.GONE);
                    }
                    else{
                        Toast.makeText(getApplicationContext(),getNameFromSearch+" not Found!!",Toast.LENGTH_SHORT).show();
                    }
                }

            }
        },2000);

    }

    //check if message is present in reciever's DB---receiver
    private boolean checkRequest(){

        System.out.println("before listener="+checkMes);//to check
        //to check if RequestFrom is present in my user's child
        userRef.child(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                System.out.println("keys of child---"+snapshot.getKey());
                if(Objects.equals(snapshot.getKey(), "RequestFrom")){
                    checkMes=true;
                    System.out.println(checkMes);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        });
        System.out.println("checkMes after listener="+checkMes);
        return checkMes;

    }

    //to show alert dialog, which has info of requested , to reciever
    private void linkInfo(Context context){

        AlertDialog linkInfoDialog=new AlertDialog.Builder(context)
                .setTitle("LINK REQUEST")
                .setMessage("Name: "+fromName+"\nEmail: "+fromEmail)
                .setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(!linkedFolderCreated)
                            FirebaseDatabase.getInstance().getReference().child("linkedUsers").child(myName+"AND"+fromName).setValue("linked");//add a new DB linkedUsers with myNameANDLinkedName

                        userRef.child(user.getUid()).child("linkedTo").setValue(fromName);//add to my DB that i am linked to fromName
                        userRef.child(Objects.requireNonNull(email_uidMap.get(fromEmail))).child("linkedTo").setValue(myName);//also write in fromName's DB that he/she is linked to me
                        System.out.println("added to DB");//to check if it worked

                        linkedName=fromName;
                        showLinkedPerson.setText("You are Linked to: "+linkedName);

                    }
                })
                .setNegativeButton("CANCEL",null)
                .setNeutralButton("IGNORE",null)
                .setCancelable(false)
                .show();
        linkInfoDialog.setCanceledOnTouchOutside(false);
    }

    //to find is my request is accepted---this method is for sender
    private void isRequestAccepted(){

        //To check if i have sent any request AND i am linked to that person
        //link request sent is recorded while sending request
        //when receiver accepts the request a linked to key is set up in both DB
        userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //create a hashmap by taking value from snapshot

                @SuppressWarnings("unchecked")
                HashMap<String,String> requestAcceptMap=(HashMap<String, String>) snapshot.getValue();
                System.out.println("in request accepted\t"+snapshot.getValue());

                if(requestAcceptMap != null && requestAcceptMap.get("linkedTo") != null && requestAcceptMap.get("Link Request sent to") != null && linkedName.isEmpty()){
                    //yes i have sent a request AND my request is accepted
                    System.out.println("My request is accepted and i am linked to "+requestAcceptMap.get("linkedTo"));

                    if(myMenu != null)  //to avoid null exception
                        myMenu.findItem(R.id.notifyAccept).setVisible(true);//show menu item of notification

                    showLinkedPerson.setText(String.format("You are Linked to: %s", requestAcceptMap.get("linkedTo")));
                }

                if(snapshot.getKey() != null)  //null check
                    System.out.println("check if request is sent by me=="+snapshot.getKey().equals("Link Request sent to"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        });

    }

    //to check if my DB has "linkedTo" key
    private void checkIfLinked(){
        System.out.println("inside checking");
        //display linked to in upper textView
        userRef.child(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                System.out.println("snapshots are: "+snapshot.toString());
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
        try {
            userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    System.out.println("inside onDataChange of checking");
                    for(DataSnapshot data: snapshot.getChildren()){

                        if(data.getKey() != null && data.getValue() != null){ //null check
                            //check if linkedTo is present in my DB
                            if(data.getKey().equals("linkedTo")){
                                String displayLinkName= data.getValue().toString();
                                System.out.println("in checkIfLinked valueEventListener---"+displayLinkName);
                                showLinkedPerson.setText(String.format("You are linked To: %s",displayLinkName));
                                linkedName=displayLinkName;
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //to check if I got any request
                //run this after 5sec so that firebase database gets loaded in around 4sec
                if(checkMes && linkedName.isEmpty()){
                    System.out.println("checkRequest---message is present");
                        myMenu.findItem(R.id.notify).setVisible(true);
                }
                else{
                    System.out.println("request not found");
                }
            }
        },4000);

        userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                @SuppressWarnings("unchecked")
                HashMap<String,String> linkMap=(HashMap<String, String>) snapshot.getValue();
                if(linkMap!=null){  //to avoid nullPointer Exception
                    System.out.println("in linkInfo---"+linkMap);
                    System.out.println(linkMap.get("displayName"));
                    fromName=linkMap.get("RequestFrom");
                    fromEmail=linkMap.get("RequestFromEmail");
                    System.out.println("fromName="+fromName+"\tfromEmail="+fromEmail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    //to check network available
    protected boolean isConnected(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo mobile=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(wifi != null && mobile != null){//null check
            //mobile data
            //no internet
            if(wifi.isConnected()){
                //wifi conneced
                return true;
            }
            else return mobile.isConnected();
        }else{
            return false;
        }

    }

    //to show unlink dialog
    private void showUnlinkDialog(Context context){

        String linkuid = null;
        Log.d("unlink", allUsers.toString());
        Log.d("unlink", "linkedName="+linkedName);

        for(User user: allUsers){
            if(user.getDisplayName().equals(linkedName)){
                linkuid=user.getUid();
            }
        }

        final String finalLinkuid = linkuid;
        Log.d("unlink", finalLinkuid+"--"+linkuid);
        AlertDialog unlinkDialog= new AlertDialog.Builder(context)
                .setTitle("Confirm Unlink")
                .setMessage("Are you sure you want to delete link with "+linkedName+
                        "\nBoth persons will not be able to see your photos" +
                        "\nTo view your photos you need to link again")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("unlink","myuid="+user.getUid()+"\n" +
                                "linkPerson uid="+ finalLinkuid);
                        LinkModel.Companion.unlink(user.getUid(),finalLinkuid);


                    }
                })
                .setNegativeButton("NO",null)
                .setNeutralButton("CANCEL",null)
                .show();
        unlinkDialog.setCanceledOnTouchOutside(false);
        unlinkDialog.setCancelable(false);
    }

    //to check if my linkedfolder is created
    private void isMyLinkedFolderCreated(){
        FirebaseDatabase.getInstance().getReference().child("linkedUsers")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot data : snapshot.getChildren()){
                            //Log.d("links", data.getKey());
                            String[] split=data.getKey().split("AND");
                            if(split[0].equals(myName) || split[1].equals(myName)){
                                Log.d("links", data.getKey());
                                linkedFolderCreated=true;
                            }
                            /*if(data.getKey().contains(myName)){
                                Log.d("links", data.getKey());
                                linkedFolderCreated=true;
                            }*/
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        isMyLinkedFolderCreated();
    }
}