package com.example.village

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
    lateinit var tvTime : TextView
    lateinit var tvCategory : TextView
    lateinit var tvBody : TextView
    lateinit var btnHeart : ImageButton
    lateinit var tvPrice : TextView
    lateinit var btnComment : ImageButton

    /* 스토리지 */
    var database = FirebaseFirestore.getInstance()
    var storage = Firebase.storage

    /* 현재 로그인한 사용자 */
    val user = Firebase.auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.posting)

        ivGoods_p = findViewById(R.id.ivGoods_p)
        ivProfile = findViewById(R.id.ivProfile)
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

        val postPosition = intent.getStringExtra("pid") // AppMainActivity에서 받은 pid
        println("postPosition1 : " + postPosition)

        btnComment.setOnClickListener {
            var intent = Intent(applicationContext, CommentActivity::class.java)
            intent.putExtra("pid2", postPosition)
            startActivity(intent)
        }


        /* 글 내용 표시 */
        val intentPost = intent.getSerializableExtra("user-posts") as Post

        // 제품 이미지
        var image_path: String = intentPost.imageUrl.toString()
        var gsRef_image = storage.getReferenceFromUrl(image_path)

        gsRef_image.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                GlideApp.with(this)
                    .load(it.result)
                    .into(ivGoods_p)
            }
        }

        ivGoods_p.clipToOutline = true

        // 사용자(판매자) 프로필 이미지
        var profile_image_path: String? = null

        database.collection("users").document(intentPost.uid!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val getData = document.toObject<UserModel>()

                    if (getData!!.imageUrl != null) {
                        profile_image_path = getData.imageUrl.toString()

                        var gsRef_profile_image = storage.getReferenceFromUrl(profile_image_path!!)

                        gsRef_profile_image.downloadUrl.addOnCompleteListener {
                            if (it.isSuccessful) {
                                GlideApp.with(this)
                                    .load(it.result)
                                    .into(ivProfile)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("DocSnippets", "Error getting documents: ", exception)
            }

        // 사용자 프로필 이미지 테두리 동그랗게
        ivProfile.clipToOutline = true

        tvName.text = intentPost.nickname                   // 이름
        tvTitle.text = intentPost.title                     // 제목
        tvTime.text = intentPost.time.toString()            // 시간
        tvPrice.text = intentPost.price.toString() + "원"   // 가격
        tvBody.text = intentPost.body.toString()            // 내용
        tvLocation.text = intentPost.location            // 장소
        tvCategory.text = intentPost.category            // 카테고리

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