package com.example.village

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.village.model.Post
import com.example.village.model.UserModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PostActivity : AppCompatActivity() {
    lateinit var ivGoods_p : ImageView
    lateinit var ivProfile : ImageView
    lateinit var tvName : TextView
    lateinit var tvLocation : TextView
    lateinit var tvTitle : TextView
    lateinit var tvLikes : TextView
    lateinit var tvViews : TextView
    lateinit var tvTime : TextView
    lateinit var tvCategory : TextView
    lateinit var tvBody : TextView
    lateinit var btnHeart : ImageButton
    lateinit var tvPrice : TextView
    lateinit var btnChat : Button

    /* 데이터베이스 */
    var database = FirebaseFirestore.getInstance()

    /* 스토리지 */
    var storage = Firebase.storage
    var storageRef = storage.reference

    /* 현재 로그인한 사용자 */
    val user = Firebase.auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.posting)

        ivGoods_p = findViewById(R.id.ivGoods_p)
        tvName = findViewById(R.id.tvName)
        tvLocation = findViewById(R.id.tvLocation)
        tvTitle = findViewById(R.id.tvTitle)
        tvLikes = findViewById(R.id.tvLikes)
        tvViews = findViewById(R.id.tvViews)
        tvTime = findViewById(R.id.tvTime)
        tvName = findViewById(R.id.tvName)
        tvCategory = findViewById(R.id.tvCategory)
        tvBody = findViewById(R.id.tvBody)
        tvPrice = findViewById(R.id.tvPrice)
        btnHeart = findViewById(R.id.btnHeart)


        val intentPost = intent.getSerializableExtra("intent-post") as Post

        // 이미지 (이제 얘만 되면 되겠다)
        GlideApp.with(this)
            .load(intentPost.imageUrl)
            .into(ivGoods_p)

        tvName.text = intentPost.nickname               // 이름
        tvTitle.text = intentPost.title                 // 제목
        tvTime.text = intentPost.timestamp.toString()   // 시간
        tvPrice.text = intentPost.price.toString()      // 가격
        tvLikes.text = intentPost.likeCount.toString()  // 좋아요 수
        tvViews.text = intentPost.viewCount.toString()  // 조회수
        tvBody.text = intentPost.body.toString()        // 내용
        // tvLocation.text = intentPost.location        // 장소
        // tvCategory.text = intentPost.category        // 카테고리

        // 좋아요 버튼
        // document() → transaction하면 될 것 같은데.. 필드를 찍어봐야겠다.
        // 그리고 화면 뒤로가기 하면 좋아요 풀림
        // 계속 유지하는 건 어떻게 할 수 없나..
        var flag: Int = 0
        var post = Post()
        var postRef = database.collection("user-post").document()
        btnHeart.setOnClickListener {
            flag = 1 - flag

            if (flag == 1) {
                btnHeart.setImageResource(R.drawable.btn_heart)
            }

            else {
                btnHeart.setImageResource(R.drawable.btn_heart_empty)
            }

            // 버튼 이미지가 채워진 하트로 변경
            // 좋아요 카운트 + - (currentUser? 인가 그거 쓰던데)
            // 그리고 Transaction.. 일단 리스트 어답터에서 값 가져온 다음에
            // 포스트는 pid나 uid로 검색해서 인텐트로 좋아요랑 조회수 포함한 값들을
            // 인텐트로 넘겨줘야 하나..
        }

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
    }
}