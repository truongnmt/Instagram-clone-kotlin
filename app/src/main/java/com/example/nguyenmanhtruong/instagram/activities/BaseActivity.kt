package com.example.nguyenmanhtruong.instagram.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.nguyenmanhtruong.instagram.R
import kotlinx.android.synthetic.main.bottom_navigation_view.*

abstract class BaseActivity(val navNumber: Int) : AppCompatActivity() {
    private val TAG = "BaseActivity"

    fun setupBottomNavigation() {
        // TODO Update bottom navigation view icon size
//        val bottomNavigationView = bottom_navigation_view
//        val menuView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
//        for (i in 0..menuView.childCount) {
//            val iconView = menuView.getChildAt(i).findViewById<View>(android.support.design.R.id.icon)
//            val layoutParams = iconView.layoutParams
//            val displayMetrics = resources.displayMetrics
//            layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32F, displayMetrics).toInt()
//            layoutParams.width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32F, displayMetrics).toInt()
//            iconView.layoutParams = layoutParams
//        }

        bottom_navigation_view.setOnNavigationItemSelectedListener {
            val nextActivity =
                    when (it.itemId) {
                        R.id.nav_item_home -> HomeActivity::class.java
                        R.id.nav_item_search -> SearchActivity::class.java
                        R.id.nav_item_share -> ShareActivity::class.java
                        R.id.nav_item_likes -> LikesActivity::class.java
                        R.id.nav_item_profile -> ProfileActivity::class.java
                        else -> {
                            Log.e(TAG, "unknown nav item clicked $it")
                            null
                        }
                    }
            if (nextActivity != null) {
                val intent = Intent(this, nextActivity)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
                overridePendingTransition(0, 0)
                true
            } else {
                false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (bottom_navigation_view != null) {
            bottom_navigation_view.menu.getItem(navNumber).isChecked = true
        }
    }
}