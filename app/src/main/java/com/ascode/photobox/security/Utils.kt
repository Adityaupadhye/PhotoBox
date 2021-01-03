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

    public var myName=FirebaseAuth.getInstance().currentUser?.displayName
    private var code=0
    private var subFolder: String?= null
    public var allLinks= ArrayList<String>()
    private var mySnaps: DatabaseReference=FirebaseDatabase.getInstance().reference
    public val imgNames=ArrayList<String>()


    companion object {

        public var linkedUserName:String?=null
        public fun createUniqueImgName(): String {
            //today's date
            val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            //current time
            val time = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())

            return "IMG_" + date + "_" + time
        }
    }

    public fun showDeleteDialog(context: Context, imgName: String,subfolder: String, linkedFolder: String) {

        val dialog = AlertDialog.Builder(context)
                .setTitle("Are you sure you want to delete?")
                .setMessage("You wont be able to recover this Image")
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setPositiveButton("YES", DialogInterface.OnClickListener { _, _ ->
                    if (imgName.isNotEmpty()){

                        //delete from DB
                        //to find correct mysnapRef here
                        println("linkedFolder=$linkedFolder")

                        //DB reference
                        val myLinkedFolder= FirebaseDatabase.getInstance().reference
                                .child("linkedUsers").child(linkedFolder)
                        //storage reference
                        var thisImgStorageRef=FirebaseStorage.getInstance().reference.child(linkedFolder)

                        if(subfolder.equals(ImageViewerActivity.select)){
                            mySnaps=myLinkedFolder.child("snaps")
                            thisImgStorageRef=thisImgStorageRef.child(imgName)
                        }
                        else{
                            mySnaps=myLinkedFolder.child("snaps").child(subfolder)
                            thisImgStorageRef=thisImgStorageRef.child(subfolder).child(imgName)
                        }
                        println("dbref onDelete=$mySnaps and imgName=$imgName")
                        mySnaps.child(imgName).removeValue().addOnCompleteListener{ task->
                            if(task.isSuccessful){
                                Toast.makeText(context,"Image deleted",Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(context,task.exception?.message,Toast.LENGTH_SHORT).show()
                            }
                        }

                        //delete from Firebase Storage
                        thisImgStorageRef.delete().addOnCompleteListener{task->
                            if(task.isSuccessful) {
                                Toast.makeText(context, "Image deleted from storage", Toast.LENGTH_SHORT).show()
                            }
                            else
                                Toast.makeText(context,task.exception?.message,Toast.LENGTH_SHORT).show()
                        }

                    }else{
                        Toast.makeText(context,"No ImageName given",Toast.LENGTH_LONG).show()
                    }

                })
                .setNegativeButton("NO", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
                .show()

        dialog.setCanceledOnTouchOutside(false)
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

        //println("dbref earlier=$mySnaps")
        Timer().schedule(10){
            println("called this timer")
            allLinks.clear()
            if(subfolder.equals(ImageViewerActivity.select)){
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