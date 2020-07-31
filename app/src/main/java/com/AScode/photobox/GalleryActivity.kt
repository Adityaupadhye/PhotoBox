package com.AScode.photobox

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class GalleryActivity : AppCompatActivity() {

    lateinit var imgView: ImageView
    private var progressDialog: ProgressDialog? = null
    private var imgViewer: Intent? = null
    private lateinit var linkedUsersRef:DatabaseReference
    private lateinit var snapRef:DatabaseReference
    var myName:String?=null
    var linkedName:String?=null
    val select="Select A subFolder"
    var subFolder:String?=select
    var snapList=ArrayList<String>()
    var urls=ArrayList<String>()
    private lateinit var loadButton:Button
    private var numOfPics=-1

    //add snapName and url to arraylist
    fun getURL() {
        if (subFolder.equals(select)) {
            println("no subfolder Selected")
            snapRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.key!!.toString().contains("IMG")){
                        snapList.add(snapshot.key!!)
                        urls.add(snapshot.value.toString())
                        println("snap=$snapList and url=$urls")
                    }

                }

                override fun onCancelled(error: DatabaseError) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
            })
        }
        else{
            snapRef.child(subFolder!!).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(snap:DataSnapshot in snapshot.children){
                        if(snap.key != null)
                            snapList.add(snapshot.key!!)

                        urls.add(snap.value.toString())
                        println("in SubFolder snap=$snapList and urls=$urls")
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        //all views
        imgView = findViewById(R.id.loadImg)
        loadButton=findViewById(R.id.loadButton)

        //intents
        imgViewer = Intent(this@GalleryActivity, ImageViewerActivity::class.java)

        //progressdialog
        progressDialog = ProgressDialog(this)

        //myName from Intent
        myName=intent.getStringExtra("myNameFromIntent")
        println("myName is $myName from Intent")

        //to get linkedName
        linkedName=intent.getStringExtra("linkedUserName")
        println("linkedName form intent is $linkedName")

        //DB refs
        linkedUsersRef =FirebaseDatabase.getInstance().reference.child("linkedUsers")
        if(linkedName != null)  //null check
            snapRef=linkedUsersRef.child(linkedName!!).child("snaps")
        else
            Toast.makeText(this@GalleryActivity,"Error! Try Again!!",Toast.LENGTH_SHORT).show();

        //getting subFolder from Intent
        subFolder=intent.getStringExtra("subFolder")

        //get url from DB
        getURL()

        //toolbar
        val toolbar = findViewById<Toolbar>(R.id.galleryToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            /*imgViewer?.putExtra("myNameForBack",myName)
            startActivity(imgViewer)*/
            finish()
        }

        //add animation to button to show after 3sec
        loadButton.alpha=0f
        loadButton.translationY=500f
        loadButton.animate().translationY(0f).alpha(1f).setDuration(3000)
    }

    //to show loading dialog
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

    //load image using glide
    protected fun loadImage(num: Int) {
        Glide.with(this@GalleryActivity)
                .load(urls.get(num))
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

    //when loadImg button is clicked
    fun loadClicked(view: View){
        numOfPics++
        setProgressDialog(1) //start loading
        if(!urls.isEmpty() && numOfPics<urls.size)
            loadImage(numOfPics)
        else {
            setProgressDialog(0)
            Toast.makeText(applicationContext,"Press again to view from start",Toast.LENGTH_SHORT).show()
            numOfPics=-1
        }
    }
}