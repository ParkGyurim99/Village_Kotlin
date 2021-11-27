package com.example.village

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.village.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_app_main.*
import kotlinx.android.synthetic.main.activity_user_info.*
import java.text.SimpleDateFormat
import java.util.*

class UserInfoActivity : AppCompatActivity() {

    private var auth : FirebaseAuth? = null
    private var fbFirestore : FirebaseFirestore? = null
    private var fbStorage : FirebaseStorage? = null
    lateinit var UserInfoName : EditText
    lateinit var UserInfoPhoneNum : EditText
    lateinit var UserInfoEmail : TextView
    lateinit var UserInfoAdress : EditText
    lateinit var UserInfoReviseBtn : Button
    lateinit var UserProfileImage : ImageView
    lateinit var logOutBtnImg : ImageView
    val Gallery = 0
    var uriPhoto : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        UserInfoEmail = findViewById<TextView>(R.id.UserInfoEmail)
        UserInfoName = findViewById<EditText>(R.id.UserInfoName)
        UserInfoPhoneNum = findViewById<EditText>(R.id.UserInfoPhoneNum)
        UserInfoAdress = findViewById<EditText>(R.id.UserInfoAdress)
        UserProfileImage = findViewById(R.id.profileImageView)
        UserInfoReviseBtn = findViewById<Button>(R.id.UserInfoReviseBtn)
        logOutBtnImg = findViewById<ImageView>(R.id.logOutBtnImg)
        //Initialize Firebase Storage
        auth = Firebase.auth
        fbFirestore = FirebaseFirestore.getInstance()
        fbStorage = FirebaseStorage.getInstance()

        var userInfo : UserModel = UserModel()

        checkUser(auth)
        getUserInfoFromFS(auth!!,userInfo)

        UserProfileImage.setOnClickListener {
            loadImageFromGallery()
        }
        UserInfoReviseBtn.setOnClickListener {
            uploadUserInfo(userInfo)
        }
        logOutBtnImg.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            if(auth?.currentUser != null) {
                auth?.signOut()
                goLoginActivity()
            }else{
                goLoginActivity()
            }
        }

    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val (alertDialog,dial_text,dial_btn) = setDialog()

        dial_btn.setOnClickListener {
            alertDialog.dismiss()    // 대화상자를 닫는 함수
        }
        if(requestCode == Gallery){
            if (resultCode == RESULT_OK){
                var dataUri = data?.data
                uriPhoto = data?.data
                try{
                    var bitmap : Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,dataUri)
                    UserProfileImage.setImageBitmap(bitmap)
                }catch (e:Exception){
                    dial_text.setText(e.message.toString())
                    alertDialog.show()
                }
            }else{
                dial_text.setText("사진 불러오기에 실패했습니다.")
                alertDialog.show()
            }
        }
    }
    private fun uploadUserInfo(userInfo: UserModel){
        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imgFileName = "IMAGE_" + timeStamp + "_.png"
        var storageRef = fbStorage?.reference?.child("profile_images")?.child(imgFileName)

        val (alertDialog,dial_text,dial_btn) = setDialog()

        dial_btn.setOnClickListener {
            alertDialog.dismiss()    // 대화상자를 닫는 함수
        }

        if(userInfo.userId.toString() != UserInfoEmail.text.toString() )
            userInfo.userId =  UserInfoEmail.text.toString()
        if(userInfo.userName != UserInfoName.text.toString())
            userInfo.userName =  UserInfoName.text.toString()
        if(userInfo.adress != UserInfoAdress.text.toString())
            userInfo.adress = UserInfoAdress.text.toString()
        if(userInfo.userPhoneNo != UserInfoPhoneNum.text.toString())
            userInfo.userPhoneNo = UserInfoPhoneNum.text.toString()
        userInfo.uid = auth?.uid.toString()
        if(uriPhoto != null){
            storageRef?.putFile(uriPhoto!!)?.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    userInfo.imageUrl = uri.toString()
                    fbFirestore?.collection("users")?.document(auth?.uid.toString())?.set(userInfo)
                    dial_text.setText("회원 정보가 수정되었습니다.")
                    alertDialog.show()
                }
            }
        }else{
            userInfo.imageUrl = null
            fbFirestore?.collection("users")?.document(auth?.uid.toString())?.set(userInfo)
            dial_text.setText("회원 정보가 수정되었습니다.")
            alertDialog.show()
        }
    }

    private fun loadImageFromGallery(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"Load Picture"),Gallery)
    }
    private fun setUserInfo(userModel: UserModel){

        if (userModel.userName != null){
            UserInfoName.setText(userModel.userName)
        }
        if (userModel.userPhoneNo != null){
            UserInfoPhoneNum.setText(userModel.userPhoneNo)
        }
        if (userModel.userId != null){
            UserInfoEmail.setText(userModel.userId)
        }
        if (userModel.adress != null){
            UserInfoAdress.setText(userModel.adress)
        }

    }
    private fun getImageFromStorage(userInfo: UserModel){

        var gsRef = fbStorage?.getReferenceFromUrl(userInfo.imageUrl.toString())

        gsRef?.downloadUrl?.addOnCompleteListener {
            if (it.isSuccessful) {
                GlideApp.with(this)
                    .load(it.result)
                    .into(UserProfileImage)
            }
        }
    }
    private fun getUserInfoFromFS(auth: FirebaseAuth , userInfo : UserModel) {
        fbFirestore!!
            .collection("users")
            .document(auth?.currentUser?.uid!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if(documentSnapshot != null) {
                    Log.d("success : ",documentSnapshot.toString())
                    if(documentSnapshot.data != null) {
                        userInfo.userName = documentSnapshot.get("userName") as? String ?: null
                        userInfo.userPhoneNo = documentSnapshot.get("userPhoneNo") as? String ?: null
                        userInfo.userId =  documentSnapshot.get("userId") as? String ?: null
                        userInfo.imageUrl =  documentSnapshot.get("imageUrl") as? String ?: null
                        userInfo.adress =  documentSnapshot.get("adress") as? String ?: null
                        setUserInfo(userInfo)
                        if(userInfo.imageUrl != null)
                            getImageFromStorage(userInfo)
                    }
                    return@addOnSuccessListener
                }
            }
    }

    private fun checkUser(auth: FirebaseAuth?){
        if(auth?.currentUser == null){
            auth?.signOut()
            goLoginActivity()
        }
    }
    private fun goLoginActivity(){
        val intent = Intent(this,LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // history 지워서 로그인 페이지로 보내
        startActivity(intent)
        finish()
    }
    private fun setDialog() : Triple<AlertDialog,TextView,TextView>{
        //set Dialog
        val view = View.inflate(this,R.layout.dialog,null)
        val builder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        val dial_text = view.findViewById<TextView>(R.id.dial_text)
        val dial_btn = view.findViewById<TextView>(R.id.dial_btn)
        return  Triple(alertDialog,dial_text,dial_btn)
    }

}