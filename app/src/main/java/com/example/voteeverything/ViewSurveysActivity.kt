package com.example.voteeverything

import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.size
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create_survey.*
import kotlinx.android.synthetic.main.activity_view_surveys.*

class ViewSurveysActivity : AppCompatActivity() {

    var controlHideUIFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_surveys)
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                if (!controlHideUIFlag) {
                    Handler().postDelayed({ hideSystemUI() }, 2000)
                } else {
                    hideSystemUI()
                    controlHideUIFlag = false
                }
            }
        }

        val user = Firebase.auth.currentUser
        val db = Firebase.firestore
        val docRefTitles = db.collection("dbInfo").document("listOfTitles")
        val docRefSurveys = db.collection("dbInfo").document("surveys")
        val container = findViewById<View>(R.id.surveysContainer) as LinearLayout
        var listOfTitles: ArrayList<String> = ArrayList()

        docRefTitles.get()
            .addOnSuccessListener { DocumentSnapshot->
                if(DocumentSnapshot.exists()){
                    listOfTitles = DocumentSnapshot.get("titleList") as ArrayList<String>
                    paintSurveys(listOfTitles,container)
                }else{
                    Toast.makeText(baseContext,"Data not found!",Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext,"Data not found!", Toast.LENGTH_SHORT)
                    .show()
            }

        backVSurveysBt.setOnClickListener {
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
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

    private fun paintSurveys(listOfTitles: ArrayList<String>, container:LinearLayout){
        for(i in 0..(listOfTitles.size-1)){
            val newElement = MaterialButton(this)
            newElement.text = listOfTitles.get(i)
            newElement.id = container.size+1
            newElement.setBackgroundColor(Color.BLACK)
            newElement.setTextColor(Color.WHITE)
            newElement.cornerRadius = 20
            container.addView(newElement)

            newElement.setOnClickListener {
                Toast.makeText(baseContext,"TEST",Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}