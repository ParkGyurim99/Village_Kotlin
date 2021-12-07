package com.example.village

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.village.databinding.ActivityAppMainBinding
import com.example.village.model.Post
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_app_main.*
import java.io.IOException
import java.util.*
val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
val PERMISSIONS_REQUEST_CODE = 100
class AppMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppMainBinding
    lateinit var auth: FirebaseAuth

    /* 리사이클러 뷰 */
    private lateinit var adapter: ListAdapter
    private val viewModel by lazy { ViewModelProvider(this).get(ListViewModel::class.java) }
    var locationManager : LocationManager? = null
    private val REQUEST_CODE_LOCATION : Int = 2
    var currentLocation : String = "북구 산격동"
    var catecoryName1 : String = "디지털기기"
    var catecoryName2 : String = "생활필수품"
    var catecoryName3 : String = "공구"
    var catecoryName4 : String = "스포츠/레저"
    var catecoryName5 : String = "도서"
    var catecoryName6 : String = "기타"
    var latitude : Double? = null
    var longitude : Double? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView.adapter = ListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        auth = Firebase.auth

        checkUser(auth)

        /* 위치 권한 허용 요청 */
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                }
                else -> {
                    // No location access granted.
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )


        val btn_map: ImageView = findViewById<ImageView>(R.id.btn_map)
        val btn_search: ImageButton = findViewById<ImageButton>(R.id.btn_search)
        var searchWord: EditText = findViewById<EditText>(R.id.searchWord)
        val btn_write: ImageButton = findViewById<ImageButton>(R.id.btn_write)
        val btn_userInfo: ImageButton = findViewById<ImageButton>(R.id.btn_userInfo)
        val btn_home: ImageButton = findViewById<ImageButton>(R.id.btn_home)
        val btn_findLocation: ImageButton = findViewById<ImageButton>(R.id.btn_findLocation)
        val btn_category1: Button = findViewById<Button>(R.id.btn_category1)
        val btn_category2: Button = findViewById<Button>(R.id.btn_category2)
        val btn_category3: Button = findViewById<Button>(R.id.btn_category3)
        val btn_category4: Button = findViewById<Button>(R.id.btn_category4)
        val btn_category5: Button = findViewById<Button>(R.id.btn_category5)
        val btn_category6: Button = findViewById<Button>(R.id.btn_category6)
        var tv_mylocation: TextView = findViewById<TextView>(R.id.tv_mylocation)

        var searchOption = "title"
        var myLocationOption = "location"
        var categoryOption = "category"

        binding.btnSearch.setOnClickListener {
            (recyclerView.adapter as ListAdapter).search(searchWord.text.toString(), searchOption)
        }

        btn_map.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("cLocation", currentLocation)
            /* 에뮬레이터 실행시 스태틱 값 전달 */
            //intent.putExtra("cLocation", "북구 대현동")
            startActivity(intent)
        }

        btn_write.setOnClickListener {
            val intent = Intent(this, WriteActivity::class.java)
            startActivity(intent)
        }

        btn_userInfo.setOnClickListener {
            val intent = Intent(this, UserInfoActivity::class.java)
            startActivity(intent)
        }

        btn_home.setOnClickListener {
            val intent = Intent(this, AppMainActivity::class.java)
            startActivity(intent.setAction(Intent.ACTION_MAIN) .addCategory(Intent.CATEGORY_LAUNCHER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        binding.btnSearch.setOnClickListener {
            (recyclerView.adapter as ListAdapter).search(searchWord.text.toString(), searchOption)
        }

        binding.btnCategory1.setOnClickListener{
            (recyclerView.adapter as ListAdapter).category(catecoryName1, categoryOption)
        }
        binding.btnCategory2.setOnClickListener{
            (recyclerView.adapter as ListAdapter).category(catecoryName2, categoryOption)
        }
        binding.btnCategory3.setOnClickListener{
            (recyclerView.adapter as ListAdapter).category(catecoryName3, categoryOption)
        }
        binding.btnCategory4.setOnClickListener{
            (recyclerView.adapter as ListAdapter).category(catecoryName4, categoryOption)
        }
        binding.btnCategory5.setOnClickListener{
            (recyclerView.adapter as ListAdapter).category(catecoryName5, categoryOption)
        }
        binding.btnCategory6.setOnClickListener{
            (recyclerView.adapter as ListAdapter).category(catecoryName6, categoryOption)
        }



        binding.btnFindLocation.setOnClickListener {
            Log.d("################", currentLocation)
            getLocation()
            tv_mylocation.text = currentLocation
            Log.d("################", currentLocation)
            (recyclerView.adapter as ListAdapter).refresh(currentLocation, myLocationOption)
        }

        adapter = ListAdapter()

        // 리사이클러 뷰
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)  // 리사이클러 뷰 방향 등을 설정
        recyclerView.adapter = adapter  // 어댑터 장착
        observerData()

        // 리사이클러 뷰 새로고침
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            observerData()

            swipeRefresh.isRefreshing = false   // 새로고침
        }

        adapter.setOnItemClickListener(object :
            ListAdapter.OnItemClickListener {
            override fun onItemClick(v: View, data: Post, pos: Int) {
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

    private fun checkUser(auth: FirebaseAuth?) {
        if (auth?.currentUser == null) {
            auth?.signOut()
            goLoginActivity()
        }
    }

    private fun goLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun getLocation(){
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        var userLocation: Location = getLatLng()
        if(userLocation != null){
            latitude = userLocation.latitude
            longitude = userLocation.longitude
            Log.d("CheckCurrentLocation", "현재 내 위치 값: ${latitude}, ${longitude}")

            var mGeoCoder =  Geocoder(applicationContext, Locale.KOREAN)
            var mResultList: List<Address>? = null
            try{
                mResultList = mGeoCoder.getFromLocation(
                    latitude!!, longitude!!, 1
                )
            }catch(e: IOException){
                e.printStackTrace()
            }
            if(mResultList != null){
                Log.d("CheckCurrentLocation", mResultList[0].getAddressLine(0))
                currentLocation = mResultList[0].getAddressLine(0)
                currentLocation = currentLocation.substring(11,17)
            }
        }
    }
    private fun getLatLng(): Location{
        var currentLatLng: Location? = null
        var hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
        var hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)

        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            val locationProvider = LocationManager.NETWORK_PROVIDER
            currentLatLng = locationManager?.getLastKnownLocation(locationProvider)
        }else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])){
                Toast.makeText(this, "앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
            }else{
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
            }
            currentLatLng = getLatLng()
        }
        return currentLatLng!!
    }
}
