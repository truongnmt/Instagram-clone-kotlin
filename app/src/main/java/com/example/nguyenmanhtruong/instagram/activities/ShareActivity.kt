package com.example.nguyenmanhtruong.instagram.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.nguyenmanhtruong.instagram.R
import com.example.nguyenmanhtruong.instagram.models.User
import com.example.nguyenmanhtruong.instagram.utils.CameraHelper
import com.example.nguyenmanhtruong.instagram.utils.FirebaseHelper
import com.example.nguyenmanhtruong.instagram.utils.GlideApp
import com.example.nguyenmanhtruong.instagram.utils.ValueEventListenerAdapter
import com.google.firebase.database.ServerValue
import kotlinx.android.synthetic.main.activity_share.*
import java.util.*

class ShareActivity : BaseActivity(2) {
    private val TAG = "ShareActivity"
    private lateinit var mCamera: CameraHelper
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
//        setupBottomNavigation()
        Log.d(TAG, "onCreate")

        mFirebase = FirebaseHelper(this)
        mCamera = CameraHelper(this)
        mCamera.takeCameraPicture()

        back_image.setOnClickListener { finish() }
        share_text.setOnClickListener { share() }

        mFirebase.currentUserReference().addValueEventListener(ValueEventListenerAdapter {
            mUser = it.asUser()!!
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == mCamera.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                GlideApp.with(this).load(mCamera.imageUri).centerCrop().into(post_image)
            } else {
                finish()
            }
        }
    }

    private fun share() {
        val imageUri = mCamera.imageUri
        if (imageUri != null) {
            val uid = mFirebase.auth.currentUser!!.uid
            mFirebase.storage.child("users").child(mFirebase.auth.currentUser!!.uid).child("images")
                    .child(imageUri.lastPathSegment).putFile(imageUri).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val imageDownloadUrl = it.result.downloadUrl!!.toString()
                            mFirebase.database.child("images").child(uid).push()
                                    .setValue(it.result.downloadUrl!!.toString())
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            mFirebase.database.child("feed-posts").child(uid)
                                                    .push().setValue(mkFeedPost(uid, imageDownloadUrl))
                                                    .addOnCompleteListener {
                                                        if (it.isSuccessful) {
                                                            startActivity(Intent(this,
                                                                    ProfileActivity::class.java))
                                                            finish()
                                                        }
                                                    }
                                        } else {
                                            showToast(it.exception!!.message!!)
                                        }
                                    }
                        } else {
                            showToast(it.exception!!.message!!)
                        }
                    }
            // add image to user images <- db
        }
    }

    private fun mkFeedPost(uid: String, imageDownloadUrl: String): FeedPost {
        return FeedPost(
                uid = uid,
                username = mUser.username,
                image = imageDownloadUrl,
                caption = caption_input.text.toString(),
                photo = mUser.photo
        )
    }
}

data class FeedPost(val uid: String = "", val username: String = "",
                    val image: String = "", val likesCount: Int = 0, val commentsCount: Int = 0,
                    val caption: String = "", val comments: List<Comment> = emptyList(),
                    val timestamp: Any = ServerValue.TIMESTAMP, val photo: String? = null) {
    // save -> Firebase puts Long value
    // get <- Long
    fun timestampDate(): Date = Date(timestamp as Long)
}

data class Comment(val uid: String, val username: String, val text: String)