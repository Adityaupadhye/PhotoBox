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
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class GalleryActivity : AppCompatActivity() {

    var imgView: ImageView? = null
    private var progressDialog: ProgressDialog? = null
    private var URL: String? = null
    private var imgViewer: Intent? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        //all views
        imgView = findViewById(R.id.loadImg)

        //intents
        imgViewer = Intent(this@GalleryActivity, ImageViewerActivity::class.java)

        //progressdialog
        progressDialog = ProgressDialog(this)

        //get url from DB
        FirebaseDatabase.getInstance().reference.child("urls").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                URL = snapshot.value.toString()
                println(URL)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })

        setProgressDialog(1) //start loading
        Handler().postDelayed({ loadImage() }, 4000)

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
                .into(imgView!!)
        imgView!!.setBackgroundColor(Color.TRANSPARENT)
    }
}