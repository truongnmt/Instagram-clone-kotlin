package com.example.nguyenmanhtruong.instagram

import android.os.Bundle
import android.util.Log

class HomeActivity : BaseActivity(0) {
    private val TAG = "HomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        Log.d(TAG, "onCreate: ")
        setupBottomNavigation()
    }
}
