package com.example.village

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.LocationListener

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.example.village.databinding.ActivityMapsBinding
import com.example.village.model.Post
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.list_item.view.*
import java.io.IOException
import java.util.*
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    /*private var items = listOf<Post>(
        Post("1", "1", "nickname1", "url1", "item1", "category", "location", 1, "12:00", 5000, "body1", 0),
        Post("2", "2", "nickname2", "url2", "item2", "category", "location", 1, "10:00", 9900, "body2", 0),
        Post("3", "3", "nickname3", "url3", "item3", "category", "location", 1, "09:00", 8000, "body3", 0)
    )*/
    private var locations = listOf<LatLng>(LatLng(35.887556, 128.612727), LatLng(35.886083, 128.612524), LatLng(35.885092, 128.613795))

    /* 데이터베이스 */
    var database = FirebaseFirestore.getInstance()

    /* 스토리지 */
    var storage = Firebase.storage

    private var itemList: MutableList<Post> = mutableListOf<Post>() // 아이템 리스트

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var now : LatLng? = null
    private var currentMarker : Marker? = null

    // 포스트 가져와서 배열 만들기
    init {
        database.collection("user-posts").orderBy("timestamp").get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    for (document in documentSnapshot) {
                        val getData = document.toObject<Post>()

                        itemList.add(getData)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("DocSnippets", "Error getting documents: ", exception)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Item around Me"

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var btn = findViewById<ImageButton>(R.id.imageButton1)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)
        { return }

        btn.setOnClickListener {
            /*fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    var latitute = location!!.latitude
                    var longitute = location!!.longitude
                    locationText.setText("현재 위치 - 위도 : $latitute / 경도 : $longitute")
                    now = LatLng(location!!.latitude, location.longitude)
                }*/
            getLocation()
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // 아이템 위치 표시
    override fun onMapReady(googleMap: GoogleMap) {
        var mGeocoder = Geocoder(applicationContext, Locale.KOREAN)
        var latLng : LatLng? = null
        var marker : Marker? = null

        mMap = googleMap

        // 포스트 개수만큼 마커 띄우기
        for (i in 0..(itemList.size-1)) {
            val latLng = LatLng(itemList[i].lat!!, itemList[i].lng!!)

            println("${itemList[i].lat}")
            println("${itemList[i].lng}")

            val address = itemList[i].location!!
            marker = mMap.addMarker(MarkerOptions().position(latLng).title("${itemList[i].title}").snippet("$address"))

            marker.tag = itemList[i].imageUrl + "#" +    // index 0번
                    itemList[i].price.toString() + "#" + // index 1번
                    itemList[i].time.toString() + "#" +  // index 2번
                    i.toString()
        }

        // 아이템 마커 클릭 리스너: 마커 클릭하면 카드뷰 띄움
        mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker?): Boolean {
                try {
                    var maps_iteminfo = findViewById<View>(R.id.maps_iteminfo)
                    var image = maps_iteminfo.findViewById<ImageView>(R.id.image)
                    var title = maps_iteminfo.findViewById<TextView>(R.id.title)
                    var location = maps_iteminfo.findViewById<TextView>(R.id.location)
                    var price = maps_iteminfo.findViewById<TextView>(R.id.price)
                    var time = maps_iteminfo.findViewById<TextView>(R.id.time)

                    var markerTagList = marker!!.tag.toString()
                        .split("#")    // 마커에 붙인 태그 [0]: 이미지, [1]: 가격, [2]: pos

                    maps_iteminfo.visibility = View.VISIBLE

                    // 이미지
                    var path: String = markerTagList[0]
                    var gsRef = storage.getReferenceFromUrl(path)

                    gsRef.downloadUrl.addOnCompleteListener {
                        if (it.isSuccessful) {
                            GlideApp.with(this@MapsActivity)
                                .load(it.result)
                                .into(image)
                        }
                    }

                    image.clipToOutline = true

                    title.text = marker!!.title
                    location.text = marker.snippet
                    price.text = markerTagList[1] + "원"
                    time.text = markerTagList[2]

                    var index = markerTagList[3].toInt()

                    // 카드뷰 클릭시 상세 페이지로 이동
                    maps_iteminfo.setOnClickListener {
                        var intent = Intent(applicationContext, PostActivity::class.java)
                        intent.putExtra("user-posts", itemList[index])
                        intent.putExtra("pid", index)
                        startActivity(intent)
                    }
                } catch (e: IllegalArgumentException) { // 현재 위치 마커 생성 → 마커 클릭해도 카드뷰 안 뜨도록
                    e.printStackTrace()
                    maps_iteminfo.visibility = View.GONE
                }

                return false
            }
        })

        //맵 클릭 리스너: 맵 클릭하면 카드뷰 없어짐
        mMap.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
            override fun onMapClick(latLng: LatLng) {
                var maps_iteminfo = findViewById<View>(R.id.maps_iteminfo)

                maps_iteminfo.visibility = View.GONE
            }
        })

        now = LatLng(itemList[0].lat!!, itemList[0].lng!!)  // 지도 화면 나갔다가 들어오면 여기서 IndexOutOfBoundsException 발생
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(now, 18.0f))
    }

    fun getLocation() {
        var locationText = findViewById<TextView>(R.id.locationText)
        var locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        var locationListener = object : LocationListener {
            override fun onLocationChanged(p0: Location) {
                val latLng = LatLng(p0!!.latitude, p0!!.longitude)  //locationText.setText("현재 위치 - 위도 : ${p0.latitude} / 경도 : ${p0.longitude}")
                var mGeocoder = Geocoder(applicationContext, Locale.KOREAN)
                var mResultList : List<Address>? = null

                try {
                    //mResultList = mGeocoder.getFromLocation(p0.latitude, p0.longitude, 1)
                    mResultList = mGeocoder.getFromLocation(p0!!.latitude, p0!!.longitude, 1)
                } catch (e : IOException) {
                    e.printStackTrace()
                }

                if (mResultList != null) {
                    Log.d("Check Current Location", mResultList[0].getAddressLine(0))
                    locationText.setText(mResultList[0].getAddressLine(0).substring(11, 17))
                }

                // 현재 위치 → 마커 없을 때 새로 생성
                if (currentMarker == null) {
                    val markerOptions = MarkerOptions()

                    markerOptions.position(latLng)
                    markerOptions.title("Current Location")

                    val bitmapdraw = resources.getDrawable(R.drawable.ic_currentlocation) as BitmapDrawable
                    val b = bitmapdraw.bitmap
                    val smallMarker = Bitmap.createScaledBitmap(b, 130, 130, false)
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))

                    //markerOptions.icon(BitmapDescriptorFactory.fromResource(smallMarker));

                    //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_user_location)) // 마커 아이콘 수정
                    //markerOptions.rotation(p0.bearing)
                    //markerOptions.anchor(0.5.toFloat(), 0.5.toFloat())
                    currentMarker = mMap.addMarker(markerOptions)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f))
                } else {
                    // 마커 이미 있을때 위치 변경
                    currentMarker!!.position = latLng
                    //currentMarker!!.rotation = p0.bearing
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }
            override fun onProviderEnabled(provider: String) { }
            override fun onProviderDisabled(provider: String) { }
        }

        try {
            locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
        } catch (ex:SecurityException) {
            Toast.makeText(applicationContext, "Error!", Toast.LENGTH_SHORT).show()
        }
    }
}