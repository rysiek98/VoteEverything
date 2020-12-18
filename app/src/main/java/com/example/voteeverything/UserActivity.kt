package com.example.voteeverything

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_user.*

class UserActivity : AppCompatActivity() {

    private var controlHideUIFlag = false
    private val currentUser = Firebase.auth.currentUser
    private val userName = currentUser?.displayName
    private val userEmail = currentUser?.email
    private val userUid = currentUser?.uid.toString()
    private val db = Firebase.firestore
    private val docRefSurveys = db.collection("dbInfo").document("surveys")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
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

        mySurveyButtonUpdate(docRefSurveys, userUid)

        helloUText.text = "Hello, $userName!"
        nameUText.text = userName
        emailUText.text = userEmail

        backUBt.setOnClickListener {
            finish()
        }

        mySurveysUBt.setOnClickListener {
            val availableSurveyWindow = Intent(applicationContext,ViewSurveysActivity::class.java)
            availableSurveyWindow.putExtra("flag", true)
            availableSurveyWindow.putExtra("userIsAnonymous", false)
            startActivity(availableSurveyWindow)
        }
    }

    override fun onResume() {
        super.onResume()
        mySurveyButtonUpdate(docRefSurveys, userUid)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

    private  fun mySurveyButtonUpdate(docRefSurveys: DocumentReference, userUid: String){
        var counter = 0
        var userToSurvey: ArrayList<String>

        docRefSurveys.get()
            .addOnSuccessListener { DocumentSnapshot->
                if(DocumentSnapshot.exists()){
                    userToSurvey = DocumentSnapshot.get("userToSurvey") as ArrayList<String>
                    userToSurvey.forEach { field -> if (field == userUid) counter++ }
                    mySurveysUBt.text = "My surveys: $counter"
                }
            }
    }
}