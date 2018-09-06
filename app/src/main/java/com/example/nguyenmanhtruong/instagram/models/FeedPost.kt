package com.example.nguyenmanhtruong.instagram.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.ServerValue
import java.util.*

data class FeedPost(val uid: String = "", val username: String = "",
                    val image: String = "", val caption: String = "",
                    val comments: List<Comment> = emptyList(),
                    val timestamp: Any = ServerValue.TIMESTAMP, val photo: String? = null,
                    @Exclude val id: String = "") {
    // save -> Firebase puts Long value
    // get <- Long
    fun timestampDate(): Date = Date(timestamp as Long)
}