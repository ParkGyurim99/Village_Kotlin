package com.example.village

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.village.model.Comment
import com.example.village.model.Post
import com.example.village.model.UserModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.*

class PostActivity : AppCompatActivity() {
    lateinit var ivGoods_p : ImageView
    lateinit var ivProfile : ImageView
    lateinit var tvName : TextView
    lateinit var tvLocation : TextView
    lateinit var tvTitle : TextView
    //lateinit var tvLikes : TextView
    //lateinit var tvViews : TextView
    lateinit var tvTime : TextView
    lateinit var tvCategory : TextView
    lateinit var tvBody : TextView
    lateinit var btnHeart : ImageButton
    lateinit var tvPrice : TextView
    lateinit var etComment : EditText
    lateinit var btnWriteComment : Button
    lateinit var btnChat : Button

    /* 데이터베이스 */
    var database = FirebaseFirestore.getInstance()

    /* 현재 로그인한 사용자 */
    val user = Firebase.auth.currentUser

    /* 리사이클러 뷰 */
    private lateinit var adapter : ListAdapter2
    private val viewModel by lazy { ViewModelProvider(this).get(ListViewModel2::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.posting)

        ivGoods_p = findViewById(R.id.ivGoods_p)
        tvName = findViewById(R.id.tvName)
        tvLocation = findViewById(R.id.tvLocation)
        tvTitle = findViewById(R.id.tvTitle)
        //tvLikes = findViewById(R.id.tvLikes)
        //tvViews = findViewById(R.id.tvViews)
        tvTime = findViewById(R.id.tvTime)
        tvName = findViewById(R.id.tvName)
        tvCategory = findViewById(R.id.tvCategory)
        tvBody = findViewById(R.id.tvBody)
        tvPrice = findViewById(R.id.tvPrice)
        btnHeart = findViewById(R.id.btnHeart)
        etComment = findViewById(R.id.etComment)
        btnWriteComment = findViewById(R.id.btnWriteComment)

        // 뒤로 가기 버튼
        var btnReturn = findViewById<ImageButton>(R.id.btnReturn)
        btnReturn.setOnClickListener {
            finish()
        }

        // 홈으로 가기 버튼
        var btnHome = findViewById<ImageButton>(R.id.btnHome)
        btnHome.setOnClickListener {
            var intent = Intent(applicationContext, AppMainActivity::class.java)
            startActivity(intent)
        }

        // 이미지 테두리 둥그래지는지 테스트
        var ivProfile = findViewById<ImageView>(R.id.ivProfile)
        ivProfile.clipToOutline = true

        /* 글 내용 표시 */
        val intentPost = intent.getSerializableExtra("user-posts") as Post
        val postPosition = intent.getIntExtra("pid", 0)

        // 이미지 (얘도 스토리지에서 바로 가져오는 방식으로..)
        var path: String = intentPost.imageUrl.toString()
        var storage = Firebase.storage
        var gsRef = storage.getReferenceFromUrl(path)

        gsRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                GlideApp.with(this)
                    .load(it.result)
                    .into(ivGoods_p)
            }
        }

        ivGoods_p.clipToOutline = true

        tvName.text = intentPost.nickname                   // 이름
        tvTitle.text = intentPost.title                     // 제목
        tvTime.text = intentPost.time.toString()            // 시간
        tvPrice.text = intentPost.price.toString() + "원"   // 가격
        tvBody.text = intentPost.body.toString()            // 내용
        //tvLikes.text = intentPost.likeCount.toString()    // 좋아요 수
        //tvViews.text = intentPost.viewCount.toString()    // 조회수
        // tvLocation.text = intentPost.location            // 장소
        // tvCategory.text = intentPost.category            // 카테고리

        var flag: Int = 0
        btnHeart.setOnClickListener {
            flag = 1 - flag

            if (flag == 1) {
                btnHeart.setImageResource(R.drawable.btn_heart)
            }

            else {
                btnHeart.setImageResource(R.drawable.btn_heart_empty)
            }
        }

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
            // document(uid)

            finish()    // 포스트 내에서 새로고침 하니까 댓글이 안 보이는데..
        }

        adapter = ListAdapter2()

        adapter.setPid(postPosition)    // pid 값 전달

        // 리사이클러 뷰
        val recyclerView : RecyclerView = findViewById(R.id.recyclerView_2)

        recyclerView.layoutManager = LinearLayoutManager(this)  // 리사이클러 뷰 방향 등을 설정
        recyclerView.adapter = adapter  // 어댑터 장착
        recyclerView.isNestedScrollingEnabled = false   // 리사이클러뷰의 스크롤을 없앰
        observerData()

        // 리사이클러 뷰 새로고침
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh_2)
        swipeRefresh.setOnRefreshListener {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            observerData()

            swipeRefresh.isRefreshing = false   // 새로고침 (왜 안 뜨지)
        }
    }

    // 리사이클러 뷰
    fun observerData() {
        viewModel.fetchData().observe(this, Observer {
            adapter.setListData(it)
            adapter.notifyDataSetChanged()
        })
    }

    // DB에 댓글 올리는 함수
    private fun commentUpload(nickname: String, uid: String, body: String, pid: Int) {
        var newComment = Comment()

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

        database.collection("user-comments")
            .add(newComment)
            .addOnSuccessListener {
                Log.w("PostActivity", "DB 업로드 성공")
            }
            .addOnFailureListener { exception ->
                Log.w("PostActivity", "Error getting documents: $exception")
            }
    }
}