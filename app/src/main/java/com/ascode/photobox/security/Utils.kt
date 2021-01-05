package com.ascode.photobox.security

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.Toast
import com.ascode.photobox.ImageViewerActivity
import com.ascode.photobox.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

class Utils: ChildEventListener {

    var myName=FirebaseAuth.getInstance().currentUser?.displayName
    private var code=0
    private var subFolder: String?= null
    var allLinks= ArrayList<String>()
    private var mySnaps: DatabaseReference=FirebaseDatabase.getInstance().reference
    val imgNames=ArrayList<String>()

    companion object {

        public final val select="Select A subFolder"

        public var linkedUserName:String?=null
        public fun createUniqueImgName(): String {
            //today's date
            val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            //current time
            val time = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())

            return "IMG_" + date + "_" + time
        }
    }

    fun findMyLinkedFolder(){
        code=0
        FirebaseDatabase.getInstance().reference.child("linkedUsers")
                .addChildEventListener(this)
    }

    fun findAllLinks(subfolder: String){

        val myLinkedFolder= FirebaseDatabase.getInstance().reference.child("linkedUsers").child(linkedUserName!!)

        code=1
        subFolder=subfolder

        //println("dbref earlier=$mySnaps")
        Timer().schedule(10){
            println("called this timer")
            allLinks.clear()
            if(subfolder.equals(Utils.select)){
                mySnaps=myLinkedFolder.child("snaps")
                mySnaps.addChildEventListener(this@Utils)
            }
            else{
                mySnaps=myLinkedFolder.child("snaps").child(subfolder)
                mySnaps.addChildEventListener(this@Utils)
            }

        }
    }

    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
        snapshot.key?.let { it ->
            if (code ==0) { //called from findMyLinkedFolder
                val split = it.split("AND".toRegex()).toTypedArray()
                //to find linkedUserName
                if (split[0] == myName || split[1] == myName) {
                    linkedUserName = it.trim()
                    println("linkedName in utils is $linkedUserName")
                    //duration = System.currentTimeMillis() - start
                    //println("end time=$duration")
                }
            }
            if(code == 1){
                //called from getAllLinks
                    println("code=1")
                if(snapshot.key!!.toString().contains("IMG_")){
                    allLinks.add(snapshot.value.toString())
                    imgNames.add(snapshot.key.toString())
                    println("added in utils $allLinks")
                    println("added in utils imgnames $imgNames")
                }
            }
        }
    }

    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

    }

    override fun onChildRemoved(snapshot: DataSnapshot) {

    }

    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

    }

    override fun onCancelled(error: DatabaseError) {

    }

    public fun getSize(list: ArrayList<String>):Int{

        return list.size
    }

}