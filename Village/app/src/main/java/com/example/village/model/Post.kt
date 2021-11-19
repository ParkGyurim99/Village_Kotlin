package com.example.village.model

data class Post(var uid : String? = null,
                var nickname : String? = null,
                var imageUrl : String? = null,
                var title : String? = null,
                var category : String? = null,
                var location : String? = null,
                var timestamp : Long? = null,
                var price : String? = null,
                var body : String? = null,
                var likeCount : Int = 0,
                var viewCount : Int = 0) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "nickname" to nickname,
            "imageUrl" to imageUrl,
            "title" to title,
            "category" to category,
            "location" to location,
            "timestamp" to timestamp,
            "price" to price,
            "body" to body,
            "likeCount" to likeCount,
            "viewCount" to viewCount
        )
    }
}