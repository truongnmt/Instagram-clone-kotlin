package com.example.nguyenmanhtruong.instagram.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraPictureTaker(private val activity: Activity) {
    var imageUri: Uri? = null
    val REQUEST_CODE = 1

    fun takeCameraPicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity.packageManager) != null) {
            val imageFile = createImageFile()
            imageUri = FileProvider.getUriForFile(activity,
                    "com.example.nguyenmanhtruong.instagram.fileprovider",
                    imageFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val simpleDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + simpleDateFormat + "_"
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = image.getAbsolutePath()
//        return image
    }
}