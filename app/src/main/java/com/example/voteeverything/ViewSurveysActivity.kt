package com.example.voteeverything

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
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
    private var userIsAnonymous = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_surveys)
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
        flag = intent.getBooleanExtra("flag", false)
        userIsAnonymous = intent.getBooleanExtra("userIsAnonymous", true)
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
        paintSurveys(docRefSurveys, currentUserUID)

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

    private fun paintSurveys(docRefSurveys: DocumentReference, currentUserUID: String) {
        val container = findViewById<View>(R.id.surveysContainer) as LinearLayout
        container.removeAllViews()
        var userToSurvey: ArrayList<String>
        docRefSurveys.get()
            .addOnSuccessListener { DocumentSnapshot->
                if(DocumentSnapshot.exists()){
                    userToSurvey = DocumentSnapshot.get("userToSurvey") as ArrayList<String>
                        paintSurveys(userToSurvey, container, currentUserUID)
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
        val voteOnSurveyWindow = Intent(applicationContext,VoteOnSurveyActivity::class.java)
        val viewVotesWindow = Intent(applicationContext,ViewVotesActivity::class.java)
        var controlFlag = false
        val text = "Ups... Nobody haven't created any surveys yet. Let's make first survey!"


        if(userToSurvey.size == 0){
            createText(container, text, this)
            return
        }

        (1 until userToSurvey.size step 2).forEach { i ->
            val userUID = userToSurvey[i - 1]
            val title = userToSurvey[i]
            if (userUID == currentUserUID && flag) {
                val newElement = createNewElement(container, title, this)
                activeButton(newElement, userUID, title, voteOnSurveyWindow, viewVotesWindow, flag)
                controlFlag = true
            }
            else if (!flag){
                val newElement = createNewElement(container, title, this)
                activeButton(newElement, userUID, title, voteOnSurveyWindow, viewVotesWindow, flag)
            }
        }

        if(!controlFlag && flag){
            val text = "Ups... You haven't created any survey yet! Make your first survey!"
            createText(container, text, this)
            return
        }
    }

    private fun activeButton(newElement: MaterialButton, userUID: String, title: String, voteOnSurveyWindow: Intent, viewVotesWindow: Intent, flag: Boolean){
        newElement.setOnClickListener {
            db.collection(userUID).document("S_"+title).get()
                .addOnSuccessListener { DocumentSnapshot ->
                    if (DocumentSnapshot.exists()) {
                        val tmp = DocumentSnapshot.get("voters") as ArrayList<String>
                        if (!tmp.contains(currentUserUID) && !userIsAnonymous) {
                            voteOnSurveyWindow.putExtra("title", title)
                            voteOnSurveyWindow.putExtra("user", userUID)
                            voteOnSurveyWindow.putExtra("currentUser", currentUserUID)
                            startActivity(voteOnSurveyWindow)
                        } else {
                            viewVotesWindow.putExtra("title", title)
                            viewVotesWindow.putExtra("userUID", userUID)
                            viewVotesWindow.putExtra("flag", flag)
                            startActivity(viewVotesWindow)
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

}