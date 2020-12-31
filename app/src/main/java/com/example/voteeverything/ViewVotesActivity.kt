package com.example.voteeverything

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
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
                    Handler().postDelayed({ hideSystemUI(window) }, 2000)
                } else {
                    hideSystemUI(window)
                    controlHideUIFlag = false
                }
            }
        }

        val title = intent.getStringExtra("title")
        val userUID = intent.getStringExtra("userUID")
        val flag = intent.getBooleanExtra("flag", false)
        val docRefSurvey = db.collection(userUID).document("S_" + title)
        val docRefUserSurvey = db.collection("dbInfo").document("surveys")
        var options: ArrayList<String>
        var xAxisLabels: ArrayList<String> = ArrayList()
        val votes: ArrayList<Float> = ArrayList()
        val container = findViewById<View>(R.id.viewVotesContainer) as LinearLayout

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
                    for (i in 0 until options.size){
                        xAxisLabels.add("Opcja ${i+1}")
                    }
                    addOptions(xAxisLabels, options, container,this)
                    setBarChart(votes, xAxisLabels)
                }else{
                    Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                    .show()
            }
        titleVVotes.text = title

        backVVotesBt.setOnClickListener {
            finish()
        }
    }

    private fun addOptions(xAxisLabel: ArrayList<String>, options: ArrayList<String>,container: LinearLayout, context: Context) {
        var i = 0
        options.forEach { option ->
           val newElement = TextView(context)
           newElement.text = xAxisLabel[i]+": " + option
           newElement.textSize = 18f
           newElement.setTextColor(Color.BLACK)
           container.addView(newElement)
           i++
       }
    }


    private fun deleteFormDB(
        title: String?,
        userUID: String?,
        docRefSurvey: DocumentReference,
        docRefUserSurvey: DocumentReference
    ) {

        var userToSurvey: ArrayList<String>
        docRefUserSurvey.get()
            .addOnSuccessListener { DocumentSnapshot->
                if(DocumentSnapshot.exists()){
                    userToSurvey = DocumentSnapshot.get("userToSurvey") as ArrayList<String>
                    for (i in 0 until userToSurvey.size){
                        if (userToSurvey[i] == userUID){
                            if (userToSurvey[i + 1] == title){
                                userToSurvey[i + 1] = "0"
                                userToSurvey[i] = "0"
                            }
                        }
                    }
                    userToSurvey.removeIf { field -> field == "0" }
                    updateSurveys(userToSurvey, db)
                }else{
                    Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Data not found!", Toast.LENGTH_SHORT)
                    .show()
            }
        docRefSurvey.delete()
        Toast.makeText(baseContext, "Data deleted from database", Toast.LENGTH_SHORT)
            .show()
        Handler().postDelayed({ finish() }, 500)
    }

    private fun setBarChart(votes: ArrayList<Float>, xAxisLabels: ArrayList<String>) {
        val entries = ArrayList<BarEntry>()
        val colors = ArrayList<Int>()
        var r = 255
        var g = 0
        var b = 0
        var it = 0f
        var votesSum = 0f
        votes.forEach { vote -> votesSum += vote }
        votes.forEach { vote ->
            entries.add(BarEntry(it, (vote / votesSum) * 100))
            colors.add(Color.rgb(r, g, b))
            it++
            r = (20..254).random()
            g = (20..254).random()
            b = (20..254).random()

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
        barChart.xAxis.xOffset = 15f
        barChart.xAxis.granularity = 1f
        barChart.xAxis.setDrawLabels(true)
        barChart.xAxis.spaceMin = 5f
        barChart.xAxis.spaceMax = 25f
        barChart.moveViewToX((votes.size / 2).toFloat())
        barChart.setPinchZoom(true)
        barChart.invalidate()
        barChart.animateY(2500)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI(window)
    }

    private fun updateSurveys(surveys: ArrayList<String>, db: FirebaseFirestore){
        val data   = hashMapOf(
            "userToSurvey" to surveys
        )
        db.collection("dbInfo").document("surveys")
            .set(data)
    }
}