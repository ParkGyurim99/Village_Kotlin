package com.example.village

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.village.model.Comment

class ListViewModel2 : ViewModel() {
    private val repo = Repository2()

    fun fetchData(): LiveData<MutableList<Comment>> {
        val mutableData = MutableLiveData<MutableList<Comment>>()

        repo.getData().observeForever{
            mutableData.value = it
        }

        return mutableData
    }
}