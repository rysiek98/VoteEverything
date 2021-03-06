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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
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
                    Handler().postDelayed({ hideSystemUI(window) }, 2000)
                } else {
                    hideSystemUI(window)
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
            val options: ArrayList<String> = ArrayList()
            val votes: ArrayList<Int> = ArrayList()
            val voters: ArrayList<String> = ArrayList()

            db.collection(user?.uid.toString()).document("S_" + title).get()
                .addOnSuccessListener { DocumentSnapshot ->
                    if (DocumentSnapshot.exists()) {
                        Toast.makeText(
                            baseContext,
                            "You already have survey with that title. You must first delete it to add new with the same title.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {

                        //Adding survey
                        if (title.isNotEmpty() && option1.isNotEmpty() && option2.isNotEmpty()) {
                            addSurvey(title, option1, option2, options, votes, container, user, voters, db)
                            addSurveyToUser(user, docRefSurveys, title, db)
                        } else {
                            Toast.makeText(
                                baseContext,
                                "Please enter all necessary data.",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                    }
                }
        }
    }

    private fun resetUI(container: LinearLayout) {
        while (container.size > 2)
            container.removeViewAt(container.size - 1)
        surveyTitle.text.clear()
        option1.text.clear()
        option2.text.clear()
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

    private fun updateSurveys( surveys: ArrayList<String>, db: FirebaseFirestore){
        val data   = hashMapOf(
            "userToSurvey" to surveys
        )
        db.collection("dbInfo").document("surveys")
            .update(data as Map<String, Any>)
    }

    private fun setSurveys( surveys: ArrayList<String>, db: FirebaseFirestore){
        val data   = hashMapOf(
            "userToSurvey" to surveys
        )
        db.collection("dbInfo").document("surveys")
            .set(data)
    }

    private fun addSurvey(
        title: String, option1: String, option2: String,
        options: ArrayList<String>, votes: ArrayList<Int>,
        container: LinearLayout, user: FirebaseUser?,
        voters: ArrayList<String>, db: FirebaseFirestore
    ) {
            options.add(option1)
            votes.add(0)
            options.add(option2)
            votes.add(0)
            var option: EditText
            for (i in 2 until container.size) {
                option = container[i] as EditText
                if (option.text.isNotEmpty()) {
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

            db.collection(user?.uid.toString()).document("S_" + title)
                .set(survey)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(
                        baseContext,
                        "Survey successfully add to database.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Handler().postDelayed({ resetUI(container) }, 500)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(baseContext, "Failure!", Toast.LENGTH_LONG)
                        .show()
                }
    }

    private fun addSurveyToUser(
        user: FirebaseUser?, docRefSurveys: DocumentReference,
        title: String, db: FirebaseFirestore
    ){
        //Adding uid-survey to uid-surveyList
        val user = user?.uid.toString()
        var surveys: ArrayList<String> = ArrayList()
        docRefSurveys.get()
            .addOnSuccessListener { DocumentSnapshot ->
                if (DocumentSnapshot.exists()) {
                    if (DocumentSnapshot.contains("userToSurvey")) {
                        surveys =
                            DocumentSnapshot.get("userToSurvey") as ArrayList<String>
                    }
                    surveys.add(user)
                    surveys.add(title)
                    updateSurveys(surveys, db)
                } else {
                    surveys.add(user)
                    surveys.add(title)
                    setSurveys(surveys, db)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    baseContext,
                    "Failed to connect to the database",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
    }

}