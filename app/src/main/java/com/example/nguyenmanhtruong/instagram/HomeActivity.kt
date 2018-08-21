package com.example.nguyenmanhtruong.instagram

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.internal.BottomNavigationMenuView
import android.util.TypedValue
import android.view.View
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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
    }
}
