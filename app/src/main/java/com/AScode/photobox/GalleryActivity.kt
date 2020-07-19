package com.AScode.photobox

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class GalleryActivity : AppCompatActivity() {

    lateinit var imgView: ImageView
    private var progressDialog: ProgressDialog? = null
    private var URL: String? = null
    private var imgViewer: Intent? = null
    private lateinit var linkedUsersRef:DatabaseReference
    private var firebaseUser =FirebaseAuth.getInstance().currentUser
    private var myName: String?=firebaseUser?.displayName
    var linkedName:String?=null
    var snapList=ArrayList<String>()
    var urls=ArrayList<String>()

    fun getLinkedName(){
        //get linkedName
        linkedUsersRef.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val split=snapshot.key?.split("AND")

                if(split?.get(0).equals(myName) || split?.get(1).equals(myName)){
                    linkedName=snapshot.key
                }
            }
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }

    fun getURL(){
        linkedUsersRef.child(linkedName!!).child("snaps").addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapList.add(snapshot.key!!)
                urls.add(snapshot.value.toString())
                println("snap=$snapList and url=$urls")
            }
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        //all views
        imgView = findViewById(R.id.loadImg)

        //intents
        imgViewer = Intent(this@GalleryActivity, ImageViewerActivity::class.java)

        //progressdialog
        progressDialog = ProgressDialog(this)

        //DB refs
        linkedUsersRef =FirebaseDatabase.getInstance().reference.child("linkedUsers")

        //to get linkedName
        getLinkedName()

        //get url from DB
        println("myName is $myName \t current firebaseUser is $firebaseUser")
        Timer().schedule(3000){
            getURL()
        }

        setProgressDialog(1) //start loading
        Handler().postDelayed({
            if(!URL.equals(null))
                loadImage()
            else {
                setProgressDialog(0)
                Toast.makeText(this,"URL not found",Toast.LENGTH_SHORT).show()
            }
        }, 4000)

        //toolbar
        val toolbar = findViewById<Toolbar>(R.id.galleryToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            startActivity(imgViewer)
            finish()
        }

    }

    private fun setProgressDialog(code: Int) {
        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_ProgressBar)
        progressDialog!!.setMessage("Loading")
        progressDialog!!.setCanceledOnTouchOutside(false)
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
        if (code == 0) {
            progressDialog!!.dismiss()
        }
    }

    protected fun loadImage() {
        Glide.with(this@GalleryActivity)
                .load(URL)
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                        setProgressDialog(0)
                        Toast.makeText(this@GalleryActivity, "failed" + e?.message, Toast.LENGTH_SHORT).show()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        setProgressDialog(0)
                        Toast.makeText(this@GalleryActivity, "Image is Loaded", Toast.LENGTH_SHORT).show()
                        return false
                    }
                })
                .into(imgView)
        imgView.setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onResume() {
        super.onResume()

        Timer().schedule(3000){
            println("delayyy")
            println("all snaps=$snapList \n urls=$urls")
        }
    }
}