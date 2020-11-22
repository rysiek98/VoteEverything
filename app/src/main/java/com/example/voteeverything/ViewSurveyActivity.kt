package com.example.voteeverything

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_view_survey.*

class ViewSurveyActivity : AppCompatActivity() {

    var controlHideUIFlag = false
    val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_survey)
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

        val title = getIntent().getStringExtra("title")
        val userUID = getIntent().getStringExtra("user")
        val docRefSurvey = db.collection(userUID).document(title)
        var options: ArrayList<String> = ArrayList()

        docRefSurvey.get()
            .addOnSuccessListener { DocumentSnapshot->
                if(DocumentSnapshot.exists()){
                    options = DocumentSnapshot.get("options") as ArrayList<String>
                    setOptionsText(options)
                }else{
                    Toast.makeText(baseContext,"Data not found!", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        surveyTitleVSurvey.text = title

        backVSurveyBt.setOnClickListener {
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

    private fun setOptionsText(options: ArrayList<String>){

       for(i in 0..(options.size -1)){
           val newElement = RadioButton(this)
           val container = findViewById<View>(R.id.rgContainer) as RadioGroup
           newElement.id = i
           newElement.text = options[i]
           container.addView(newElement)
       }

    }

}