package com.ascode.photobox.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.ascode.photobox.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<String> urls;
    ArrayList<String> names;
    String subfolder;
    String linkedFolder;
    GalleryViewClickListener clickListener;

    public MyAdapter(){

    }

    public MyAdapter(Context context,ArrayList<String> urls, ArrayList<String> names,String subfolder, String linkedFolder
                        ,GalleryViewClickListener clickListener) {
        this.context = context;
        this.urls = urls;
        this.names=names;
        this.subfolder=subfolder;
        this.linkedFolder=linkedFolder;
        this.clickListener=clickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater=LayoutInflater.from(context);
        View galleryItem= inflater.inflate(R.layout.gallery_item,parent,false);

        return new MyViewHolder(galleryItem);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        loadImg(position,holder);   //loads image

        /*holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context,position+" clicked",Toast.LENGTH_SHORT).show();
                openDialog(position);
            }
        });


        //delete img on longclick
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //show delete dialog
               new Utils().showDeleteDialog(context,names.get(position),subfolder,linkedFolder);

                return false;
            }
        })*/;
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    //to dismiss loader
    private void showLoading(MyViewHolder holder){
        holder.loader.setAlpha(0);
    }

    //loads a particular image
    private void loadImg(final int position, final MyViewHolder viewHolder){

        Glide.with(context)
                .asBitmap()
                .load(urls.get(position))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        showLoading(viewHolder);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        showLoading(viewHolder);
                        Toast.makeText(context,"loaded "+position,Toast.LENGTH_SHORT).show();
                        return false;
                    }
                })
                .into(viewHolder.imageView);
    }

/*    //opens the full screen dialog
    private void openDialog(final int pos){

        final View photoDialog = LayoutInflater.from(context)
                .inflate(R.layout.photo_dialog_view,null);

        final ViewPager2 pager2=photoDialog.findViewById(R.id.imgSlider);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(photoDialog.getContext(),urls,pos);
        pager2.post(new Runnable() {
            @Override
            public void run() {
                //to display the selected image by changing the default start of viewpager
                pager2.setCurrentItem(pos,false);
            }
        });
        pager2.setAdapter(viewPagerAdapter);

        final AlertDialog photoViewDialog = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
                .setView(photoDialog)
                .show();

        photoDialog.findViewById(R.id.backImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoViewDialog.dismiss();
                System.out.println("clicked");
            }
        });
    }*/

    class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        ProgressBar loader;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView=itemView.findViewById(R.id.pic);
            loader=itemView.findViewById(R.id.picLoader);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    clickListener.onItemLongClick(getAdapterPosition());
                    return true;
                }
            });

        }
    }
}
