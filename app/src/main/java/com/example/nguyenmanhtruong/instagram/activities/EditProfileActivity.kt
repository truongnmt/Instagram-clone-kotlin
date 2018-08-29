package com.example.nguyenmanhtruong.instagram.activities

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import android.widget.TextView
import com.example.nguyenmanhtruong.instagram.R
import com.example.nguyenmanhtruong.instagram.models.User
import com.example.nguyenmanhtruong.instagram.views.PasswordDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class EditProfileActivity : AppCompatActivity(), PasswordDialog.Listener {
    private val TAG = "EditProfileActivity"
    private lateinit var mUser: User
    private lateinit var mPendingUser: User
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mStorage: StorageReference
    private val TAKE_PICTURE_REQUEST_CODE = 1
    private lateinit var mImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        Log.d(TAG, "onCreate")

        close_image.setOnClickListener { finish() }
        save_image.setOnClickListener { updateProfile() }
        change_photo_text.setOnClickListener { takeCameraPicture() }

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        mStorage = FirebaseStorage.getInstance().reference

        mDatabase.child("users").child(mAuth.currentUser!!.uid).addListenerForSingleValueEvent(ValueEventListenerAdapter {
            mUser = it.getValue(User::class.java)!!
            name_input.setText(mUser.name, TextView.BufferType.EDITABLE)
            username_input.setText(mUser.username, TextView.BufferType.EDITABLE)
            website_input.setText(mUser.website, TextView.BufferType.EDITABLE)
            bio_input.setText(mUser.bio, TextView.BufferType.EDITABLE)
            phone_input.setText(mUser.phone.toString(), TextView.BufferType.EDITABLE)
            email_input.setText(mUser.email, TextView.BufferType.EDITABLE)
        })
    }


    private fun takeCameraPicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val imageFile = createImageFile()
            mImageUri = FileProvider.getUriForFile(this,
                    "com.example.nguyenmanhtruong.instagram.fileprovider",
                    imageFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
            startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE)
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + simpleDateFormat + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = image.getAbsolutePath()
//        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            val uid = mAuth.currentUser!!.uid
            // upload image to firebase storage
            mStorage.child("users/$uid/photo").putFile(mImageUri).addOnCompleteListener {
                if (it.isSuccessful) {
                    mDatabase.child("users/$uid/photo").setValue(it.result.downloadUrl.toString())
                            .addOnCompleteListener {
                        if(it.isSuccessful) {
                            Log.d(TAG, "onActivityResult: photo saved successfully")
                        } else {
                            showToast(it.exception!!.message!!)
                        }
                    }
                } else {
                    showToast(it.exception!!.message!!)
                }
            }
            // save image to database user.photo
        }
    }

    private fun updateProfile() {
        mPendingUser = User(
                name = name_input.text.toString(),
                username = username_input.text.toString(),
                website = website_input.text.toString(),
                bio = bio_input.text.toString(),
                email = email_input.text.toString(),
                phone = phone_input.text.toString().toLong()
        )
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

    override fun onPasswordConfirm(password: String) {
        Log.d(TAG, "onPasswordConfirm: password: $password")
        val credential = EmailAuthProvider.getCredential(mUser.email, password)
        mAuth.currentUser!!.reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                mAuth.currentUser!!.updateEmail(mPendingUser.email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        updateUser(mPendingUser)
                    } else {
                        showToast(it.exception!!.message!!)
                    }
                }
            } else {
                showToast(it.exception!!.message!!)
            }
        }
    }

    private fun updateUser(user: User) {
        val updatesMap = mutableMapOf<String, Any>()
        if (user.name != mUser.name) updatesMap["name"] = user.name
        if (user.username != mUser.username) updatesMap["username"] = user.username
        if (user.website != mUser.website) updatesMap["website"] = user.website
        if (user.bio != mUser.bio) updatesMap["bio"] = user.bio
        if (user.email != mUser.email) updatesMap["email"] = user.email
        if (user.phone != mUser.phone) updatesMap["phone"] = user.phone

        mDatabase.child("users").child(mAuth.currentUser!!.uid).updateChildren(updatesMap)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        showToast("Profile saved")
                        finish()
                    } else {
                        showToast(it.exception!!.message!!)
                    }
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
