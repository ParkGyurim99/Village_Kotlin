package com.example.village

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.village.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ListAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var postList = mutableListOf<Post>()
    private var mContext: Context? = null
    var firestore : FirebaseFirestore? = null

    interface OnItemClickListener{
        fun onItemClick(v:View, data: Post, pos: Int)
    }
    private var listener : OnItemClickListener? = null
    fun setOnItemClickListener(listener : OnItemClickListener) {
        this.listener = listener
    }

    fun setListData(data: MutableList<Post>) {
        postList = data
    }

    init {  // searchList의 문서를 불러온 뒤 Person으로 변환해 ArrayList에 담음
        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("user-posts")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            // ArrayList 비워줌
            postList.clear()

            for (snapshot in querySnapshot!!.documents) {
                var item = snapshot.toObject(Post::class.java)
                postList.add(item!!)
            }
            notifyDataSetChanged()
        }
    }

    // 아이템 레이아웃과 결합
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder {
        mContext = parent.context

        val view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false)

        return ViewHolder(view)
    }

    // View에 내용 입력
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val post: Post = postList[position]
        var viewHolder = (holder as ViewHolder).itemView

        holder.bind(post)
    }

    // 리스트 내 아이템 개수
    override fun getItemCount(): Int {
        return postList.size
    }

    // onCreateViewHolder에서 만든 view와 실제 데이터를 연결
    // 레이아웃 내 View 연결
    inner class ViewHolder(itemView: View)  // itemView는 list_item.xml
        : RecyclerView.ViewHolder(itemView) {

        val image: ImageView = itemView.findViewById(R.id.image)
        //val nickname: TextView = itemView.findViewById(R.id.nickname)
        val title: TextView = itemView.findViewById(R.id.title)

        val location: TextView = itemView.findViewById(R.id.location)
        val time: TextView = itemView.findViewById(R.id.time)
        val price: TextView = itemView.findViewById(R.id.price)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        //val viewCount: TextView = itemView.findViewById(R.id.viewCount)

        // onBindViewHolder에서 호출
        fun bind(item: Post) {
            var path: String = item.imageUrl.toString()
            var storage = Firebase.storage
            var gsRef = storage.getReferenceFromUrl(path)

            // 이미지
            gsRef.downloadUrl.addOnCompleteListener {
                if (it.isSuccessful) {
                    GlideApp.with(itemView)
                        .load(it.result)
                        .into(image)
                }
            }

            // 일단 list_item.xml에 표시될 데이터
            title.text = item.title                      // 제목
            time.text = item.timestamp.toString()        // 시간
            price.text = item.price.toString() + "원"     // 가격
            likeCount.text = item.likeCount.toString()   // 좋아요 수

            // list_item.xml에 저장만 할 데이터
            // uid.text = item.uid                       // uid
            // nickname.text = item.nickname             // 이름
            //viewCount.text = item.viewCount.toString() // 조회수
            // location.text = item.location             // 장소
            // category.text = item.category
            val pos = adapterPosition
            if(pos!= RecyclerView.NO_POSITION)
            {
                itemView.setOnClickListener {
                    listener?.onItemClick(itemView,item,pos)
                }
            }
        }
    }

    fun search(serachWord : String, option : String) {
        firestore = FirebaseFirestore.getInstance()
        firestore?.collection("user-posts")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            // ArrayList 비워줌
            postList.clear()
            for (snapshot in querySnapshot!!.documents) {
                if (snapshot.getString(option)!!.contains(serachWord)) {
                    var item = snapshot.toObject(Post::class.java)
                    postList.add(item!!)
                }
            }
            notifyDataSetChanged()
        }
    }
}