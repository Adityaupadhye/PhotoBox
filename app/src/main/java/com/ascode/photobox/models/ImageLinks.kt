package com.ascode.photobox.models

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ImageLinks{

    public fun getAllLinks(linkedName: String, subFolder: String):ArrayList<String>{

        val snapRef= FirebaseDatabase.getInstance().reference.child("linkedUsers")
                        .child(linkedName).child("snaps")

        var urls = ArrayList<String>()

        snapRef.addListenerForSingleValueEvent(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap in snapshot.children){
                    urls.add(snap.value.toString())
                    //Thread.sleep(1000)
                    println("snap.children= $snap")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


        return urls
    }
}