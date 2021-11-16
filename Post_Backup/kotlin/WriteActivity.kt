package com.example.team_project_0_posting

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.lang.Exception
import java.util.*

class WriteActivity : AppCompatActivity() {
    lateinit var btnWrite : Button
    lateinit var btnUploadPic : ImageButton
    lateinit var etTitle : EditText
    lateinit var btnCategory : Button
    lateinit var etPrice : EditText
    lateinit var etBody : EditText

    lateinit var test_name : EditText
    lateinit var ivGoods_w : ImageView
    var imgUri: Uri? = null

    /* 데이터베이스 */
    var database = FirebaseFirestore.getInstance()

    /* 스토리지 */
    var storage = Firebase.storage
    var storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.writing)

        btnWrite = findViewById(R.id.btnWrite)
        btnUploadPic = findViewById(R.id.btnUploadPic)
        etTitle = findViewById(R.id.etTitle)
        btnCategory = findViewById(R.id.btnCategory)
        etPrice = findViewById(R.id.etPrice)
        etBody = findViewById(R.id.etBody)

        ivGoods_w = findViewById(R.id.ivGoods_w)
        test_name = findViewById(R.id.test_name)

        // 갤러리 열고, 사진 고르면 표시해주는 버튼
        btnUploadPic.setOnClickListener { 
            openGallery()
        }

        // 작성 완료 버튼 누를 시, DB에 포스트 업로드
        btnWrite.setOnClickListener {
            var title = etTitle.text.toString()
            var price = etPrice.text.toString()
            var body = etBody.text.toString()
            var user_name = test_name.text.toString()

            postUpload(user_name, imgUri, title, price, body)

            finish()
        }

        // 뒤로 가기 버튼
        var btnReturn = findViewById<ImageButton>(R.id.btnReturn)
        btnReturn.setOnClickListener {
            finish()
        }
    }

    // 갤러리 여는 함수
    private fun openGallery() {
        val intent: Intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        startActivityForResult(intent, 1)   // 1 = 갤러리 열기
    }

    // 갤러리에서 가져온 이미지 표시
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == 1) {
                imgUri = data?.data

                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imgUri)
                    ivGoods_w.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        else {
            Log.d("ActivityResult", "Error")
        }
    }

    // DB에 포스트 올리는 함수
    private fun postUpload(userName: String, uri: Uri?, title: String, price: String, body: String) {
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + ".png"

        var imageRef = storageRef.child("images/").child(imageFileName)

        imageRef?.putFile(uri!!).addOnSuccessListener {
            var newPost = Post()

            newPost.userId = userName.toString()
            newPost.imageUrl = imageFileName
            newPost.title = title
            newPost.price = price
            newPost.body = body
            newPost.timestamp = System.currentTimeMillis()

            database.collection("user-posts")
                .add(newPost)
                .addOnSuccessListener {
                    // Toast.makeText(this, "데이터가 추가되었습니다", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Log.w("MainActivity", "Error getting documents: $exception")
                }
        }
    }
}