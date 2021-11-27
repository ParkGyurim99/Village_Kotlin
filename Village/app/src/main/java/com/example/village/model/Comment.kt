package com.example.village.model

import java.io.Serializable

class Comment (var uid : String? = null,
               var pid : Int? = null,
               var nickname : String? = null,
               var imageUrl : String? = null,
               var timestamp : Long? = null,
               var time : String? = null,
               var body : String? = null) : Serializable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "pid" to pid,
            "nickname" to nickname,
            "imageUrl" to imageUrl,
            "timestamp" to timestamp,
            "time" to time,
            "body" to body
        )
    }
}
