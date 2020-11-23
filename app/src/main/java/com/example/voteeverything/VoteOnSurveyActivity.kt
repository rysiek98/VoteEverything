package com.example.voteeverything

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_vote_on_survey.*

class VoteOnSurveyActivity : AppCompatActivity() {

    var controlHideUIFlag = false
    val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote_on_survey)
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
        var votes: ArrayList<Int> = ArrayList()
        var voters: ArrayList<String> = ArrayList()

        docRefSurvey.get()
            .addOnSuccessListener { DocumentSnapshot->
                if(DocumentSnapshot.exists()){
                    options = DocumentSnapshot.get("options") as ArrayList<String>
                    votes.addAll(DocumentSnapshot.get("votes") as ArrayList<Int>)
                    voters.addAll(DocumentSnapshot.get("voters") as ArrayList<String>)
                    setOptions(options)
                }else{
                    Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        surveyTitleVSurvey.text = title

        voteVSurveyBt.setOnClickListener {
            val radioButtonID: Int = rgContainer.checkedRadioButtonId
            votes[radioButtonID] = votes[radioButtonID] + 1
            voters.add(userUID)
            updateDB(voters,votes,db,userUID,title)
            val votesWindow = Intent(applicationContext,ViewVotesActivity::class.java)
            startActivity(votesWindow)
        }

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

    private fun setOptions(options: ArrayList<String>){

       for(i in 0..(options.size -1)){
           val newElement = RadioButton(this)
           val container = findViewById<View>(R.id.rgContainer) as RadioGroup
           newElement.id = i
           newElement.text = options[i]
           newElement.textSize = 20F
           container.addView(newElement)
       }

    }

    private fun updateDB( voters: ArrayList<String>, votes: ArrayList<Int>, db: FirebaseFirestore, userUID: String, title: String){
        val data   = hashMapOf(
            "votes" to votes,
            "voters" to voters
        )
        db.collection(userUID).document(title)
            .update(data as Map<String, Any>)
    }

}