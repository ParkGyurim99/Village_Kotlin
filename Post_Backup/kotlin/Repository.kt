package com.example.team_project_0_posting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class Repository {
    fun getData(): LiveData<MutableList<Post>> {
        val mutableData = MutableLiveData<MutableList<Post>>()
        val database = FirebaseFirestore.getInstance()
        val myRef = database.collection("user-posts")

        myRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val listData: MutableList<Post> = mutableListOf<Post>()

                for (document in documentSnapshot) {
                    val getData = document.toObject<Post>()

                    listData.add(getData!!)

                    mutableData.value = listData
                }
            }
            .addOnFailureListener { exception ->
                Log.w("DocSnippets", "Error getting documents: ", exception)
            }

        return mutableData
    }
}