package com.example.village

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null){
            goMainActivity()
        }
        val loginBtn = findViewById<Button>(R.id.loginBtn);
        val loginUserEmail = findViewById<EditText>(R.id.loginUserEmail)
        val loginUserPwd = findViewById<EditText>(R.id.loginUserPwd)
        val signUpUrl = findViewById<TextView>(R.id.signUpUrl)

        loginBtn.setOnClickListener {
            loginBtn.isEnabled = false
            val email = loginUserEmail.text.toString().trim()
            val password = loginUserPwd.text.toString().trim()
            //set Dialog
            val (alertDialog,dial_text,dial_btn) = setDialog()

            dial_btn.setOnClickListener {
                alertDialog.dismiss()    // 대화상자를 닫는 함수
            }

            if(email.isBlank() || password.isBlank()){
                loginBtn.isEnabled = true
                dial_text.setText("Fill the Blank!")
                alertDialog.show()
                return@setOnClickListener
            }
            //Firebase Auth Check
            auth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener {
                    loginBtn.isEnabled = true
                    goMainActivity()
                }
                .addOnFailureListener { e ->
                    loginBtn.isEnabled = true
                    dial_text.setText(e.message)
                    alertDialog.show()
                }
        }
        signUpUrl.setOnClickListener {
            goSignUpActivity()
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
    private fun goMainActivity(){
        println("goMainActivity")
        val intent= Intent(this,AppMainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun goSignUpActivity(){
        println("goSignUpActivity")
        val intent = Intent(this,SignUpActivity::class.java)
        startActivity(intent)
    }
}