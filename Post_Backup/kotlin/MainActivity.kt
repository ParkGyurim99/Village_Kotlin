package com.example.team_project_0_posting

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        var btnWritePost = findViewById<Button>(R.id.btnWritePost)
        btnWritePost.setOnClickListener {
            var intent = Intent(applicationContext, WriteActivity::class.java)
            startActivity(intent)
        }

        var btnPost = findViewById<Button>(R.id.btnPost)
        btnPost.setOnClickListener {
            var intent = Intent(applicationContext, PostActivity::class.java)
            startActivity(intent)
        }
    }
}