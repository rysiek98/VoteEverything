package com.example.voteeverything

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_view_votes.*

class ViewVotesActivity : AppCompatActivity() {

    private var controlHideUIFlag = false
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_votes)
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

        val title = intent.getStringExtra("title")
        val userUID = intent.getStringExtra("userUID")
        val flag = intent.getBooleanExtra("flag", false)
        val docRefSurvey = db.collection(userUID).document(title)
        val docRefUserSurvey = db.collection("dbInfo").document("surveys")
        var options: ArrayList<String>
        var votes: ArrayList<Float> = ArrayList()

        if(flag){
            deleteVVotesBt.visibility = View.VISIBLE
            deleteVVotesBt.setOnClickListener {
                deleteFormDB(title, userUID, docRefSurvey, docRefUserSurvey)
            }
        }

        docRefSurvey.get()
            .addOnSuccessListener { DocumentSnapshot->
                if(DocumentSnapshot.exists()){
                    votes.addAll(DocumentSnapshot.get("votes") as ArrayList<Float>)
                    options = DocumentSnapshot.get("options") as ArrayList<String>
                    setBarChart(votes, options)
                }else{
                    Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext,"Data not found!", Toast.LENGTH_SHORT)
                    .show()
            }
        titleVVotes.text = title

        backVVotesBt.setOnClickListener {
            finish()
        }
    }


    private fun deleteFormDB(
        title: String?,
        userUID: String?,
        docRefSurvey: DocumentReference,
        docRefUserSurvey: DocumentReference
    ) {

        var userToSurvey: ArrayList<String>
        var tmp: ArrayList<String> = ArrayList()
        docRefUserSurvey.get()
            .addOnSuccessListener { DocumentSnapshot->
                if(DocumentSnapshot.exists()){
                    userToSurvey = DocumentSnapshot.get("userToSurvey") as ArrayList<String>
                    for (i in 0 until userToSurvey.size){
                        if (userToSurvey[i] == userUID){
                            if (userToSurvey[i+1] == title){
                                userToSurvey[i+1] = "0"
                                userToSurvey[i] = "0"
                            }
                        }
                    }
                    userToSurvey.removeIf { field -> field == "0" }
                    updateSurveys(userToSurvey, db)
                }else{
                    Toast.makeText(baseContext,"Data not found!",Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext,"Data not found!", Toast.LENGTH_SHORT)
                    .show()
            }
        docRefSurvey.delete()
        Toast.makeText(baseContext,"Data deleted from database", Toast.LENGTH_SHORT)
            .show()
        Handler().postDelayed({ finish() }, 500)
    }

    private fun setBarChart(votes: ArrayList<Float>, xAxisLabels: ArrayList<String>) {
        val entries = ArrayList<BarEntry>()
        val colors = ArrayList<Int>()
        var r = 50
        var g = 100
        var b = 80
        var it: Float = 0f
        var votesSum = 0f
        votes.forEach { vote -> votesSum += vote }
        votes.forEach { vote ->
            entries.add(BarEntry(it, (vote/votesSum)*100))
            if (r < 255 && g < 240 && b < 245 ) {
                r += 5
                g += 25
                b += 10
            }else{
                r = 50
                g = 100
                b = 80
            }
            colors.add(Color.rgb(r,g,b))
            it++
        }
        val barDataSet = BarDataSet(entries, "Total number of votes: $votesSum")
        barDataSet.valueTextSize = 14f
        val data = BarData(barDataSet)
        barChart.data = data
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.textSize = 12f
        barChart.axisLeft.textSize = 12f
        barDataSet.colors = colors
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.setVisibleXRangeMaximum(5f)
        barChart.xAxis.labelCount = votes.size
        barChart.moveViewToX((votes.size/2).toFloat())
        barChart.setPinchZoom(true)
        barChart.invalidate()
        barChart.animateY(2500)
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

    private fun updateSurveys( surveys: ArrayList<String>, db: FirebaseFirestore){
        val data   = hashMapOf(
            "userToSurvey" to surveys
        )
        db.collection("dbInfo").document("surveys")
            .set(data)
    }
}