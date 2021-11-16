package com.example.team_project_0_posting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListViewModel : ViewModel() {
    private val repo = Repository()

    fun fetchData(): LiveData<MutableList<Post>> {
        val mutableData = MutableLiveData<MutableList<Post>>()

        repo.getData().observeForever{
            mutableData.value = it
        }

        return mutableData
    }
}