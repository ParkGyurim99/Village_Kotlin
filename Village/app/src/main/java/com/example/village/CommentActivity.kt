package com.example.village

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.village.model.Comment
import com.example.village.model.UserModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class CommentActivity : AppCompatActivity() {
    lateinit var etComment : EditText
    lateinit var btnWriteComment : Button

    /* 데이터베이스 */
    var database = FirebaseFirestore.getInstance()

    /* 현재 로그인한 사용자 */
    val user = Firebase.auth.currentUser

    /* 리사이클러 뷰 */
    private lateinit var adapter : ListAdapter2
    //private val viewModel by lazy { ViewModelProvider(this).get(ListViewModel2::class.java) }

    var postPosition = 0   // PostActivity에서 받은 pid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        etComment = findViewById(R.id.etComment)
        btnWriteComment = findViewById(R.id.btnWriteComment)

        // 뒤로 가기 버튼
        var btnReturn = findViewById<ImageButton>(R.id.btnReturn)
        btnReturn.setOnClickListener {
            finish()
        }

        postPosition = intent.getIntExtra("pid2", 0)

        // 댓글 작성 완료 버튼 누를 시, DB에 댓글 업로드
        btnWriteComment.setOnClickListener {
            var body = etComment.text.toString()

            // 닉네임과 uid도 같이 저장
            var uid = user!!.uid
            var nickname: String = "실패"

            // 같은 uid를 가진 사용자 닉네임 DB에서 가져오기
            database.collection("users").get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {
                        for (document in documentSnapshot) {
                            val getData = document.toObject<UserModel>()

                            if (getData!!.uid.contentEquals(uid)) {
                                nickname = getData.userName.toString()

                                commentUpload(nickname!!, uid, body, postPosition) // 리스너 안에다 넣어야 되네..
                                break
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("DocSnippets", "Error getting documents: ", exception)
                }

            etComment.setText("")
            closeKeyboard()
        }

        adapter = ListAdapter2()

        adapter.setPid(postPosition)    // pid 값 전달

        // 리사이클러 뷰
        val recyclerView : RecyclerView = findViewById(R.id.recyclerView_2)
        val manager = LinearLayoutManager(this)

        recyclerView.layoutManager = manager // 리사이클러 뷰 방향 등을 설정
        recyclerView.adapter = adapter  // 어댑터 장착
        adapter.clearAllItems()

        observerData()

        // 리사이클러 뷰 새로고침
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh_2)
        swipeRefresh.setOnRefreshListener {
            adapter.clearAllItems()
            observerData()

            swipeRefresh.isRefreshing = false
        }
    }

    // DB에 댓글 올리는 함수
    private fun commentUpload(nickname: String, uid: String, body: String, pid: Int) {
        var newComment = Comment()
        //var repo2 = Repository2()

        val long_now = System.currentTimeMillis()
        val t_date = Date(long_now)     // 현재 시간을 Date 타입으로 변환
        val t_dateFormat = SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale("ko", "KR"))

        // 닉네임과 uid도 같이 저장
        newComment.nickname = nickname
        newComment.uid = uid
        newComment.pid = pid
        newComment.body = body
        newComment.timestamp = System.currentTimeMillis()
        newComment.time = t_dateFormat.format(t_date).toString()

        //var repo2 = Repository2()
        //repo2.setPid(pid)   // Repository에 pid값 전달

        database.collection("user-comments")
            .add(newComment)
            .addOnSuccessListener {
                Log.w("PostActivity", "DB 업로드 성공")
            }
            .addOnFailureListener { exception ->
                Log.w("PostActivity", "Error getting documents: $exception")
            }
    }

    // 기존 Repository2.kt
    fun getData(): LiveData<MutableList<Comment>> {
        val mutableData = MutableLiveData<MutableList<Comment>>()
        val database = FirebaseFirestore.getInstance()
        val myRef = database.collection("user-comments")

        myRef.orderBy("timestamp").get()
            .addOnSuccessListener { documentSnapshot ->
                val listData: MutableList<Comment> = mutableListOf<Comment>()

                for (document in documentSnapshot) {
                    val getData = document.toObject<Comment>()

                    /*listData.add(getData!!)

                    println("getData.pid: ${getData.pid}")

                    mutableData.value = listData*/

                    if (postPosition == getData.pid) {
                        listData.add(getData!!)

                        println("getData.pid: ${getData.pid}")

                        mutableData.value = listData
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("DocSnippets", "Error getting documents: ", exception)
            }

        return mutableData
    }

    // 기존 ListViewModel2.kt
    fun fetchData(): LiveData<MutableList<Comment>> {
        val mutableData = MutableLiveData<MutableList<Comment>>()

        getData().observeForever{
            mutableData.value = it
        }

        return mutableData
    }

    // 리사이클러 뷰
    fun observerData() {
        fetchData().observe(this, Observer {
            adapter.setListData(it)
            adapter.notifyDataSetChanged()
        })
    }

    // 작성 완료 시 키보드 닫기
    fun closeKeyboard()
    {
        var view = this.currentFocus

        if(view != null)
        {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}