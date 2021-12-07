package com.example.village

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.village.model.Post
import com.example.village.model.UserModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.dialog.*
import java.io.IOException
import java.lang.Exception
import java.lang.NumberFormatException
import java.util.*

class WriteActivity : AppCompatActivity() {
    lateinit var btnWrite : Button
    lateinit var btnUploadPic : ImageButton
    lateinit var etTitle : EditText
    lateinit var btnCategory : Button
    lateinit var etPrice : EditText
    lateinit var etBody : EditText
    lateinit var ivGoods_w : ImageView
    var imgUri: Uri? = null

    /* 데이터베이스 */
    var database = FirebaseFirestore.getInstance()

    /* 스토리지 */
    var storage = Firebase.storage
    var storageRef = storage.reference

    /* 현재 로그인한 사용자 */
    val user = Firebase.auth.currentUser

    /* 현재 위치 */
    var location : String? = null
    var lat : Double? = null
    var lng : Double? = null

    /* 카테고리 받을 변수 */
    var category : String? = null

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

        // 갤러리 열고, 사진 고르면 표시해주는 버튼
        btnUploadPic.setOnClickListener {
            openGallery()
        }

        getLocation()


        val (alertDialog, dial_text, dial_btn) = setDialog()
        dial_btn.setOnClickListener {
            alertDialog.dismiss()    // 대화상자를 닫는 함수
        }

        // 작성 완료 버튼 누를 시, DB에 포스트 업로드
        btnWrite.setOnClickListener {


            try {
                // 칸이 비어있는지 먼저 검사
                var title = etTitle.text.toString()
                var category = btnCategory.text.toString()
                var body = etBody.text.toString()

                if (imgUri == null || title.isNullOrBlank() || category.isNullOrBlank() || body.isNullOrBlank()) {
                    throw NullPointerException("NullPointerException")
                }

                // 닉네임과 uid, price 저장
                var uid = user!!.uid
                var nickname: String? = null

                var price = etPrice.text.toString().toInt()

                // 같은 uid를 가진 사용자 닉네임 DB에서 가져오기
                database.collection("users").get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot != null) {
                            for (document in documentSnapshot) {
                                val getData = document.toObject<UserModel>()

                                if (getData!!.uid.contentEquals(uid)) {
                                    nickname = getData.userName.toString()

                                    postUpload(nickname!!, uid, imgUri, title, category, price, body)
                                    break
                                }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("DocSnippets", "Error getting documents: ", exception)
                    }

            } catch (e: NullPointerException) {
                dial_text.setText("빈 칸을 모두 채우거나 사진을 선택해주세요.")
                alertDialog.show()

                return@setOnClickListener
            } catch (e: NumberFormatException) {
                dial_text.setText("가격은 숫자로 적어주세요.")
                alertDialog.show()

                return@setOnClickListener
            }

            finish()
        }

        // 뒤로 가기 버튼
        var btnReturn = findViewById<ImageButton>(R.id.btnReturn)
        btnReturn.setOnClickListener {
            finish()
        }

        // 카테고리 버튼
        btnCategory.setOnClickListener {
            var catMenu = PopupMenu(applicationContext, btnCategory)

            menuInflater?.inflate(R.menu.category_menu, catMenu.menu)
            catMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.cat_item1 -> {
                        btnCategory.setText("디지털기기")
                        category = "디지털기기"
                    }

                    R.id.cat_item2 -> {
                        btnCategory.setText("생활가전")
                        category = "생활가전"
                    }

                    R.id.cat_item3 -> {
                        btnCategory.setText("공구")
                        category = "공구"
                    }

                    R.id.cat_item4 -> {
                        btnCategory.setText("스포츠/레저")
                        category = "스포츠/레저"
                    }

                    R.id.cat_item5 -> {
                        btnCategory.setText("도서")
                        category = "도서"
                    }

                    R.id.cat_item6 -> {
                        btnCategory.setText("기타")
                        category = "기타"
                    }
                }

                false
            }

            catMenu.show()
        }
    }

    private fun setDialog() : Triple<AlertDialog, TextView, TextView>{
        val view = View.inflate(this,R.layout.dialog,null)
        val builder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
        builder.setView(view)

        val alertDialog = builder.create()
        alertDialog.setCancelable(false)

        val dial_text = view.findViewById<TextView>(R.id.dial_text)
        val dial_btn = view.findViewById<TextView>(R.id.dial_btn)

        return  Triple(alertDialog, dial_text, dial_btn)
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
    private fun postUpload(nickname: String, uid: String, imageUri: Uri?, title: String, category: String, price: Int, body: String) {
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + ".png"

        var imageRef = storageRef.child("images/").child(imageFileName)

        imageRef?.putFile(imageUri!!).addOnSuccessListener {
            var newPost = Post()

            val long_now = System.currentTimeMillis()
            val t_date = Date(long_now)     // 현재 시간을 Date 타입으로 변환
            val t_dateFormat = SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale("ko", "KR"))

            var locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

            // 닉네임과 uid도 같이 저장
            newPost.nickname = nickname
            newPost.uid = uid

            newPost.imageUrl = "gs://village-e6e1a.appspot.com/images/" + imageFileName
            newPost.title = title
            newPost.price = price
            newPost.body = body
            newPost.timestamp = System.currentTimeMillis()
            newPost.time = t_dateFormat.format(t_date)
            newPost.category  = category
            newPost.lat = lat
            newPost.lng = lng
            newPost.location = location

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

    fun getLocation() {
        var locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        var locationListener = object : LocationListener {
            override fun onLocationChanged(p0: Location) {
                lat = p0!!.latitude
                lng = p0!!.longitude

                var mGeocoder = Geocoder(applicationContext, Locale.KOREAN)
                var mResultList : List<Address>? = null

                try {
                    mResultList = mGeocoder.getFromLocation(p0.latitude, p0.longitude, 1)
                } catch (e : IOException) {
                    e.printStackTrace()
                }

                if (mResultList != null) {
                    Log.d("Check Current Location", mResultList[0].getAddressLine(0))
                    location = mResultList[0].getAddressLine(0).substring(11, 17)
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }
            override fun onProviderEnabled(provider: String) { }
            override fun onProviderDisabled(provider: String) { }
        }

        try {
            locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
        } catch (ex:SecurityException) {
            Toast.makeText(applicationContext, "Error!", Toast.LENGTH_SHORT).show()
        }
    }
}