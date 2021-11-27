package com.example.village

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.village.model.Comment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class Repository2 {
    fun getData(): LiveData<MutableList<Comment>> {
        val mutableData = MutableLiveData<MutableList<Comment>>()
        val database = FirebaseFirestore.getInstance()
        val myRef = database.collection("user-comments")

        myRef.orderBy("timestamp").get()
            .addOnSuccessListener { documentSnapshot ->
                val listData: MutableList<Comment> = mutableListOf<Comment>()

                for (document in documentSnapshot) {
                    val getData = document.toObject<Comment>()

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