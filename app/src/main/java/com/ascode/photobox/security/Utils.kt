package com.ascode.photobox.security

import com.ascode.photobox.ImageViewerActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class Utils: ChildEventListener {

    public var myName=FirebaseAuth.getInstance().currentUser?.displayName
    public var linkedUserName:String?=null
    private var code=0
    private var subFolder: String?= null
    public var allLinks= ArrayList<String>()


    companion object{
        public fun createUniqueImgName():String{
            //today's date
            val date= SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            //current time
            val time = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())

            return "IMG_"+date+"_"+time
        }
    }

    public fun findMyLinkedFolder(){
        code=0
        FirebaseDatabase.getInstance().reference.child("linkedUsers")
                .addChildEventListener(this)
    }

    public fun findAllLinks(subfolder: String){

        val myLinkedFolder= FirebaseDatabase.getInstance().reference.child("linkedUsers").child(linkedUserName!!)

        code=1
        subFolder=subfolder

        Timer().schedule(10){
            println("called this timer")
            allLinks.clear()
            if(subfolder.equals(ImageViewerActivity.select))
                myLinkedFolder.child("snaps").addChildEventListener(this@Utils)
            else
                myLinkedFolder.child("snaps").child(subfolder).addChildEventListener(this@Utils)
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
                    println("added in utils $allLinks")
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