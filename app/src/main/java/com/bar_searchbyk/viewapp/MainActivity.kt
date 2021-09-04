package com.bar_searchbyk.viewapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val parentLayout = findViewById<ConstraintLayout>(R.id.parentLayout)
        val myCustomView = findViewById<CustomView>(R.id.myCustomView)

        parentLayout.viewTreeObserver.addOnGlobalLayoutListener(object :ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                // 親Viewのheightとwidthが確定次第、カスタムViewの位置をセットする
                myCustomView.initView(parentLayout.width.toFloat(),parentLayout.height.toFloat())
                parentLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }
}