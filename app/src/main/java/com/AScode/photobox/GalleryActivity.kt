package com.AScode.photobox

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class GalleryActivity : AppCompatActivity() {

    lateinit var imgView: ImageView
    private var progressDialog: ProgressDialog? = null
    private lateinit var linkedUsersRef:DatabaseReference
    private lateinit var snapRef:DatabaseReference
    var myName:String?=null
    var linkedName:String?=null
    val select="Select A subFolder"
    var subFolder:String?=select
    var snapList=ArrayList<String>()
    var urls=ArrayList<String>()
    private lateinit var loadButton: Button
    private lateinit var loadBack: Button
    private var numOfPics=0
    var imgBitmap:Bitmap?=null
    var start:Long=0; var end:Long=0

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
                    end=System.currentTimeMillis()-start
                    println("end=$end")
                }

                override fun onCancelled(error: DatabaseError) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
            })
        }
        else{
            snapRef.child(subFolder!!).addChildEventListener(object : ChildEventListener{

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if(snapshot.key != null)
                        snapList.add(snapshot.key.toString())

                    urls.add(snapshot.value.toString())
                    println("snap= $snapList \nurl=$urls")
                }

                override fun onCancelled(error: DatabaseError) {

                }
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                }
                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        //all views
        imgView = findViewById(R.id.loadImg)
        loadButton=findViewById(R.id.loadButton)
        loadBack=findViewById(R.id.loadBack)

        //intents- not required

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

        //start time
        start=System.currentTimeMillis()

        //get url from DB
        getURL()

        //toolbar
        val toolbar = findViewById<Toolbar>(R.id.galleryToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        //add animation to button to show after 3sec
        loadButton.alpha=0f ; loadBack.alpha=0f
        loadButton.translationX=500f ; loadBack.translationX=-500f
        loadButton.animate().translationX(0f).alpha(1f).setDuration(1500)
        loadBack.animate().translationX(0f).alpha(1f).setDuration(1500)

        //when imageView is clicked photoActivity should open
        imgView.setOnClickListener{

            /*the idea is to open a fullscreen dialog when image is clicked
            the dialog would have layout of photoActivity*/

            if(imgView.drawable != null){
                //inflate the view of photoActivity
                val view=layoutInflater.inflate(R.layout.activity_photo,null)

                //image view of the view
                val photoImg=view.findViewById<ImageView>(R.id.photo)
                photoImg.setImageBitmap(imgBitmap)

                //toolbar Textview of the view
                val toolText=view.findViewById<TextView>(R.id.toolbarText)
                toolText.text=snapList.get(numOfPics)

                //toolbar of view
                val photoToolbar=view.findViewById<Toolbar>(R.id.photoToolbar)

                //display alertDialog when image is clicked
                val photoDialog: AlertDialog=AlertDialog.Builder(this,android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
                        .setView(view)
                        .show()

                //we can set nav listener in dialog which has layout file containing toolbar with nav icon
                photoToolbar.setNavigationOnClickListener{
                    photoDialog.dismiss()  //dismiss the dialog when back is clicked on toolbar
                }
            }
            else{
                Toast.makeText(this,"No Image Selected",Toast.LENGTH_SHORT).show()
            }

        }


        println("delayyy")
        println("all snaps=$snapList \n urls=$urls")
        //show 1st image upon opening
        setProgressDialog(1) //start loading
        Timer().schedule(1000){
            loadImage(numOfPics)
        }
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
        //to save this image as bitmap and transfer to alertDialog
        Glide.with(this@GalleryActivity)
                .asBitmap()
                .load(urls.get(num))
                .listener(object  : RequestListener<Bitmap?>{
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap?>?, isFirstResource: Boolean): Boolean {
                        setProgressDialog(0)//stop loading
                        Toast.makeText(applicationContext,"Error Loading Image",Toast.LENGTH_SHORT).show()
                        return false

                    }
                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        setProgressDialog(0)//stop loading
                        Toast.makeText(applicationContext,"Image Loaded",Toast.LENGTH_SHORT).show()
                        println("onResReady= $resource")
                        return false
                    }

                })
                .into(object : CustomTarget<Bitmap>() {
                    override fun onLoadCleared(placeholder: Drawable?) {}
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        imgBitmap=resource //save image bitmap to load  directly and also load in alertDialog
                        imgView.setImageBitmap(imgBitmap)
                        imgView.setBackgroundColor(Color.TRANSPARENT)
                    }
                })

    }

    //when loadImg button is clicked
    fun loadClicked(view: View){
        numOfPics++
        setProgressDialog(1) //start loading
        if(!urls.isEmpty() && numOfPics<urls.size){
            loadImage(numOfPics)
        }
        else {
            setProgressDialog(0)
            Toast.makeText(applicationContext,"End of Images in this folder",Toast.LENGTH_SHORT).show()
            numOfPics=urls.size-1
        }
        println(numOfPics)
    }

    //when loadBack clicked
    fun loadBack(view: View){
        numOfPics--
        setProgressDialog(1)//start loading
        if(!urls.isEmpty() && numOfPics>=0){
            loadImage(numOfPics)
        }
        else{
            setProgressDialog(0)
            Toast.makeText(applicationContext,"This is the first image",Toast.LENGTH_SHORT).show()
            numOfPics=0
        }
        println(numOfPics)
    }
}