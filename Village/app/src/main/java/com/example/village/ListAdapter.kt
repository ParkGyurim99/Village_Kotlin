package com.example.village

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.village.ListAdapter.ViewHolder
import com.example.village.model.Post
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

        gsRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                GlideApp.with(holder.itemView)
                    .load(it.result)
                    .into(holder.image)
            }
        }

        // 일단 list_item.xml에 표시될 데이터
        holder.nickname.text = post.nickname
        holder.title.text = post.title

        // xml에 저장만 할 데이터
        //holder.location.text = post.location
        holder.timestamp.text = post.timestamp.toString()
        holder.price.text = post.price
        holder.likeCount.text = post.likeCount.toString()
        //holder.viewCount.text = post.viewCount.toString()
        //holder.uid.text = post.uid
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val image : ImageView = itemView.findViewById(R.id.image)
        val nickname : TextView = itemView.findViewById(R.id.nickname)
        val title : TextView = itemView.findViewById(R.id.title)

        val location : TextView = itemView.findViewById(R.id.location)
        val timestamp : TextView = itemView.findViewById(R.id.timestamp)
        val price : TextView = itemView.findViewById(R.id.price)
        val likeCount : TextView = itemView.findViewById(R.id.likeCount)
        val viewCount : TextView = itemView.findViewById(R.id.viewCount)
    }

    /*fun likeEvent(position: Int) {
        var firestore = FirebaseFirestore.getInstance()
        var tsPost = firestore.collection("user-posts").document(uidList[position])

        firestore.runTransaction { transaction ->
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var postData = transaction.get(tsPost).toObject<Post>()

            if (postData!!.lik)
        }
    }*/
}