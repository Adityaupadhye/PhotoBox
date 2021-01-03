package com.ascode.photobox

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ascode.photobox.models.ImageModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_gallery.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class GalleryActivity : AppCompatActivity() {

    lateinit var imgView: ImageView
    //private var progressDialog: ProgressDialog? = null
    private lateinit var linkedUsersRef:DatabaseReference
    private lateinit var snapRef:DatabaseReference
    var myName:String?=null
    var linkedName:String?=null
    private val select="Select A subFolder"
    var subFolder:String?=select
    var snapList=ArrayList<String>()
    var urls=ArrayList<String>()
    private lateinit var loadButton: Button
    private lateinit var loadBack: Button
    private var numOfPics=0
    var imgBitmap:Bitmap?=null
    var start:Long=0; var end:Long=0

    //add snapName and url to arrayList
    private fun getURL() {
        if (subFolder.equals(select)) {
            println("no subfolder Selected")
            snapRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.key!!.toString().contains("IMG_")){
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
        loadButton=findViewById(R.id.loadNext)
        loadBack=findViewById(R.id.loadBack)

        //intents- not required

        //progressdialog
        //progressDialog = ProgressDialog(this)

        viewGallery.setOnClickListener{
            viewGallery()
        }

        //myName from Intent
        myName=intent.getStringExtra("myNameFromIntent")
        println("myName is $myName from Intent")

        //to get linkedName
        linkedName=intent.getStringExtra("linkedUserName")
        println("linkedName form intent is $linkedName")

        //DB refs
        linkedUsersRef =FirebaseDatabase.getInstance().reference.child("linkedUsers")

        //for null check
        linkedName?.let {
            snapRef=linkedUsersRef.child(linkedName!!).child("snaps")
        }
        if(linkedName != null)  //null check
            snapRef=linkedUsersRef.child(linkedName!!).child("snaps")
        else
            Toast.makeText(this@GalleryActivity,"Error! Try Again!!",Toast.LENGTH_SHORT).show();

        //getting subFolder from Intent
        subFolder=intent.getStringExtra("subFolder")

        //start time
        start=System.currentTimeMillis()

        //testing new function


        //get url from DB
        getURL()

        //toolbar
        val toolbar = findViewById<Toolbar>(R.id.galleryToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        //add animation to button to show after 3sec

        loadBack.alpha=0f ; loadBack.alpha=0f
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
                val photoImg=view.findViewById<SubsamplingScaleImageView>(R.id.photo)
                photoImg.setImage(ImageSource.bitmap(imgBitmap!!))
                //photoImg.setImageBitmap(imgBitmap)

                //toolbar Textview of the view
                val toolText=view.findViewById<TextView>(R.id.toolbarText)
                toolText.text= snapList[numOfPics]

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

                photoToolbar.inflateMenu(R.menu.img_menu)

                //set menu listener for this layout
                photoToolbar.setOnMenuItemClickListener(object : Toolbar.OnMenuItemClickListener{
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        //switch case
                        when(item?.itemId){
                            R.id.share ->{
                                Toast.makeText(this@GalleryActivity,"Selected",Toast.LENGTH_SHORT).show()
                                shareImg(imgBitmap!!)
                                return true
                            }
                            else -> return false
                        }
                    }
                })


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

//        progressDialog!!.setProgressStyle(android.R.style.Widget_DeviceDefault_ProgressBar)
//        progressDialog!!.setMessage("Loading")
//        progressDialog!!.setCanceledOnTouchOutside(false)
//        progressDialog!!.setCancelable(false)
//        progressDialog!!.show()
        loaderLayout.alpha=1f
        if (code == 0) {
            //progressDialog!!.dismiss()
            loaderLayout.alpha=0f
        }
    }

    //load image using glide
    protected fun loadImage(num: Int) {
        //to save this image as bitmap and transfer to alertDialog
        Glide.with(this@GalleryActivity)
                .asBitmap()
                .load(urls[num])
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

    //to share imageUrl using intent
    private fun shareImg(bitmap: Bitmap?){
        val intent=Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT,"This Image is shared from PhotoBox app "+urls[numOfPics])
        intent.type="text/plain"
        val chooser=Intent.createChooser(intent,"Sent from PhotoBox")
        startActivity(chooser)
    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater=menuInflater
        menuInflater.inflate(R.menu.img_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }*/

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.share ->{
                Toast.makeText(this,"yesss",Toast.LENGTH_SHORT).show()
                shareImg(imgBitmap)
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

    private fun addFile(){
        
    }

    private fun viewGallery(){
        val galleryIntent=Intent(this,GalleryViewActivity::class.java)
        galleryIntent.putExtra("url",urls)
        startActivity(galleryIntent)
    }

}