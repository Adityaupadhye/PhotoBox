package com.ascode.photobox.models

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserModel {

    public fun getAllUsers(): ArrayList<User>{
        val userList= ArrayList<User>()

        FirebaseDatabase.getInstance().reference.child("users")
                .addListenerForSingleValueEvent(object : ValueEventListener{

                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(uid in snapshot.children){
                            val name= uid.child("displayName").value.toString()
                            val email=uid.child("email").value.toString()
                            userList.add(User(uid.key.toString(),name, email))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

        return userList
    }
}