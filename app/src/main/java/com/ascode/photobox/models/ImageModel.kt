package com.ascode.photobox.models

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ImageModel : ValueEventListener{

    public fun findSnapNames(myLinkedName: String){
        FirebaseDatabase.getInstance().reference.child("linkedUsers")
                .child(myLinkedName).child("snaps")
                .addListenerForSingleValueEvent(this)
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        for(data in snapshot.children){
            if(data.key.toString().contains("IMG_")){

            }
        }
    }

    override fun onCancelled(error: DatabaseError) {

    }

}