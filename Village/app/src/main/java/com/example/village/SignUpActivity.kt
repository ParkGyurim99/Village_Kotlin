package com.example.village


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.village.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private var auth : FirebaseAuth? = null
    private var fbFirestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        val signUpBtn = findViewById<Button>(R.id.signUpBtn);
        val signUpUserName = findViewById<EditText>(R.id.signUpUserName)
        val signUpUserEmail = findViewById<EditText>(R.id.signUpUserEmail)
        val signUpUserPwd = findViewById<EditText>(R.id.signUpUserPwd)

        auth = Firebase.auth

        signUpBtn.setOnClickListener {
            signUpBtn.isEnabled = false
            createAccount(signUpUserName.text.toString(),signUpUserEmail.text.toString(),signUpUserPwd.text.toString())
        }
    }
    private fun createAccount(name : String,email: String, password: String) {

        val signUpBtn = findViewById<Button>(R.id.signUpBtn)
        //set Dialog
        val (alertDialog,dial_text,dial_btn) = setDialog()

        dial_btn.setOnClickListener {
            alertDialog.dismiss()    // 대화상자를 닫는 함수
            signUpBtn.isEnabled = true
        }

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnSuccessListener {
                    signUpBtn.isEnabled = true
                    setUserInfoToFireStore(name,auth?.currentUser?.email.toString(),auth?.uid.toString())
                }
                ?.addOnFailureListener { e ->
                    dial_text.setText(e.message)
                    alertDialog.show()
                }
        }else {
            //Fill the blanck
            dial_text.setText("Fill the blanck!")
            alertDialog.show()
        }
    }
    private fun setUserInfoToFireStore(userName : String,userId : String, userUid : String) {
        fbFirestore = FirebaseFirestore.getInstance()
        var userInfo = UserModel()
        userInfo.uid = userUid
        userInfo.userId = userId
        userInfo.userName = userName

        val (alertDialog,dial_text,dial_btn) = setDialog()

        dial_btn.setOnClickListener {
            auth?.signOut()
            alertDialog.dismiss()    // 대화상자를 닫는 함수
            goLoginActivity()
            finish() // 가입창 종료
        }

        fbFirestore!!
            .collection("users")
            .document(userUid)
            .set(userInfo)
            .addOnSuccessListener {
                dial_text.setText("Register Success!")
                alertDialog.show()
            }
            .addOnFailureListener { e ->
                Log.d("alert : ",e.message.toString())
            }
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
    private fun goLoginActivity(){
        println("goLoginActivity")
        val intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
    }
}
