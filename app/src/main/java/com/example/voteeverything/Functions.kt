package com.example.voteeverything

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.size
import com.google.android.material.button.MaterialButton

fun hideSystemUI(window: Window) {
    // Enables regular immersive mode.
    // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
    // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
            // Set the content to appear under the system bars so that the
            // content doesn't resize when the system bars hide and show.
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // Hide the nav bar and status bar
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN)
}

fun createNewElement(container: LinearLayout, title: String, context: Context): MaterialButton {

    val newElement = MaterialButton(context)
    newElement.text = title
    newElement.id = container.size + 1
    newElement.setBackgroundColor(Color.BLACK)
    newElement.setTextColor(Color.WHITE)
    newElement.cornerRadius = 20
    container.addView(newElement)
    return newElement
}

fun createText(container: LinearLayout, text: String, context: Context) {
    val newElement = TextView(context)
    newElement.text = text
    newElement.textSize = 25f
    newElement.setTextColor(Color.WHITE)
    container.addView(newElement)
}