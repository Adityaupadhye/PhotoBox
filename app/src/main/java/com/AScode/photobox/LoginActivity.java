package com.AScode.photobox;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.*;

public class LoginActivity extends AppCompatActivity {

    EditText emailText,pswdText,newPinText,confirmPinText,pinText;
    String email,pswd;
    String enteredPin,finalPin,newPIN,confirmPIN;
    SharedPreferences preferences;
    private FirebaseAuth mAuth;
    TextView signUp;
    Intent welcome,sign;
    private GoogleSignInClient mGoogleSignInClient;
    final static int RC_SIGN_IN=2;
    ProgressDialog load; //to show loading
    DatabaseReference userDB;

    //Login button code
    public void userLogin(View view){

        email=emailText.getText().toString();
        pswd=pswdText.getText().toString();


        if(email.isEmpty())
            Toast.makeText(getApplicationContext(),"Please enter Email",Toast.LENGTH_LONG).show();
        else if(pswd.isEmpty())
            Toast.makeText(getApplicationContext(),"Please enter Password",Toast.LENGTH_LONG).show();
        else{
            loading(1); // start loading
            //signIN
            mAuth.signInWithEmailAndPassword(email,pswd)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                loading(0); //dismiss
                                //sign in success
                                Log.d("sign in: ","successful");
                                Toast.makeText(getApplicationContext(),"signed in succcessfully",Toast.LENGTH_SHORT).show();
                                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                                assert user != null;
                                System.out.println("display name: (inside LoginActivity userLogin)"+user.getDisplayName());
                                updateUI(user);
                            }else{
                                //sign in unsuccessful
                                Log.w("sign in prob:",task.getException());
                                loading(0); //dismiss
                                Toast.makeText(getApplicationContext(),"Authentication failed\nIncorrect Password",Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        }
                    });
        }

    }

    //App PIN Dialog box
    protected void securityDialog(){
        //get layout created
        LayoutInflater inflater=getLayoutInflater();
        final View view=inflater.inflate(R.layout.dialog,null);
        final AlertDialog dialog=new AlertDialog.Builder(this,R.style.alertDialog)
                .setTitle("SECURITY")
                .setMessage("Enter 4-digit App PIN\nRESET PIN if not created OR Forgot")
                .setPositiveButton("OK",null)
                .setNegativeButton("EXIT",null)
                .setNeutralButton("RESET PIN",null)
                .setView(view)
                .show();
        dialog.setCanceledOnTouchOutside(false);

        //set positiveButton
        Button positiveButton=dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pinText=view.findViewById(R.id.pin);//find EditText pin from view
                enteredPin=pinText.getText().toString();
                System.out.println("click pin="+finalPin);
                if(enteredPin.equals(finalPin)){
                    Toast.makeText(getApplicationContext(),"Correct PIN",Toast.LENGTH_SHORT).show();
                    System.out.println("PIN Entered is correct");
                    dialog.dismiss();

                    // Check if user is signed in (non-null) and update UI accordingly.
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    updateUI(currentUser);

                }else{
                    pinText.setText(null);
                    Toast.makeText(getApplicationContext(),"Incorrect PIN!  Try Again",Toast.LENGTH_SHORT).show();
                }

            }
        });

        //set NegativeButton
        Button negativeButton=dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);

            }
        });

        //set NeutralButton
        Button neutralButton=dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                resetPinDialog();
            }
        });

        dialog.setCancelable(false);

    }

    //reset PIN Dialog
    protected void resetPinDialog(){
        LayoutInflater inflater=getLayoutInflater();
        final View view=inflater.inflate(R.layout.reset_pin_dialog,null);
        final AlertDialog alertDialog=new AlertDialog.Builder(this,R.style.alertDialog)
                .setTitle("Set App PIN")
                .setMessage("Enter a 4-digit PIN")
                .setPositiveButton("DONE",null)
                .setNegativeButton("Cancel",null)
                .setNeutralButton("BACK",null)
                .setView(view)
                .show();
        alertDialog.setCanceledOnTouchOutside(false);

        //set positiveButton
        Button positiveButton=alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newPinText=view.findViewById(R.id.newPin);//find EditText newPin from dialog view
                confirmPinText=view.findViewById(R.id.ConfirmPin);//find EditText confirmPin from dialog view
                newPIN=newPinText.getText().toString();
                confirmPIN=confirmPinText.getText().toString();
                //check if newPin and ConfirmPin are equal
                if(newPIN.equals(confirmPIN) && !newPIN.isEmpty()){
                    Toast.makeText(getApplicationContext(),"PIN reset Successfully",Toast.LENGTH_SHORT).show();
                    //finalPin=newPIN;
                   preferences=getApplicationContext().getSharedPreferences("com.AScode.photobox",Context.MODE_PRIVATE);
                   preferences.edit().putString("pin",newPIN).apply();
                   finalPin=preferences.getString("pin","0000");
                    System.out.println("final pin="+finalPin);

                    securityDialog();
                    alertDialog.dismiss();
                }
                else{
                    Toast.makeText(getApplicationContext(),"PIN not matching",Toast.LENGTH_SHORT).show();
                    newPinText.setText(null);
                    confirmPinText.setText(null);
                    newPinText.requestFocus();
                }
            }
        });

        //set negativeButton
        Button negativeButton=alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        //set neutralButton
        Button neutralButton=alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                securityDialog();
            }
        });

        alertDialog.setCancelable(false);

    }

    //login using google (starts activity for google sign in)
    public void googleLogin(View view){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    protected void loading(int code){
        load.setProgressStyle(android.R.style.Widget_DeviceDefault_ProgressBar);
        load.setMessage("Signing In please wait..");
        load.setCancelable(false);
        load.setCanceledOnTouchOutside(false);
        if(code==1)
            load.show();
        else if(code == 0)
            load.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //firebaseAuth
        mAuth=FirebaseAuth.getInstance();

        //user DB
        userDB=FirebaseDatabase.getInstance().getReference().child("users");

        emailText =findViewById(R.id.emailText);
        pswdText=findViewById(R.id.pswdText);

        preferences=getApplicationContext().getSharedPreferences("com.AScode.photobox",Context.MODE_PRIVATE);
        finalPin=preferences.getString("pin","0000");

        System.out.println("this is the final pin="+finalPin);
        securityDialog();

        signUp=findViewById(R.id.textView);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),signUp_Activity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            }
        });

        //intents
        welcome=new Intent(getApplicationContext(),WelcomeActivity.class);
        sign=new Intent(getApplicationContext(),signUp_Activity.class);

        //progress dialog
        load=new ProgressDialog(LoginActivity.this);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    @Override
    public void onStart() {
        super.onStart();

        Log.i("onStart: ","Activity started");
    }

    //change Activity based on if user is signedIN
    protected void updateUI(FirebaseUser currentUser) {

        if(currentUser != null){
            Toast.makeText(getApplicationContext(),"You are Signed IN",Toast.LENGTH_SHORT).show();
            startActivity(welcome);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }
        else if(finalPin.equals(enteredPin)){
           Snackbar.make(findViewById(R.id.layoutLogin),"Not Signed IN",Snackbar.LENGTH_SHORT).show();
            // Toast.makeText(getApplicationContext(),"You are Not Signed IN",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                Log.d("TAG: ", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG: ", "Google sign in failed", e);
                Toast.makeText(getApplicationContext(),"Google Login Failed!",Toast.LENGTH_SHORT).show();
            }
        }
    }

    //google sign in code
    private void firebaseAuthWithGoogle(String idToken) {
        loading(1); //start loading
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG: ", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user!=null){
                                loading(0); //dismiss
                                updateUI(user); //to change activity

                                /*//setting up a map for key vale pairs to add it into firebase data base
                                HashMap<String,String> map=new HashMap<>();
                                map.put("displayName",user.getDisplayName());
                                map.put("email", user.getEmail());*/

                                //put data into users map will erease all data already associated with the UID
                                userDB.child(user.getUid()).child("displayName").setValue(user.getDisplayName());
                                userDB.child(user.getUid()).child("email").setValue(user.getEmail());
                            }
                            Snackbar.make(findViewById(R.id.layoutLogin),"Successful Login",Snackbar.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            loading(0); //dismiss
                            Log.w("TAG: ", "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.layoutLogin), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);

                        }

                        // ...
                    }
                });
    }

    
}


