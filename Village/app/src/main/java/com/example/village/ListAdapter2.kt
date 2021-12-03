package com.example.village

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.village.model.Comment
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ListAdapter2 : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var commentList = mutableListOf<Comment>()
    private var mContext: Context? = null
    private var pid: Int? = null
    private var pid_comment_list = ArrayList<Comment>()
    private var pid_comment_index: Int = 0  // 포스트마다 달린 댓글의 리스트를 참조할 index

    fun setListData(data: MutableList<Comment>) {
        commentList = data
    }

    fun setPid(pid: Int) {
        this.pid = pid
    }

    // 아이템 레이아웃과 결합
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter2.ViewHolder {
        mContext = parent.context

        val view = LayoutInflater.from(mContext).inflate(R.layout.list_comment_item, parent, false)
        return ViewHolder(view)
    }

    // View에 내용 입력
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var viewHolder = (holder as ViewHolder).itemView

        pid_comment_index = 0   // 초기화

        for (i in 0..commentList.size-1) {
            if (commentList[i].pid == pid) {
                pid_comment_list.add(commentList[i])

                println("pid_comment_list[$pid_comment_index]에 들어가는 댓글 : " + commentList[i].body)

                pid_comment_index++
            }
        }

        if (position < pid_comment_index)
        {
            holder.bind(pid_comment_list[position])

            println(" ")
            println("After Bind → pid_comment_list[${position}]: " + pid_comment_list[position].body)
            println(" ")

            holder.comment_list_layout.visibility = VISIBLE
        }
    }

    // 전체 리스트 내 아이템 개수
    override fun getItemCount(): Int {
        return commentList.size
    }

    // 레이아웃 내 View 연결
    inner class ViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView) {

        val comment_image: ImageView = itemView.findViewById(R.id.comment_image)
        val comment_nickname: TextView = itemView.findViewById(R.id.comment_nickname)
        val comment_time: TextView = itemView.findViewById(R.id.comment_time)
        val comment_body: TextView = itemView.findViewById(R.id.comment_body)
        val comment_list_layout: ConstraintLayout = itemView.findViewById(R.id.comment_list_layout)

        // onBindViewHolder에서 호출
        fun bind(item: Comment) {
            comment_image.clipToOutline = true

            if (item.pid == pid) {
                var path: String = item.imageUrl.toString()

                // 이미지
                GlideApp.with(itemView)
                    .load(path)
                    .into(comment_image)

                comment_nickname.text = item.nickname      // 이름
                comment_time.text = item.time.toString()   // 시간
                comment_body.text = item.body
            }
        }
    }

    fun clearAllItems() {
        pid_comment_list.clear()
        pid_comment_index = 0
    }
}