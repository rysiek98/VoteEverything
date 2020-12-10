package com.example.voteeverything

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.size
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_view_surveys.*

class ViewSurveysActivity : AppCompatActivity() {

    private var controlHideUIFlag = false
    private val currentUserUID = Firebase.auth.currentUser?.uid.toString()
    private val db = Firebase.firestore
    private var flag = false

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
        flag = intent.getBooleanExtra("flag", false)
        if(!flag){
            textVSurveysText.text = "Available Surveys"
        }else{
            val name = Firebase.auth.currentUser?.displayName
            textVSurveysText.text = "$name Surveys"
        }

        backVSurveysBt.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val docRefSurveys = db.collection("dbInfo").document("surveys")
        paintSurveys(docRefSurveys, currentUserUID, flag)

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

    private fun paintSurveys(docRefSurveys: DocumentReference, currentUserUID: String, flag: Boolean) {
        val container = findViewById<View>(R.id.surveysContainer) as LinearLayout
        container.removeAllViews()
        var userToSurvey: ArrayList<String>
        docRefSurveys.get()
            .addOnSuccessListener { DocumentSnapshot->
                if(DocumentSnapshot.exists()){
                    userToSurvey = DocumentSnapshot.get("userToSurvey") as ArrayList<String>
                    if(!flag) {
                        paintSurveys(userToSurvey, container, currentUserUID)
                    }else{
                        paintSurveys(userToSurvey, container, currentUserUID)
                    }
                }else{
                    Toast.makeText(baseContext,"Data not found!",Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext,"Data not found!", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun paintSurveys(userToSurvey: ArrayList<String>, container:LinearLayout, currentUserUID: String){
        val surveyWindow = Intent(applicationContext,VoteOnSurveyActivity::class.java)
        val votesWindow = Intent(applicationContext,ViewVotesActivity::class.java)
        var controlFlag = false
        val text = "Ups... Nobody haven't created any surveys yet. Let's make first survey!"


        if(userToSurvey.size == 0){
            createText(container, text)
            return
        }

        (1 until userToSurvey.size step 2).forEach { i ->
            val userUID = userToSurvey[i - 1]
            val title = userToSurvey[i]
            if (userUID == currentUserUID && flag) {
                val newElement = createNewElement(container, title)
                activeButton(newElement, userUID, title, surveyWindow, votesWindow, flag)
                controlFlag = true
            }
            else if (!flag){
                val newElement = createNewElement(container, title)
                activeButton(newElement, userUID, title, surveyWindow, votesWindow, flag)
            }
        }

        if(!controlFlag && flag){
            val text = "Ups... You haven't created any surveys yet! Make your first survey!"
            createText(container, text)
            return
        }
    }

    private fun activeButton(newElement: MaterialButton, userUID: String, title: String, surveyWindow: Intent, votesWindow: Intent, flag: Boolean){
        newElement.setOnClickListener {
            db.collection(userUID).document(title).get()
                .addOnSuccessListener { DocumentSnapshot ->
                    if (DocumentSnapshot.exists()) {
                        val tmp = DocumentSnapshot.get("voters") as ArrayList<String>
                        if (!tmp.contains(currentUserUID)) {
                            surveyWindow.putExtra("title", title)
                            surveyWindow.putExtra("user", userUID)
                            surveyWindow.putExtra("currentUser", currentUserUID)
                            startActivity(surveyWindow)
                        } else {
                            votesWindow.putExtra("title", title)
                            votesWindow.putExtra("userUID", userUID)
                            votesWindow.putExtra("flag", flag)
                            startActivity(votesWindow)
                        }
                    } else {
                        Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun createNewElement(container: LinearLayout, title: String ): MaterialButton{
        val newElement = MaterialButton(this)
        newElement.text = title
        newElement.id = container.size + 1
        newElement.setBackgroundColor(Color.BLACK)
        newElement.setTextColor(Color.WHITE)
        newElement.cornerRadius = 20
        container.addView(newElement)
        return newElement
    }

    private fun createText(container: LinearLayout, text: String){
        val newElement = TextView(this)
        newElement.text = text
        newElement.textSize = 30f
        newElement.setTextColor(Color.WHITE)
        container.addView(newElement)
    }

}