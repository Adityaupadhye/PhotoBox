package com.AScode.photobox;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class signUp_Activity extends AppCompatActivity {

    private EditText nameText, emailText,pswdText,re_pswdText;
    private FirebaseAuth mAuth;
    private String name,email,pswd,re_pswd;
    private Intent welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_);

        //initialize editTexts in onCreate only
        nameText=findViewById(R.id.name);
        emailText=findViewById(R.id.username);
        pswdText=findViewById(R.id.password);
        re_pswdText=findViewById(R.id.re_password);

        //firebase auth
        mAuth=FirebaseAuth.getInstance();

        //intents
        welcome=new Intent(getApplicationContext(),WelcomeActivity.class);
    }

    public void signupUser(View view){
        //get strings form all editTexts
        name=nameText.getText().toString();
        email=emailText.getText().toString();
        pswd=pswdText.getText().toString();
        re_pswd=re_pswdText.getText().toString();

        if(name.isEmpty() || email.isEmpty() || pswd.isEmpty() || re_pswd.isEmpty()){
            Toast.makeText(this,"Pls Enter All Details!",Toast.LENGTH_SHORT).show();
        }
        //checking if both pswds are equal and no field is null
        else if(pswd.equals(re_pswd)){
            //to show creating account progress
            final ProgressDialog progressDialog=new ProgressDialog(signUp_Activity.this);
            progressDialog.setProgressStyle(R.style.Widget_AppCompat_ProgressBar);
            progressDialog.setMessage("Loading");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
            //signUp users
            mAuth.createUserWithEmailAndPassword(email,pswd)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //signUp complete
                                Toast.makeText(getApplicationContext(),"Account Created Successfully",Toast.LENGTH_LONG).show();
                                System.out.println("Account Created Successfully");

                                //to set the display name we have to update profile
                                FirebaseUser user=mAuth.getCurrentUser();
                                UserProfileChangeRequest profileChangeRequest=new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name.trim()).build();
                                if(user!=null) {
                                    user.updateProfile(profileChangeRequest)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("profile update: ", "user display name updated---"+name);
                                                    } else {
                                                        Log.d("profile update: ", task.getException().toString());
                                                    }
                                                }
                                            });


                                    //setting up a map for key vale pairs to add it into firebase data base
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("displayName", name);
                                    map.put("email", user.getEmail());
                                    System.out.println("map---"+map);
                                    //put data into users
                                    FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).setValue(map);
                                    System.out.println("Upadted to database");
                                }
                                progressDialog.dismiss();
                                updateUI_signUp(user);

                                }else{
                                //signUp not Complete
                                Log.w("User not created: ",task.getException());
                                Toast.makeText(getApplicationContext(),"Error! User Not created",Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        }
                    });

        }
        else{
            pswdText.setText(null);
            re_pswdText.setText(null);
            Toast.makeText(getApplicationContext(),"Password not matching\nEnter Password again",Toast.LENGTH_LONG).show();
        }
    }

    private void updateUI_signUp(FirebaseUser user){
        if(user!=null){
            Toast.makeText(getApplicationContext(),"You are Signed IN",Toast.LENGTH_SHORT).show();
            System.out.println("created account and sent to welcomeActivity");
            welcome.putExtra("nameFromSignUp",name);
            startActivity(welcome);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }else{
            Toast.makeText(getApplicationContext(),"User Not created",Toast.LENGTH_LONG).show();
        }
    }

}
