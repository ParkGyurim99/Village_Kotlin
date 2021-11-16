package com.example.team_project_0_posting

data class Post(var userId : String? = null,
                var imageUrl : String? = null,
                var title : String? = null,
                var timestamp : Long? = null,
                var price : String? = null,
                var body : String? = null) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "imageUrl" to imageUrl,
            "title" to title,
            "timestamp" to timestamp,
            "price" to price,
            "body" to body
        )
    }
}
