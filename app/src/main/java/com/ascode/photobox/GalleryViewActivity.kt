package com.ascode.photobox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ascode.photobox.adapters.MyAdapter
import kotlinx.android.synthetic.main.activity_gallery_view.*

class GalleryViewActivity : AppCompatActivity() {

    lateinit var links:ArrayList<String>

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_view)

        //links=intent.getSerializableExtra("url") as ArrayList<String>

        val currentFolder= intent.getStringExtra("currentFolder")
        currentFolderText.text= currentFolder

        links=intent.getStringArrayListExtra("imgLinks") as ArrayList<String>

        val myAdapter=MyAdapter(this,links)
        galleryRecycler.adapter=myAdapter

        val manager:RecyclerView.LayoutManager= GridLayoutManager(this,2)
        galleryRecycler.layoutManager=manager


        //to display back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            android.R.id.home-> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)

    }
}


