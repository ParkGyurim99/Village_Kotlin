package com.example.team_project_0_posting

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.team_project_0_posting.ListAdapter.ViewHolder
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ListAdapter(private val context: Context): RecyclerView.Adapter<ViewHolder>() {
    private var postList = mutableListOf<Post>()

    fun setListData(data:MutableList<Post>){
        postList = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item,parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListAdapter.ViewHolder, position: Int) {
        val post : Post = postList[position]

        var path : String = "gs://village-e6e1a.appspot.com/images/" + post.imageUrl
        var storage = Firebase.storage
        var gsRef = storage.getReferenceFromUrl(path)

        /* GlideApp.with(holder.itemView)
            .load(path + post.imageUrl)
            .into(holder.image)*/

        gsRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                GlideApp.with(holder.itemView)
                    .load(it.result)
                    .into(holder.image)
            }
        }

        holder.name.text = post.userId
        holder.title.text = post.title
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val image : ImageView = itemView.findViewById(R.id.image)
        val name : TextView = itemView.findViewById(R.id.name)
        val title : TextView = itemView.findViewById(R.id.title)
    }
}