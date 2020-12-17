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

    private var controlHideUIFlag = false
    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote_on_survey)
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

        val title = intent.getStringExtra("title")
        val userUID = intent.getStringExtra("user")
        val currentUserUID = intent.getStringExtra("currentUser")
        val docRefSurvey = db.collection(userUID).document(title)
        var options: ArrayList<String>
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
            .addOnFailureListener {
                Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                    .show()
            }

        surveyTitleVSurvey.text = title

        voteVSurveyBt.setOnClickListener {
            val radioButtonID: Int = rgContainer.checkedRadioButtonId
            votes[radioButtonID] = votes[radioButtonID] + 1
            voters.add(currentUserUID)
            updateDB(voters,votes,db,userUID,title)
            val votesWindow = Intent(applicationContext,ViewVotesActivity::class.java)
            votesWindow.putExtra("title",title)
            votesWindow.putExtra("userUID", userUID)
            startActivity(votesWindow)
            finish()
        }

        backVSurveyBt.setOnClickListener {
            finish()
        }
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

    private fun setOptions(options: ArrayList<String>){

        (0 until options.size).forEach { i ->
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