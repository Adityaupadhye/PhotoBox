package com.ascode.photobox

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ascode.photobox.adapters.GalleryViewClickListener
import com.ascode.photobox.adapters.MyAdapter
import com.ascode.photobox.adapters.ViewPagerAdapter
import com.ascode.photobox.security.Utils
import com.bumptech.glide.util.Util
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_gallery_view.*
import kotlinx.android.synthetic.main.activity_sign_up_.*
import kotlinx.android.synthetic.main.view_pager_item.*
import java.util.*

class GalleryViewActivity : AppCompatActivity() , GalleryViewClickListener{

    lateinit var links:ArrayList<String>
    lateinit var names:ArrayList<String>
    lateinit var currentFolder:String
    private lateinit var linkedFolder:String

    lateinit var myAdapter: MyAdapter
    lateinit var listener: ValueEventListener
    lateinit var location: DatabaseReference

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_view)

        //data from intents
        linkedFolder= intent.getStringExtra("linkedFolder").toString()
        currentFolder= intent.getStringExtra("currentFolder").toString()
        links=intent.getStringArrayListExtra("imgLinks") as ArrayList<String>
        names= intent.getStringArrayListExtra("imgNames") as ArrayList<String>


        currentFolderText.text= currentFolder

        myAdapter=MyAdapter(this, links, names,
                currentFolder, linkedFolder, this)
        galleryRecycler.adapter = myAdapter

        //notify adapter when child is removed from correct location
        location=FirebaseDatabase.getInstance().reference
                .child("linkedUsers").child(linkedFolder).child("snaps")
        if(!currentFolder.equals(Utils.select)) {
            location=location.child(currentFolder)
        }

        val manager:RecyclerView.LayoutManager= GridLayoutManager(this, 2)
        galleryRecycler.layoutManager=manager


        //to display back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        //to notify adapter
        listener=location.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                println("in onData changes")
                myAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    //recyclerview onItemClick
    override fun onItemClick(position: Int) {
        println("listener working")
        openDialog(position)
        //Toast.makeText(this, "new onclickListener", Toast.LENGTH_LONG).show()
    }

    //recyclerView onItemLongCLick
    override fun onItemLongClick(position: Int) {
        //delete img on longClick
        showDeleteDialog(this,names.get(position),position)
    }

    //opens the full screen dialog
    private fun openDialog(pos: Int) {
        val photoDialog = LayoutInflater.from(this)
                .inflate(R.layout.photo_dialog_view, null)

        val pager2: ViewPager2 = photoDialog.findViewById(R.id.imgSlider)
        val photoViewDialog = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
                .setView(photoDialog)
                .create()

        val viewPagerAdapter = ViewPagerAdapter(photoDialog.context, links,names,photoViewDialog)
        pager2.post { //to display the selected image by changing the default start of viewpager
            pager2.setCurrentItem(pos, false)
        }
        pager2.adapter = viewPagerAdapter

        photoViewDialog.show()

    }

    //deletes specific image
    private fun showDeleteDialog(context: Context, imgName: String, position: Int) {

        val dialog = AlertDialog.Builder(context)
                .setTitle("Are you sure you want to delete?")
                .setMessage("You wont be able to recover this Image")
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setPositiveButton("YES") { _, _ ->
                    if (imgName.isNotEmpty()) {

                        //delete from DB
                        //to find correct mysnapRef here
                        println("linkedFolder=$linkedFolder")

                        //DB reference
                        val myLinkedFolder = FirebaseDatabase.getInstance().reference
                                .child("linkedUsers").child(linkedFolder)
                        //storage reference
                        var thisImgStorageRef = FirebaseStorage.getInstance().reference.child(linkedFolder)
                        //exact snapRef
                        val mySnaps: DatabaseReference

                        if (currentFolder.equals(Utils.select)) {
                            mySnaps = myLinkedFolder.child("snaps")
                            thisImgStorageRef = thisImgStorageRef.child(imgName)
                        } else {
                            mySnaps = myLinkedFolder.child("snaps").child(currentFolder)
                            thisImgStorageRef = thisImgStorageRef.child(currentFolder).child(imgName)
                        }
                        println("dbref onDelete=$mySnaps and imgName=$imgName")
                        mySnaps.child(imgName).removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        }

                        //delete from Firebase Storage
                        thisImgStorageRef.delete().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Image deleted from storage", Toast.LENGTH_LONG).show()
                            } else
                                Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                        }

                        //delete specefic link from links list
                        links.removeAt(position)

                    } else {
                        Toast.makeText(context, "No ImageName given", Toast.LENGTH_LONG).show()
                    }

                }
                .setNegativeButton("NO") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

        dialog.setCanceledOnTouchOutside(false)
    }

/*    //edit folder name
    private fun editFolder(newName: String){
        Timer().schedule(1000){
                key?.let {
                    val updateMap= HashMap<String,Any>()
                    updateMap.put(it,newName)
                    FirebaseDatabase.getInstance().reference.child("linkedUsers")
                            .child(linkedFolder!!).child("subfolders")
                            .updateChildren(updateMap)
                    currentFolderText.text=newName
                }
        }
    }

    //to find correct key
    private fun findKey(){

        FirebaseDatabase.getInstance().reference.child("linkedUsers")
                .child(linkedFolder!!).child("subfolders")
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(data in snapshot.children){
                            if(data.value?.equals(currentFolder)!!){
                                println(data.key+"--"+data.value)
                                key=data.key
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

    }

    //to create dialog
    private fun createDialog(context: Context){
        val layout= LayoutInflater.from(context)
                .inflate(R.layout.create_sub_folder_dialog,null)

        findKey()

        val editDialog= AlertDialog.Builder(context)
                .setTitle("Rename Folder")
                .setView(layout)
                .setMessage("add new Folder Name")
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    val newText=layout.createSubfolderText.text.toString()
                    if(newText.isEmpty()){
                        Toast.makeText(context,"No Name Entered",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        editFolder(newText)
                    }
                })
                .setNegativeButton("CANCEL", DialogInterface.OnClickListener { dialog, which ->

                })
                .show()
        editDialog.setCanceledOnTouchOutside(false)
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)

    }

    override fun onDestroy() {
        super.onDestroy()
        location.removeEventListener(listener)
    }

}


