package com.example.nguyenmanhtruong.instagram.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.nguyenmanhtruong.instagram.R
import com.example.nguyenmanhtruong.instagram.models.User
import com.example.nguyenmanhtruong.instagram.utils.CameraHelper
import com.example.nguyenmanhtruong.instagram.utils.FirebaseHelper
import com.example.nguyenmanhtruong.instagram.utils.ValueEventListenerAdapter
import com.example.nguyenmanhtruong.instagram.views.PasswordDialog
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.android.synthetic.main.activity_edit_profile.*


class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {
    private val TAG = "EditProfileActivity"
    private lateinit var mUser: User
    private lateinit var mPendingUser: User
    private lateinit var mFirebase: FirebaseHelper
    private lateinit var mCamera: CameraHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "onCreate")

        mCamera = CameraHelper(this)
        close_image.setOnClickListener { finish() }
        save_image.setOnClickListener { updateProfile() }
        change_photo_text.setOnClickListener { mCamera.takeCameraPicture() }

        mFirebase = FirebaseHelper(this)

        mFirebase.currentUserReference()
                .addListenerForSingleValueEvent(ValueEventListenerAdapter {
                    mUser = it.getValue(User::class.java)!!
                    name_input.setText(mUser.name)
                    username_input.setText(mUser.username)
                    website_input.setText(mUser.website)
                    bio_input.setText(mUser.bio)
                    phone_input.setText(mUser.phone?.toString())
                    email_input.setText(mUser.email)
                    profile_image.loadUserPhoto(mUser.photo)
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == mCamera.REQUEST_CODE && resultCode == RESULT_OK) {
            mFirebase.uploadUserPhoto(mCamera.imageUri!!) {
                val photoUrl = it.downloadUrl.toString()
                mFirebase.updateUserPhoto(photoUrl) {
                    mUser = mUser.copy(photo = photoUrl)
                    profile_image.loadUserPhoto(mUser.photo)
                }
            }
        }
    }


    private fun updateProfile() {
        mPendingUser = readInputs()
        val error = validate(mPendingUser)
        if (error == null) {
            if (mPendingUser.email == mUser.email) {
                updateUser(mPendingUser)
            } else {
                // password
                PasswordDialog().show(supportFragmentManager, "password_dialog")
                // update email in auth
                // update user
            }
        } else {
            showToast(error)
        }
    }

    private fun readInputs(): User {
        return User(
                name = name_input.text.toString(),
                username = username_input.text.toString(),
                email = email_input.text.toString(),
                website = website_input.text.toStringOrNull(),
                bio = bio_input.text.toStringOrNull(),
                phone = phone_input.text.toString().toLongOrNull()
        )
    }

    override fun onPasswordConfirm(password: String) {
        if (password.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(mUser.email, password)
            mFirebase.reauthenticate(credential) {
                mFirebase.updateEmail(mPendingUser.email) {
                    updateUser(mPendingUser)
                }
            }
        } else {
            showToast("You should enter your password")
        }
    }

    private fun updateUser(user: User) {
        val updatesMap = mutableMapOf<String, Any?>()
        if (user.name != mUser.name) updatesMap["name"] = user.name
        if (user.username != mUser.username) updatesMap["username"] = user.username
        if (user.website != mUser.website) updatesMap["website"] = user.website
        if (user.bio != mUser.bio) updatesMap["bio"] = user.bio
        if (user.email != mUser.email) updatesMap["email"] = user.email
        if (user.phone != mUser.phone) updatesMap["phone"] = user.phone

        mFirebase.updateUser(updatesMap) {
            showToast("Profile saved")
            finish()
        }
    }

    private fun validate(user: User): String? =
            when {
                user.name.isEmpty() -> "Please enter name"
                user.username.isEmpty() -> "Please enter username"
                user.email.isEmpty() -> "Please enter email"
                else -> null
            }
}
