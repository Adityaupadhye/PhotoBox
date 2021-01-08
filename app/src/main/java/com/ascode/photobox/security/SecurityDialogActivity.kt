package com.ascode.photobox.security

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ascode.photobox.LoginActivity
import com.ascode.photobox.R
import com.ascode.photobox.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

//this class creates the security dialog at start of app
class SecurityDialogActivity : AppCompatActivity() {

    var enteredPin:String?=null; var finalPin:String?=null;
    var newPIN:String?=null; var confirmPIN:String?=null
    var sharedPreferences:SharedPreferences?=null
    lateinit var securityDialogView: View; lateinit var resetPinView: View

    //create dialog using custom view
    private fun createDialog(view: View, title:String?,
                             msg:String?, posText:String?,
                             negText:String?, neuText:String?): AlertDialog{

        val dialog = AlertDialog.Builder(this@SecurityDialogActivity, R.style.alertDialog)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(posText, null)
                .setNegativeButton(negText, null)
                .setNeutralButton(neuText, null)
                .setView(view)
                .show()

        return dialog
    }

    //App PIN Dialog box
    private fun securityDialog(view: View) {

        val dialog = createDialog(view,
                "SECURITY",
                "Enter 4-digit App PIN\nRESET PIN if not created OR Forgot",
                "OK",
                "EXIT",
                "RESET PIN")
        dialog.setCanceledOnTouchOutside(false)

        //set positiveButton
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            val pinText = view.findViewById<EditText>(R.id.pin) //find EditText pin from view
            enteredPin = pinText.getText().toString()
            println("click pin=$finalPin")
            if (enteredPin == finalPin) {
                // Check if user is signed in (non-null) and update UI accordingly.
                val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

                Toast.makeText(applicationContext, "Correct PIN", Toast.LENGTH_SHORT).show()
                println("PIN Entered is correct")
                dialog.dismiss()

                updateUI(currentUser)
            } else {
                pinText.text = null
                Toast.makeText(applicationContext, "Incorrect PIN!  Try Again", Toast.LENGTH_SHORT).show()
            }
        }

        //set NegativeButton
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setOnClickListener { System.exit(0) }

        //set NeutralButton
        val neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        neutralButton.setOnClickListener {
            //dialog.dismiss()
            resetPinDialog(resetPinView)
        }
        dialog.setCancelable(false)
    }

    //reset PIN Dialog
    private fun resetPinDialog(view:View) {

        val alertDialog = createDialog(view,
                "Set App PIN","Enter a 4-digit PIN","DONE",
                "Cancel","BACK")
        alertDialog.setCanceledOnTouchOutside(false)

        //set positiveButton
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            val newPinText = view.findViewById<EditText>(R.id.newPin) //find EditText newPin from dialog view
            val confirmPinText = view.findViewById<EditText>(R.id.ConfirmPin) //find EditText confirmPin from dialog view
            newPIN = newPinText.getText().toString()
            confirmPIN = confirmPinText.getText().toString()
            //check if newPin and ConfirmPin are equal
            if (newPIN == confirmPIN && !newPIN!!.isEmpty()) {
                Toast.makeText(applicationContext, "PIN reset Successfully", Toast.LENGTH_SHORT).show()
                //finalPin=newPIN;
                //sharedPreferences = applicationContext.getSharedPreferences("com.comcode.photobox", MODE_PRIVATE)
                sharedPreferences?.edit()?.putString("pin", newPIN)?.apply()
                finalPin = sharedPreferences?.getString("pin", "0000")
                println("final pin=$finalPin")
                //securityDialog()
                alertDialog.dismiss()
            } else {
                Toast.makeText(applicationContext, "PIN not matching", Toast.LENGTH_SHORT).show()
                newPinText.setText(null)
                confirmPinText.setText(null)
                newPinText.requestFocus()
            }
        }

        //set negativeButton
        val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setOnClickListener { System.exit(0) }

        //set neutralButton
        val neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        neutralButton.setOnClickListener {
            alertDialog.dismiss()
            //securityDialog()
        }
        alertDialog.setCancelable(false)
    }

    //change activity function
    private fun updateUI(currentUser: FirebaseUser?){

        if(currentUser==null){
            //show login screen
            val loginIntent=Intent(applicationContext, LoginActivity::class.java)
            startActivity(loginIntent)
        }else{
            val welcomeIntent=Intent(applicationContext, WelcomeActivity::class.java)
            startActivity(welcomeIntent)
        }
        finish()

    }

    /*//testing func
    private fun deleteData(){
        FirebaseDatabase.getInstance().reference.child("test").setValue(null)
                .addOnCompleteListener {
            Toast.makeText(this,"deleted",Toast.LENGTH_SHORT).show()
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_dialog)

        //inflate view for custom dialog
        securityDialogView = layoutInflater.inflate(R.layout.dialog, null)
        resetPinView= layoutInflater.inflate(R.layout.reset_pin_dialog,null)

        securityDialog(securityDialogView)

        sharedPreferences=applicationContext.getSharedPreferences("com.ascode.photobox", Context.MODE_PRIVATE)
        finalPin=sharedPreferences?.getString("pin","0000")

    }
}