package com.example.voteeverything

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.size
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_rate_companies.*
import kotlinx.android.synthetic.main.add_comment.*
import kotlinx.android.synthetic.main.add_comment.view.*

class RateCompaniesActivity : AppCompatActivity() {

    private var controlHideUIFlag = false
    private val db = Firebase.firestore
    private var userIsAnonymous = true
    private var description: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate_companies)
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

        userIsAnonymous = intent.getBooleanExtra("userIsAnonymous", true)
        val ratingBar = findViewById<View>(R.id.ratingBar) as RatingBar
        val title = intent.getStringExtra("title")
        val userUID = intent.getStringExtra("user")
        val currentUserUID = intent.getStringExtra("currentUser")
        val docRefCompany = db.collection(userUID).document("C_"+title)
        val votes: ArrayList<Float> = ArrayList()
        val voters: ArrayList<String> = ArrayList()
        val comments: ArrayList<String> = ArrayList()
        val container = findViewById<View>(R.id.containerRCompanies) as LinearLayout

        titleRCompanies.text = title

        docRefCompany.get()
            .addOnSuccessListener { DocumentSnapshot->
                if(DocumentSnapshot.exists()){
                    descriptionRCompanies.text = DocumentSnapshot.get("description") as String
                    votes.addAll(DocumentSnapshot.get("votes") as ArrayList<Float>)
                    voters.addAll(DocumentSnapshot.get("voters") as ArrayList<String>)
                    comments.addAll(DocumentSnapshot.get("comments") as ArrayList<String>)
                }else{
                    Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                    .show()
            }

        Handler().postDelayed({ paint(comments, container, votes, ratingBar) }, 200)

        backRCompanyBt.setOnClickListener {
            finish()
        }

        addComment.setOnClickListener {
            if (!userIsAnonymous) {
                val dialogView = LayoutInflater.from(this).inflate(R.layout.add_comment, null)
                val mBuilder = AlertDialog.Builder(this).setView(dialogView)
                val mAlert = mBuilder.show()
                mAlert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                dialogView.backAddCommentBt.setOnClickListener {
                    mAlert.dismiss()
                }

                dialogView.addCommentBt.setOnClickListener {
                    description = descriptionRCompanies.text.toString()
                    if (dialogView.commentAddComment.text.isNotEmpty()) {
                        var newComment = Firebase.auth.currentUser?.displayName + ": "
                        newComment += dialogView.commentAddComment.text.toString()
                        comments.add(newComment)

                        if (!updateComments(comments, db, userUID, title)) {
                            if(comments.size == 1){
                                container.removeAllViews()
                                descriptionRCompanies.text = description
                                paint(comments, container, votes, ratingBar)
                            }else {
                                paintComments(comments, container)
                            }
                            Toast.makeText(
                                baseContext,
                                "Your comment was successfully add to database.",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            Handler().postDelayed({ mAlert.dismiss() }, 1000)
                        }
                    } else {
                        Toast.makeText(baseContext, "Enter something ;)", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }else{
                Toast.makeText(
                    baseContext,
                    "Only registered users could add comments.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        voteRCompaniesBt.setOnClickListener {
            when {
                voters.contains(currentUserUID) -> {
                    Toast.makeText(baseContext, "You've already judged it.", Toast.LENGTH_SHORT)
                        .show()
                }
                ratingBar.rating == 0f -> {
                    Toast.makeText(baseContext, "Ups... You should use rating bar.", Toast.LENGTH_SHORT)
                        .show()
                }
                userIsAnonymous ->{
                    Toast.makeText(baseContext, "Only registered users could give rates.", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    votes[0] = votes[0] + ratingBar.rating
                    votes[1] = votes[1] + 1
                    voters.add(currentUserUID)
                    updateDB(voters, votes, db, userUID, title)
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

    private fun paint(comments: ArrayList<String>, container: LinearLayout, votes: ArrayList<Float>, ratingBar: RatingBar){
        paintComments(comments, container)
        paintRating(votes, ratingBar)
    }

    private fun paintComments(comments: ArrayList<String>, container: LinearLayout){
        val text = "Ups... Nobody haven't write any comment yet. Let's make first comment!"
        if(comments.size == 0){
            createText(container, text, this)
            return
        }

        if (comments.size+1 == container.size){
            createNewElement(container, comments.last())
        }else {
            comments.forEach { comment ->
                createNewElement(container, comment)
            }
        }

    }

    private fun paintRating(votes: ArrayList<Float>, ratingBar: RatingBar){
        val rating: Float = votes[0]/votes[1]
        ratingBar.rating = rating

        if (userIsAnonymous){
            ratingBar.setIsIndicator(true)
            ratingBar.isFocusable = false

        }
    }

    private fun updateDB(voters: ArrayList<String>, votes: ArrayList<Float>, db: FirebaseFirestore, userUID: String, title: String){
        val data   = hashMapOf(
            "votes" to votes,
            "voters" to voters
        )
        db.collection(userUID).document("C_"+title)
            .update(data as Map<String, Any>)
    }

    private fun updateComments(comments: ArrayList<String>, db: FirebaseFirestore, userUID: String, title: String): Boolean{
        val data   = hashMapOf(
            "comments" to comments,
        )
        return db.collection(userUID).document("C_"+title)
            .update(data as Map<String, Any>).isSuccessful

    }

    private fun createNewElement(container: LinearLayout, title: String) {
        val newElement = TextView(this)
        newElement.text = title
        newElement.id = container.size + 1
        newElement.textSize = 18f
        container.addView(newElement)
    }

}