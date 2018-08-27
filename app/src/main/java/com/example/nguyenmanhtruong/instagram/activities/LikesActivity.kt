package com.example.nguyenmanhtruong.instagram.activities

import android.os.Bundle
import android.util.Log
import com.example.nguyenmanhtruong.instagram.R

class LikesActivity : BaseActivity(3) {
    private val TAG = "LikesActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setupBottomNavigation()
        Log.d(TAG, "onCreate")
    }
}
