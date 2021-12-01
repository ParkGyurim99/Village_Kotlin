package com.example.village

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.village.model.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

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
    lateinit var btnComment : ImageButton

    /* 데이터베이스 */
    var database = FirebaseFirestore.getInstance()

    /* 현재 로그인한 사용자 */
    val user = Firebase.auth.currentUser

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
        btnComment = findViewById(R.id.btnComment)
        //btnWriteComment = findViewById(R.id.btnWriteComment)

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

        val postPosition = intent.getIntExtra("pid", 0) // AppMainActivity에서 받은 pid

        btnComment.setOnClickListener {
            var intent = Intent(applicationContext, CommentActivity::class.java)
            intent.putExtra("pid2", postPosition)
            startActivity(intent)
        }



        // 이미지 테두리 둥그래지는지 테스트
        var ivProfile = findViewById<ImageView>(R.id.ivProfile)
        ivProfile.clipToOutline = true


        /* 글 내용 표시 */
        val intentPost = intent.getSerializableExtra("user-posts") as Post

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
        // tvLocation.text = intentPost.location            // 장소
        // tvCategory.text = intentPost.category            // 카테고리

        var flag = 0
        btnHeart.setOnClickListener {
            flag = 1 - flag

            if (flag == 1) {
                btnHeart.setImageResource(R.drawable.ic_heart)
            }

            else {
                btnHeart.setImageResource(R.drawable.ic_heart_empty)
            }
        }
    }
}