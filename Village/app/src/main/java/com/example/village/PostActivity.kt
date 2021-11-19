package com.example.village

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.posting)

        btnHeart = findViewById(R.id.btnHeart)
        btnHeart.setOnClickListener {
            // 버튼 이미지가 채워진 하트로 변경
            // 좋아요 카운트 + - (currentUser? 인가 그거 쓰던데)
            // 그리고 Transaction.. 일단 리스트 어답터에서 값 가져온 다음에
            // 포스트는 pid나 uid로 검색해서 인텐트로 좋아요랑 조회수 포함한 값들을
            // 인텐트로 넘겨줘야 하나..
            // 일단 회원가입 로그인부터 좀 합쳐야 할 듯
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