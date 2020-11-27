package com.example.voteeverything

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.size
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create_survey.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CreateSurveyActivity : AppCompatActivity() {

    private var controlHideUIFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_survey)
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
        val docRefSurveys = db.collection("dbInfo").document("surveys")


        backCSurveyBt.setOnClickListener {
            finish()
        }

        addOptionCSurveyBt.setOnClickListener {
            val newElement = EditText(this)
            newElement.hint = option1.hint.toString()
            newElement.typeface = option1.typeface
            newElement.setTextColor(option1.textColors)
            newElement.textSize = 20F
            val container = findViewById<View>(R.id.optionsContainer) as LinearLayout
            newElement.id = container.size+1
            container.addView(newElement)
        }

        deleteOptionCSurveyBt.setOnClickListener {
            val container = findViewById<View>(R.id.optionsContainer) as LinearLayout
            if(container.size > 2){
                container.removeViewAt(container.size - 1)
            }
        }

        createCSurveyBt.setOnClickListener {

            val title = surveyTitle.text.toString()
            val container = findViewById<View>(R.id.optionsContainer) as LinearLayout
            val option1 = container.findViewById<EditText>(R.id.option1).text.toString()
            val option2 = container.findViewById<EditText>(R.id.option2).text.toString()
            var options: ArrayList<String> = ArrayList()
            var votes: ArrayList<Int> = ArrayList()
            var voters: ArrayList<String> = ArrayList()
            var listOfTitles: ArrayList<String> = ArrayList()
            var surveys:  ArrayList<String> = ArrayList()

            //Adding survey
            if (title.isNotEmpty() && option1.isNotEmpty() && option2.isNotEmpty()) {
                options.add(option1)
                votes.add(0)
                options.add(option2)
                votes.add(0)
                var option: EditText
                for(i in 2..(container.size - 1)){
                    option = container[i] as EditText
                    if(option.text.isNotEmpty()) {
                        options.add(option.text.toString())
                        votes.add(0)
                    }
                }
                val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
                val currentDate = sdf.format(Date())
                val survey = hashMapOf(
                    "userID" to user?.uid,
                    "userName" to user?.displayName,
                    "creationData" to currentDate,
                    "title" to title,
                    "options" to options,
                    "votes" to votes,
                    "voters" to voters
                )

                db.collection(user?.uid.toString()).document(title)
                    .set(survey)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(baseContext,"Survey successfully add to database.",Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(baseContext,"Failure!",Toast.LENGTH_SHORT)
                            .show()
                    }

                //Adding uid-survey to uid-surveyList
                val user = user?.uid.toString()
                docRefSurveys.get()
                    .addOnSuccessListener { DocumentSnapshot->
                        if(DocumentSnapshot.exists()){
                            if(DocumentSnapshot.contains("userToSurvey")) {
                                surveys = DocumentSnapshot.get("userToSurvey") as ArrayList<String>
                            }
                            surveys.add(user)
                            surveys.add(title)
                            updateSurveys(surveys, db)
                        }else{
                            surveys.add(user)
                            surveys.add(title)
                            updateSurveys(surveys,db)
                        }
                    }
                    .addOnFailureListener {
                        surveys.add(user)
                        surveys.add(title)
                        updateSurveys(surveys, db)
                    }

            }else{
                Toast.makeText(baseContext,"Please enter all necessary data.",Toast.LENGTH_SHORT)
                    .show()
            }

        }

    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun updateSurveys( surveys: ArrayList<String>, db: FirebaseFirestore){
        val data   = hashMapOf(
            "userToSurvey" to surveys
        )
        db.collection("dbInfo").document("surveys")
            .update(data as Map<String, Any>)
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
}