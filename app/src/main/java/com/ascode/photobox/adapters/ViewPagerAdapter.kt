package com.ascode.photobox.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ascode.photobox.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.view_pager_item.view.*

class ViewPagerAdapter(
        var context: Context,
        var urls: ArrayList<String>,
        var pos: Int
) : RecyclerView.Adapter<ViewPagerAdapter.PhotoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val view= LayoutInflater.from(parent.context)
                .inflate(R.layout.view_pager_item, parent, false)

        return PhotoHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
        loadImg(position,holder)
    }

    override fun getItemCount(): Int {
        return urls.size
    }


    private fun loadImg(position: Int, viewHolder: PhotoHolder) {
        println("url in loadImg is ${urls[position]} \n and position = $position")
        Glide.with(context)
                .asDrawable()
                .load(urls[position])
                .listener(object : RequestListener<Drawable?>{
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable?>?, isFirstResource: Boolean): Boolean {
                        showLoading(viewHolder)

                        return false
                    }
                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        showLoading(viewHolder)
                        //println("loaded position=$position")
                        return false
                    }
                })
                .into(viewHolder.itemView.fullImg)
    }

    private fun showLoading(holder: PhotoHolder) {
        holder.itemView.imgLoad.alpha = 0f
    }

    inner class PhotoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

}