package com.example.village

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.example.village.databinding.ActivityAppMainBinding
import com.example.village.model.Person
import com.example.village.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_app_main.*

class AppMainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAppMainBinding
    lateinit var auth : FirebaseAuth

    /* 리사이클러 뷰 */
    private lateinit var adapter : ListAdapter
    private val viewModel by lazy { ViewModelProvider(this).get(ListViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityAppMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView.adapter = ListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        auth = Firebase.auth
        
        checkUser(auth)

        val btn_map : ImageView = findViewById<ImageView>(R.id.btn_map)
        val btn_search : ImageButton = findViewById<ImageButton>(R.id.btn_search)
        var searchWord : EditText = findViewById<EditText>(R.id.searchWord)
        val btn_write : ImageButton = findViewById<ImageButton>(R.id.btn_write)
        val btn_userInfo : ImageButton = findViewById<ImageButton>(R.id.btn_userInfo)

        var searchOption = "title"
        // val recyclerView : RecyclerView = findViewById(R.id.recyclerview)
        btn_map.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        binding.btnSearch.setOnClickListener {
            (recyclerView.adapter as ListAdapter).search(searchWord.text.toString(), searchOption)
        }

        btn_write.setOnClickListener {
            val intent = Intent(this, WriteActivity::class.java)
            startActivity(intent)
        }

        btn_userInfo.setOnClickListener {
            val intent = Intent(this,UserInfoActivity::class.java)
            startActivity(intent)
        }


        adapter = ListAdapter()

        // 리사이클러 뷰
        val recyclerView : RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)  // 리사이클러 뷰 방향 등을 설정
        recyclerView.adapter = adapter  // 어댑터 장착
        observerData()

        // 리사이클러 뷰 새로고침
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            observerData()

            swipeRefresh.isRefreshing = false   // 새로고침
        }

        adapter.setOnItemClickListener(object :
            ListAdapter.OnItemClickListener{
            override fun onItemClick(v: View, data: Post, pos : Int) {
                Intent(this@AppMainActivity, PostActivity::class.java).apply {
                    putExtra("user-posts", data)
                    putExtra("pid", pos)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { startActivity(this) }
            }
        })
    }

    // 리사이클러 뷰
    fun observerData() {
        viewModel.fetchData().observe(this, Observer {
            adapter.setListData(it)
            adapter.notifyDataSetChanged()
        })
    }

    private fun checkUser(auth: FirebaseAuth?){
        if(auth?.currentUser == null){
            auth?.signOut()
            goLoginActivity()
        }
    }
    private fun goLoginActivity(){
        val intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}