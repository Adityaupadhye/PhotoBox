package com.ascode.photobox.models

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class LinkModel {

    companion object{
        const val TAG="unlink"
        val userDB= FirebaseDatabase.getInstance().reference.child("users")

        fun unlink(myUid: String, linkedUid: String){
            val updateMap: HashMap<String,Any?> = HashMap()
            updateMap.put("linkedTo",null)
            updateMap.put("RequestFrom",null);
            updateMap.put("RequestFromEmail",null);
            updateMap.put("Link Request sent to",null);
            //remove my link
            userDB.child(myUid).updateChildren(updateMap).addOnCompleteListener { task->
                if(task.isSuccessful){
                    Log.d(TAG, "deleted my link")
                }
                else{
                    Log.d(TAG, task.exception?.message.toString())
                }
            }

            //remove linked person link
            userDB.child(linkedUid).updateChildren(updateMap).addOnCompleteListener { task->
                if(task.isSuccessful){
                    Log.d(TAG, "deleted linked person link link")
                }
                else{
                    Log.d(TAG, task.exception?.message.toString())
                }
            }
        }
    }
}