package com.example.village

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var items = listOf<Post>(
        Post("1", "1", "nickname1", "url1", "item1", "category", "location", 1, "12:00", 5000, "body1", 0),
        Post("2", "2", "nickname2", "url2", "item2", "category", "location", 1, "10:00", 9900, "body2", 0),
        Post("3", "3", "nickname3", "url3", "item3", "category", "location", 1, "09:00", 8000, "body3", 0)
    )
    private var locations = listOf<LatLng>(LatLng(35.887556, 128.612727), LatLng(35.886083, 128.612524), LatLng(35.885092, 128.613795))

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var now = LatLng(35.886108, 128.613053)
    private var currentMarker : Marker? = null

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
//            fusedLocationClient.lastLocation
//                .addOnSuccessListener { location: Location? ->
//                    // Got last known location. In some rare situations this can be null.
//                    var latitute = location!!.latitude
//                    var longitute = location!!.longitude
//                    locationText.setText("현재 위치 - 위도 : $latitute / 경도 : $longitute")
//                    now = LatLng(location!!.latitude, location.longitude)
//                }
            getLocation()
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        for (i in 0..2) { mMap.addMarker(MarkerOptions().position(locations[i]).title("Item")) } // items.title

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(now, 18.0f))
    }

    fun getLocation() {
        var locationText = findViewById<TextView>(R.id.locationText)
        var locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        var locationListener = object : LocationListener {
            override fun onLocationChanged(p0: Location) {
                val latLng = LatLng(p0!!.latitude, p0!!.longitude)
                locationText.setText("현재 위치 - 위도 : ${p0.latitude} / 경도 : ${p0.longitude}")
                // 마커 없을 때 새로 생성
                if (currentMarker == null) {
                    val markerOptions = MarkerOptions()
                    markerOptions.position(latLng)
                    markerOptions.title("Current Location")
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