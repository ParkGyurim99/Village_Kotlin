package com.example.village.model

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class Post(var uid : String? = null,
                var pid : String? = null,
                var nickname : String? = null,
                var imageUrl : String? = null,
                var title : String? = null,
                var category : String? = null,
                var location : String? = null,
                var lat : Double? = null,
                var lng : Double? = null,
                var timestamp : Long? = null,
                var time : String? = null,
                var price : Int? = null,
                var body : String? = null,
                var likeCount : Int = 0,
                var viewCount : Int = 0) : Serializable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "pid" to pid,
            "nickname" to nickname,
            "imageUrl" to imageUrl,
            "title" to title,
            "category" to category,
            "location" to location,
            "lat" to lat,
            "lng" to lng,
            "timestamp" to timestamp,
            "time" to time,
            "price" to price,
            "body" to body,
            "likeCount" to likeCount,
            "viewCount" to viewCount
        )
    }
}