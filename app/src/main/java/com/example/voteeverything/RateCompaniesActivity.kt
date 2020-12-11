package com.example.voteeverything

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.RatingBar
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_rate_companies.*

class RateCompaniesActivity : AppCompatActivity() {

    private var controlHideUIFlag = false
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate_companies)
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

        val ratingBar = findViewById<View>(R.id.ratingBar) as RatingBar
        val title = intent.getStringExtra("title")
        val userUID = intent.getStringExtra("user")
        val currentUserUID = intent.getStringExtra("currentUser")
        val docRefCompany = db.collection(userUID).document(title)
        var votes: ArrayList<Float> = ArrayList()
        var voters: ArrayList<String> = ArrayList()

        titleRCompanies.text = title

        docRefCompany.get()
            .addOnSuccessListener { DocumentSnapshot->
                if(DocumentSnapshot.exists()){
                    descriptionRCompanies.text = DocumentSnapshot.get("description") as String
                    votes.addAll(DocumentSnapshot.get("votes") as ArrayList<Float>)
                    voters.addAll(DocumentSnapshot.get("voters") as ArrayList<String>)
                }else{
                    Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                    .show()
            }

        Handler().postDelayed({ paintRating(votes, ratingBar) }, 200)

        backRCompanyBt.setOnClickListener {
            finish()
        }

        voteRCompaniesBt.setOnClickListener {
            if(voters.contains(currentUserUID)){
                Toast.makeText(baseContext, "You've already judged it.", Toast.LENGTH_SHORT)
                    .show()
            }else if ( ratingBar.rating == 0f){
                Toast.makeText(baseContext, "Ups... You should use rating bar.", Toast.LENGTH_SHORT)
                    .show()
            }
            else {
                votes[0] = votes[0] + ratingBar.rating
                votes[1] = votes[1] + 1
                voters.add(currentUserUID)
                updateDB(voters, votes, db, userUID, title)
            }
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

    private fun paintRating(votes: ArrayList<Float>, ratingBar: RatingBar){
        var rating: Float = votes[0]/votes[1]
        ratingBar.rating = rating
    }

    private fun updateDB(voters: ArrayList<String>, votes: ArrayList<Float>, db: FirebaseFirestore, userUID: String, title: String){
        val data   = hashMapOf(
            "votes" to votes,
            "voters" to voters
        )
        db.collection(userUID).document(title)
            .update(data as Map<String, Any>)
    }
}