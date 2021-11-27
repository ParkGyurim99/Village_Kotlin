package com.example.village

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.village.model.Comment

class ListAdapter2 : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var commentList = mutableListOf<Comment>()
    private var mContext: Context? = null
    private var pid: Int? = null
    private var pid_comment_index: Int = 0  // 포스트마다 달린 댓글의 리스트를 참조할 index

    interface OnItemClickListener{
        fun onItemClick(v: View, data: Comment, pos: Int)
    }
    private var listener : OnItemClickListener? = null
    fun setOnItemClickListener(listener : OnItemClickListener) {
        this.listener = listener
    }

    fun setListData(data: MutableList<Comment>) {
        commentList = data
    }

    fun setPid(pid: Int) {
        this.pid = pid
    }

    // 아이템 레이아웃과 결합
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ListAdapter2.ViewHolder {
        mContext = parent.context

        val view = LayoutInflater.from(mContext).inflate(R.layout.list_comment_item, parent, false)
        return ViewHolder(view)
    }

    // View에 내용 입력
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val comment: Comment = commentList[position]
        val pid_comment_list = ArrayList<Comment>()
        var viewHolder = (holder as ViewHolder).itemView
        var index: Int = 0

        println(" ")
        println(" ")
        println("position : " + position)
        println(" ")
        println(" ")

        for (i in 0..commentList.size-1) {
            if (commentList[i].pid == pid) {
                pid_comment_list.add(commentList[i])

                /*println(" ")
                println("i : " + i)
                println("bind_comment[$index] : " + pid_comment_list[index].body)
                println(" ")*/

                index++
            }
        }

        if (pid_comment_index < index) {
            holder.bind(pid_comment_list[pid_comment_index++])

            println(" ")
            println(" ")
            // println("After Bind → pid_comment_index: $pid_comment_index")
            println("After Bind → comment[${pid_comment_index-1}]: " + pid_comment_list[pid_comment_index-1].body)
            println(" ")
            println(" ")
        }
    }

    // 리스트 내 아이템 개수
    override fun getItemCount(): Int {
        return commentList.size
    }

    // 레이아웃 내 View 연결
    inner class ViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        //val comment_cardView: ConstraintLayout = itemView.findViewById(R.id.comment_cardView)
        val comment_image: ImageView = itemView.findViewById(R.id.comment_image)
        val comment_nickname: TextView = itemView.findViewById(R.id.comment_nickname)
        val comment_time: TextView = itemView.findViewById(R.id.comment_time)
        val comment_body: TextView = itemView.findViewById(R.id.comment_body)

        // onBindViewHolder에서 호출
        fun bind(item: Comment) {
            /*var path: String = item.imageUrl.toString()
            var storage = Firebase.storage
            var gsRef = storage.getReferenceFromUrl(path)

            // 이미지
            gsRef.downloadUrl.addOnCompleteListener {
                if (it.isSuccessful) {
                    GlideApp.with(itemView)
                        .load(it.result)
                        .into(comment_image)
                }
            }*/

            // uid.text = item.uid                                   // uid
            if (item.pid == pid) {
                comment_nickname.text = item.nickname                // 이름
                comment_time.text = item.time.toString()   // 시간
                comment_body.text = item.body

                itemView.visibility = VISIBLE
            }
        }
    }
}