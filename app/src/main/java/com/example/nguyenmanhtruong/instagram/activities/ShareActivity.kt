package com.example.nguyenmanhtruong.instagram.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.nguyenmanhtruong.instagram.R
import com.example.nguyenmanhtruong.instagram.models.FeedPost
import com.example.nguyenmanhtruong.instagram.models.User
import com.example.nguyenmanhtruong.instagram.utils.CameraHelper
import com.example.nguyenmanhtruong.instagram.utils.FirebaseHelper
import com.example.nguyenmanhtruong.instagram.utils.GlideApp
import com.example.nguyenmanhtruong.instagram.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.activity_share.*

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
            val uid = mFirebase.currentUid()
            mFirebase.storage.child("users").child(mFirebase.currentUid()!!).child("images")
                    .child(imageUri.lastPathSegment).putFile(imageUri).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val imageDownloadUrl = it.result.downloadUrl!!.toString()
                            mFirebase.database.child("images").child(uid).push()
                                    .setValue(it.result.downloadUrl!!.toString())
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            mFirebase.database.child("feed-posts").child(uid)
                                                    .push().setValue(mkFeedPost(uid!!, imageDownloadUrl))
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
